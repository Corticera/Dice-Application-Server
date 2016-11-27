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
package org.corticerasf.dice;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.corticerasf.dice.core.StandardContext;
import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.lifecycle.LifecycleException;
import org.corticerasf.dice.lifecycle.LifecycleListener;
import org.corticerasf.dice.lifecycle.LifecycleState;
import org.corticerasf.dice.security.SecurityConfig;
import org.corticerasf.dice.startup.ContextConfig;
import org.corticerasf.dice.startup.HostConfig;
import org.corticerasf.dice.utils.ExceptionUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * The <code>ServerManager</code> does the instantiation and
 * startup/stop jobs of a server.
 * 
 * @author J. Godara
 */
public abstract class ServerManager {
	
	protected static final Logger logger = Logger.getLogger(ServerManager.class);
	
	private Server server = null;
	
	protected ClassLoader parentClassLoader = ServerManager.class.getClassLoader();
	protected String configFile = "conf/server.properties";
	protected boolean await = false;
	protected boolean useShutdownHook = true;
	protected Thread shoutdownHook;

    /**
     * Are we starting a new server?
     *
     * @deprecated  Unused - will be removed in DefaultServerManager 2.0.x
     */
    @Deprecated
    protected boolean starting = false;


    /**
     * Are we stopping an existing server?
     *
     * @deprecated  Unused - will be removed in DefaultServerManager 2.0.x
     */
    @Deprecated
    protected boolean stopping = false;
	
	public ServerManager() {
		setSecurityProtection();
	}
	
	public void start() throws Exception {
		if (getServer() == null) 
			load();
		
		if (getServer() == null) {
			logger.fatal("Server instance could not be created.");
			return;
		}
		
		long timpoint1 = System.nanoTime();
		
		try {
			getServer().start();
		} catch (LifecycleException ex) {
			logger.fatal("Server startup failed!", ex);
			try {
				getServer().destroy();
			} catch (LifecycleException ex1) {
				logger.debug("The failed server could not be destroyed", ex1);
			}
			return;
		}
		
		long timepoint2 = System.nanoTime();
		logger.info("Server started in " + ((timepoint2 - timpoint1) / 1000000) + " miliseconds.");
		
		// Register the shutdown hook.
		if (useShutdownHook) {
			if (shoutdownHook == null)
				shoutdownHook = new CorticeraShutdownHook();
			
			Runtime.getRuntime().addShutdownHook(shoutdownHook);
		}
		
		if (await) {
			getServer().await();
			stop();
		}
		
	}
	
	public void stop() {		
		
		try {
			// Remove shutdown hook first so that stop() does not gets called
			// twice.
			if (useShutdownHook)
				Runtime.getRuntime().removeShutdownHook(shoutdownHook);
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
		}
		
		try {
			Server server = getServer();
			LifecycleState state = server.getState();
			if (LifecycleState.STOPPING_PREP.compareTo(state) <= 0
					&& LifecycleState.DESTROYED.compareTo(state) >= 0) {
				// Already Destroyed
			} else {
				server.stop();
				server.destroy();
			}
		} catch (LifecycleException ex) {
			logger.error("DefaultServerManager.stop", ex);
		}
	}
	
	public void load() throws Exception {
		
		long t1 = System.nanoTime();
		
		initDirectories();
		initNaming();
		
		instantiateServer();
		
		getServer().setServerManager(this);
		
		try {
			getServer().init();
		} catch (LifecycleException ex) {
			if (Boolean.getBoolean("corticera.startup.EXIT_ON_FAILURE")) {
				throw new Error(ex);
			} else {
				logger.error("Could not start server.", ex);
			}
		}
		
		long t2 = System.nanoTime();
		logger.info("Server intialized in " + ((t2 - t1) / 1000000) + " miliseconds.");
	}
	
