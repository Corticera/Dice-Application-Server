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
package org.corticerasf.dice.core;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.corticerasf.dice.Context;
import org.corticerasf.dice.Host;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.container.ContainerBase;
import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.lifecycle.LifecycleEvent;
import org.corticerasf.dice.lifecycle.LifecycleListener;

public class StandardHost extends ContainerBase implements Host {

	private static final Logger logger = Logger.getLogger(StandardHost.class);

	private String[] aliases = new String[0];
	private final Object aliasesLock = new Object();
	private String appBase = "webcontent";
	private String xmlBase = null;
	private String workDir = null;
	private boolean createDirs = true;
	private boolean hotDeployment = true;

	// Classloaders of web applications
	private Map<ClassLoader, String> childClassLoaders = new HashMap<ClassLoader, String>();

	private Pattern deployIgnore = null;

	private String configClass = "org.corticerasf.dice.startup.ContextConfig";
	private String contextClass = "org.corticerasf.dice.core.StandardContext";

	public String getXmlBase() {
		return xmlBase;
	}

	public void setXmlBase(String xmlBase) {
		this.xmlBase = xmlBase;
	}

	public String getAppBase() {
		return appBase;
	}

	public void setAppBase(String appBase) {
		if (StringUtils.isEmpty(appBase))
			logger.warn("There is a problem with the app base. " + getName());

		this.appBase = appBase;
	}

	public String getConfigClass() {
		return configClass;
	}

	public void setConfigClass(String configClass) {
		this.configClass = configClass;
	}

	public String getWorkDir() {
		return workDir;
	}

	public void setWorkDir(String workDir) {
		this.workDir = workDir;
	}

	public String getDeployIgnore() {
		if (deployIgnore == null)
			return null;

		return deployIgnore.toString();
	}

	public Pattern getDeployIgnorePattern() {
		return deployIgnore;
	}

	public void setDeployIgnore(String deployIgnore) {
		if (deployIgnore == null)
			this.deployIgnore = null;
		else
			this.deployIgnore = Pattern.compile(deployIgnore);
	}

	public ExecutorService getStartStopExecutor() {
		return startStopExecutor;
	}

	public void addAlias(String alias) {
		alias = alias.toLowerCase(Locale.ENGLISH);

		synchronized (aliasesLock) {
			// Skip duplicate aliases
			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].equals(alias))
					return;
			}
			// Add this alias to the list
			String newAliases[] = new String[aliases.length + 1];
			for (int i = 0; i < aliases.length; i++)
				newAliases[i] = aliases[i];
			newAliases[aliases.length] = alias;
			aliases = newAliases;
		}
	}

	public String[] findAliases() {
		synchronized (aliasesLock) {
			return aliases;
		}
	}

	public void removeAlias(String alias) {

		alias = alias.toLowerCase(Locale.ENGLISH);

		synchronized (aliasesLock) {

			// Make sure this alias is currently present
			int n = -1;
			for (int i = 0; i < aliases.length; i++) {
				if (aliases[i].equals(alias)) {
					n = i;
					break;
				}
			}
			if (n < 0)
				return;

			// Remove the specified alias
			int j = 0;
			String results[] = new String[aliases.length - 1];
			for (int i = 0; i < aliases.length; i++) {
				if (i != n)
					results[j++] = aliases[i];
			}
			aliases = results;

		}

		// Inform interested listeners
		fireContainerEvent(REMOVE_ALIAS_EVENT, alias);

	}

	public boolean getCreateDirs() {
		return createDirs;
	}

	public void setCreateDirs(boolean createDirs) {
		this.createDirs = createDirs;
	}

	@Override
	public void addChild(Container child) {
		child.addLifecycleListener(new MemoryLeakTrackingListener());

		if (!(child instanceof Context)) {
			throw new IllegalArgumentException("Child " + child
					+ " must be an instance of " + Context.class);
		}

		super.addChild(child);
	}

	@Override
	public void setName(String name) {
		if (StringUtils.isEmpty(name))
			throw new IllegalArgumentException(
					"StandardHost.setName(): Empty name.");

		this.name = name;
	}

	@Override
	public String getInfo() {
		return "StandardHost[" + getName() + "]/1.0";
	}

	@Override
	public String toString() {
		return getInfo();
	}

	public String getContextClass() {
		return contextClass;
	}

	public void setContextClass(String contextClass) {
		this.contextClass = contextClass;
	}

	/**
	 * Used to ensure the regardless of {@link Context} implementation, a record
	 * is kept of the class loader used every time a context starts.
	 */
	private class MemoryLeakTrackingListener implements LifecycleListener {

		public void lifecycleEvent(LifecycleEvent event) {
			if (event.getType().equals(Lifecycle.AFTER_START_EVENT)) {
				if (event.getSource() instanceof Context) {
					Context context = ((Context) event.getSource());
					childClassLoaders.put(context.getLoader().getClassLoader(),
							context.getServletContext().getContextPath());
				}
			}
		}
	}

	public boolean isHotDeploymentEnabled() {
		return hotDeployment;
	}

	public void setHotDeployment(boolean hotDeployment) {
		this.hotDeployment = hotDeployment;
	}

}
