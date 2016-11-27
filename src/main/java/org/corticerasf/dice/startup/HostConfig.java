/*
 * Copyright 2016 Corticera Software Foundation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.corticerasf.dice.startup;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.corticerasf.dice.Engine;
import org.corticerasf.dice.Globals;
import org.corticerasf.dice.Host;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.lifecycle.LifecycleEvent;
import org.corticerasf.dice.lifecycle.LifecycleListener;
import org.corticerasf.dice.utils.ContextName;

public class HostConfig implements LifecycleListener {

	private static final Logger logger = Logger.getLogger(HostConfig.class);

	private Host host = null;
	private File appBase = null;
	private File configBase = null;
	private List<String> serviced = new ArrayList<String>();
	private Map<String, DeployedApplication> deployed = new HashMap<String, DeployedApplication>();
	
	public void lifecycleEvent(LifecycleEvent event) {

		host = (Host) event.getLifecycle();
		
		if (event.getType().equals(Lifecycle.PERIODIC_EVENT)) {
			check();
		} else if (event.getType().equals(Lifecycle.BEFORE_START_EVENT)) {
			//beforeStart();
		} else if (event.getType().equals(Lifecycle.START_EVENT)) {
			//start();
		} else if (event.getType().equals(Lifecycle.STOP_EVENT)) {
			//stop();
		}

	}

	protected void check() {
		if (host.isHotDeploymentEnabled()) {
			deployWebapps();
		}
	}

	protected void deployWebapps() {
		File appBase = appBase();
		File configBase = configBase();
		String[] filteredAppPaths = filterAppPaths(appBase.list());

		deployDescriptors(configBase, configBase.list());

//		deployWars(appBase, filteredAppPaths);
//
//		deployDirectories(appBase, filteredAppPaths);

	}
	
	protected void deployDescriptor(ContextName cn, File contextXml) {
		DeployedApplication deployedApp = new DeployedApplication(cn.getName(), true);
		
		long startTime = 0;
		
		
	}

	protected void deployDescriptors(File configBase, String[] files) {

		if (files == null)
			return;

		ExecutorService es = host.getStartStopExecutor();
		List<Future<?>> results = new ArrayList<Future<?>>();

		for (int i = 0; i < files.length; i++) {
			File contextXml = new File(configBase, files[i]);

			if (files[i].toLowerCase(Locale.ENGLISH).endsWith(".xml")) {
				ContextName cn = new ContextName(files[i], true);

				if (isServiced(cn.getName()) || deploymentExists(cn.getName()))
					continue;

				results.add(es.submit(new DeployDescriptor(this, cn, contextXml)));
			}
		}

		for (Future<?> result : results) {
			try {
				result.get();
			} catch (Exception e) {
				logger.error("Error in deploy descriptor thread.", e);
			}
		}
	}
	
	protected boolean deploymentExists(String name) {
		return deployed.containsKey(name) || host.getChild(name) != null;
	}

	protected String[] filterAppPaths(String[] unfilteredAppPaths) {
		Pattern filter = host.getDeployIgnorePattern();
		if (filter == null || unfilteredAppPaths == null)
			return unfilteredAppPaths;

		List<String> filterdList = new ArrayList<String>();
		Matcher matcher = null;
		for (String appPath : unfilteredAppPaths) {
			if (matcher == null)
				matcher = filter.matcher(appPath);
			else
				matcher.reset();

			if (matcher.matches()) {
				if (logger.isDebugEnabled()) {
					logger.debug("Ignoring path " + appPath);
				}
			} else {
				filterdList.add(appPath);
			}
		}

		return filterdList.toArray(new String[filterdList.size()]);
	}

	protected File appBase() {
		if (appBase != null)
			return appBase;

		appBase = returnCanonicalPath(host.getAppBase());
		return appBase;
	}

	protected File configBase() {
		if (configBase != null)
			return configBase;

		if (host.getXmlBase() != null) {
			configBase = returnCanonicalPath(host.getXmlBase());
		} else {
			String xmlDir = "conf";
			Container parent = host.getParent();
			if (parent instanceof Engine)
				xmlDir += "/" + parent.getName();
			xmlDir += "/" + host.getName();
			configBase = returnCanonicalPath(xmlDir);
		}

		return configBase;
	}

	protected File returnCanonicalPath(String path) {
		File file = new File(path);
		File base = new File(Globals.getDiceBase());
		if (!file.isAbsolute()) {
			file = new File(base, path);
		}

		try {
			return file.getCanonicalFile();
		} catch (IOException ex) {
			return file;
		}

	}
	
	public synchronized void addServiced(String name) {
		serviced.add(name);
	}
	
	public synchronized boolean isServiced(String name) {
		return serviced.contains(name);
	}
	
	public synchronized void removeServiced(String name) {
		serviced.remove(name);
	}
	
	private static class DeployDescriptor implements Runnable {

        private HostConfig config;
        private ContextName cn;
        private File descriptor;

        public DeployDescriptor(HostConfig config, ContextName cn,
                File descriptor) {
            this.config = config;
            this.cn = cn;
            this.descriptor= descriptor;
        }

        public void run() {
            config.deployDescriptor(cn, descriptor);
        }
    }
	
	/**
     * This class represents the state of a deployed application, as well as
     * the monitored resources.
     */
    protected static class DeployedApplication {
        public DeployedApplication(String name, boolean hasDescriptor) {
            this.name = name;
            this.hasDescriptor = hasDescriptor;
        }

        /**
         * Application context path. The assertion is that
         * (host.getChild(name) != null).
         */
        public String name;

        /**
         * Does this application have a context.xml descriptor file on the
         * host's configBase?
         */
        public final boolean hasDescriptor;

        /**
         * Any modification of the specified (static) resources will cause a
         * redeployment of the application. If any of the specified resources is
         * removed, the application will be undeployed. Typically, this will
         * contain resources like the context.xml file, a compressed WAR path.
         * The value is the last modification time.
         */
        public LinkedHashMap<String, Long> redeployResources =
            new LinkedHashMap<String, Long>();

        /**
         * Any modification of the specified (static) resources will cause a
         * reload of the application. This will typically contain resources
         * such as the web.xml of a webapp, but can be configured to contain
         * additional descriptors.
         * The value is the last modification time.
         */
        public HashMap<String, Long> reloadResources =
            new HashMap<String, Long>();

        /**
         * Instant where the application was last put in service.
         */
        public long timestamp = System.currentTimeMillis();

        /**
         * In some circumstances, such as when unpackWARs is true, a directory
         * may be added to the appBase that is ignored. This flag indicates that
         * the user has been warned so that the warning is not logged on every
         * run of the auto deployer.
         */
        public boolean loggedDirWarning = false;
    }

}