	public void load(String[] args) {
	
		try {
			if (checkAgruments(args)) {
				load();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	protected abstract void initDirectories();
	
	protected abstract void initNaming();
	
	protected void instantiateServer() throws Exception {
		
		String serverClassName = System.getProperty(Globals.DICE_SERVER_CLASS
				, "org.corticerasf.dice.core.StandardServer");
		
		if (logger.isDebugEnabled())
			logger.debug("Using server " + serverClassName);
		
		Server serverInstance = (Server) Class.forName(serverClassName).newInstance();
		
		if (!StringUtils.isEmpty(System.getProperty("corticera.port"))) {
			try {
				int port = Integer.parseInt(System.getProperty("corticera.port"));
				serverInstance.setPort(port);
			} catch (NumberFormatException ex) {
				logger.warn("The port is malformed, using default.", ex);
			} catch (Exception e) {
				logger.warn("Cannot read port from system properties, using default.", e);
			}
		}

		InputStream is = null;
		Throwable error = null;
		try {
			String confUrl = System.getProperty(Globals.DICE_CONFIG_PROP);
			if (!StringUtils.isEmpty(confUrl))
				is = new FileInputStream(new File(confUrl));
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
		}
		
		if (is == null) {
			try {
				File home = new File(Globals.getDiceBase());
				File conf = new File(home, "conf");
				File corticeraXml = new File(conf, "dice.xml");
				is = new FileInputStream(corticeraXml);
			} catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);
				error = t;
			}
		}
		
		if (is == null || error != null) {
			Exception ex = new Exception("corticera.xml could not be loaded.", error);
			logger.error(ex);
			if (Boolean.getBoolean("corticera.startup.EXIT_ON_FAILURE")) {
				return;
			}
		}
		
		try {
			loadConfigFile(is, serverInstance);
		} catch (Throwable t) {
			logger.fatal("Cannot load corticera.xml file!!!!!", t);
			return;
		}
		
		setServer(serverInstance);
	}
	
	protected void loadConfigFile(InputStream is, Server serverInstance) throws Exception {		
		DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
		
		documentBuilder.setEntityResolver(new EntityResolver() {
			
			public InputSource resolveEntity(String publicId, String systemId)
					throws SAXException, IOException {
				systemId = systemId.substring(systemId.lastIndexOf('/') + 1);
				if ("server.dtd".equalsIgnoreCase(systemId)) {
					return new InputSource(new FileInputStream(new File(Globals.getDiceBase() + "/conf/" + systemId)));
				} else {
					return null;
				}
			}
		});
		
		Document document = documentBuilder.parse(is);
		
		document.getDocumentElement().normalize();
		XPath xPath = XPathFactory.newInstance().newXPath();
		
		registerListeners((Element) xPath.evaluate("/Corticera", document.getDocumentElement(), XPathConstants.NODE), serverInstance);
		
		NodeList servicesInDOM = (NodeList) xPath.evaluate("/Corticera/Srvc", document.getDocumentElement(), XPathConstants.NODESET);
		
		for (int i = 0 ; i < servicesInDOM.getLength() ; i++) {
			Element serviceNode = (Element) servicesInDOM.item(i);
			
			String serviceClassName = System.getProperty(Globals.DICE_SERVER_SERVICE_CLASS,
										"org.corticerasf.dice.core.StandardService");
			
			if (serverInstance.findService(serviceNode.getAttribute("name")) != null) {
				logger.warn("Cannot add service " + serviceNode.getAttribute("name") + " (Already Added).");
				continue;
			}
			
			if (!StringUtils.isEmpty(serviceNode.getAttribute("class")))
				serviceClassName = serviceNode.getAttribute("class");
			
			Service serviceObject = (Service) Class.forName(serviceClassName).newInstance();
			serviceObject.setName(serviceNode.getAttribute("name"));
			serviceObject.setServer(serverInstance);			
			
			Engine engineInstance = createEngine(serviceNode);
			registerListeners(serviceNode, serviceObject);
			
			serviceObject.setContainer(engineInstance);		
			
			if (logger.isDebugEnabled())
				logger.debug("Adding service " + serviceObject.getInfo());
			
			serverInstance.addService(serviceObject);			
		}
	}
	
	private void registerListeners(Element node, Lifecycle target) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		NodeList listenersInDOM = node.getElementsByTagName("Listener");
		
		for (int i = 0 ; i < listenersInDOM.getLength() ; i++) {
			Element listenerNode = (Element) listenersInDOM.item(i);
			
			LifecycleListener listener = (LifecycleListener) 
					Class.forName(listenerNode.getAttribute("class")).newInstance();
			
			if (logger.isDebugEnabled())
				logger.debug("Adding lifecycle listener " + listener.getClass().getName() + ".");
			
			target.addLifecycleListener(listener);
		}
	}
	
	private Engine createEngine(Element serviceNode) throws InstantiationException, IllegalAccessException, ClassNotFoundException {;
	
		Map<String, Service> engineRef = new HashMap<String, Service>();
		
		Element engineNode = (Element) serviceNode.getElementsByTagName("Engine").item(0);
		
		if (engineRef.get(engineNode.getAttribute("name")) != null) {
			IllegalArgumentException ex = new IllegalArgumentException("Engine name '"
					+ engineNode.getAttribute("name") + " is already bound to " + engineRef.get(engineNode.getAttribute("name")));
			throw ex;
		}
		
		String engineClassName = System.getProperty(Globals.DICE_SERVER_SERVICE_ENGINE_CLASS,
									"org.corticerasf.dice.core.StandardEngine");
		
		if (!StringUtils.isEmpty(engineNode.getAttribute("class")))
			engineClassName = engineNode.getAttribute("class");
		
		Engine engineInstance = (Engine) Class.forName(engineClassName).newInstance();
		engineInstance.setName(engineNode.getAttribute("name"));
		
		List<Host> hosts = createHosts(engineNode);
		for (Host host : hosts) {
			engineInstance.addChild(host);
		}
		
		registerListeners(engineNode, engineInstance);
		
		return engineInstance;
	}
	
	private List<Host> createHosts(Element engineNode) 
			throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		
		List<Host> hosts = new ArrayList<Host>();
		NodeList hostNodes = engineNode.getElementsByTagName("Host");
		for (int j = 0 ; j < hostNodes.getLength() ; j++) {
			Element hostNode = (Element) hostNodes.item(j);
			
			String hostClassName = System.getProperty(Globals.DICE_SERVER_SERCIVE_ENGINE_HOST_CLASS, 
									"org.corticerasf.dice.core.StandardHost");
			
			Host hostInstance = (Host) Class.forName(hostClassName).newInstance();
			
			hostInstance.setAppBase(hostNode.getAttribute("docbase"));
			hostInstance.setName(hostNode.getAttribute("name"));
			
			registerListeners(hostNode, hostInstance);
			
			boolean hasHostConfigListener = false;			
			for (LifecycleListener listener : hostInstance.findLifecycleListeners()) {
				if (listener instanceof HostConfig) {
					hasHostConfigListener = true;
					break;
				}
			}
			
			if (!hasHostConfigListener) {
				LifecycleListener hostConfigListener = new HostConfig();
				hostInstance.addLifecycleListener(hostConfigListener);
			}
			
			Context context = new StandardContext();
			context.addLifecycleListener(new ContextConfig());
			
			// hostInstance.addChild(context);
			
			hosts.add(hostInstance);
		}
		
		return hosts;
	}
	
	protected void setSecurityProtection() {
		SecurityConfig securityConfig = SecurityConfig.newInstance();
		securityConfig.setPackageAccess();
		securityConfig.setPackageDefinition();
	}
	
	protected boolean checkAgruments(String[] args) {

        boolean isConfig = false;

        if (args.length < 1) {
            usage();
            return false;
        }

        for (int i = 0; i < args.length; i++) {
            if (isConfig) {
                configFile = args[i];
                isConfig = false;
            } else if (args[i].equals("-config")) {
                isConfig = true;
            } else if (args[i].equals("-help")) {
                usage();
                return (false);
            } else if (args[i].equals("start")) {
                starting = true;
                stopping = false;
            } else if (args[i].equals("configtest")) {
                starting = true;
                stopping = false;
            } else if (args[i].equals("stop")) {
                starting = false;
                stopping = true;
            } else {
                usage();
                return false;
            }
        }

        return true;

	}
	
	protected void usage() {

        System.out.println
            ("usage: java org.corticerasf.dice.ServerManager"
             + " [ -config {pathname} ]"
             + " { -help | start | stop }");

    }

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public ClassLoader getParentClassLoader() {
		return parentClassLoader;
	}

	public void setParentClassLoader(ClassLoader parentClassLoader) {
		this.parentClassLoader = parentClassLoader;
	}

	public boolean isAwait() {
		return await;
	}

	public void setAwait(boolean await) {
		this.await = await;
	}
	
	protected class CorticeraShutdownHook extends Thread {
		
		@Override
		public void run() {
			try {
				if (getServer() != null) {
					ServerManager.this.stop();
				}
			} catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);
				logger.error("Shoutdown Hook Failed!!!", t);
			}
		}
		
	}

}
