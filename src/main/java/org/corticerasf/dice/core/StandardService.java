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

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.corticerasf.dice.Executor;
import org.corticerasf.dice.Server;
import org.corticerasf.dice.Service;
import org.corticerasf.dice.connector.Connector;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.lifecycle.LifecycleBase;
import org.corticerasf.dice.lifecycle.LifecycleException;
import org.corticerasf.dice.lifecycle.LifecycleState;

public class StandardService extends LifecycleBase implements Service {

	private static Logger logger = Logger.getLogger(StandardServer.class);

	private String name = "StandardService";

	private Server server = null;
	private Container container = null;

	private Connector[] connectors = new Connector[0];
	private ArrayList<Executor> executors = new ArrayList<Executor>();

	private final Object connectorsLock = new Object();

	public String getInfo() {
		return getClass().getName() + "[" + getName() + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Server getServer() {
		return server;
	}

	public void setServer(Server server) {
		this.server = server;
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	@Override
	protected void initInternal() throws LifecycleException {

		if (container != null)
			container.init();

		for (Executor executor : findExecutors()) {
			// TODO Executor Domain
			executor.init();
		}

		synchronized (connectorsLock) {
			try {
				for (Connector connector : connectors) {
					connector.init();
				}
			} catch (Exception ex) {
				LifecycleException e = new LifecycleException(
						"Error while initializing connectors.", ex);
				logger.error(e);
				if (Boolean
						.getBoolean("corticera.startup.EXIT_ON_INIT_FAILURE")) {
					throw e;
				}
			}
		}
		
		if (logger.isDebugEnabled())
			logger.debug("Service " + getInfo() + " initailized.");

	}

	@Override
	protected void startInternal() throws LifecycleException {
		logger.info("Starting service " + getInfo() + "...");
		setState(LifecycleState.STARTING);

		if (container != null) {
			synchronized (container) {
				container.start();
			}
		}

		synchronized (executors) {
			for (Executor executor : findExecutors()) {
				executor.start();
			}
		}

		synchronized (connectorsLock) {
			try {
				for (Connector connector : connectors) {
					if (!connector.getState().equals(LifecycleState.FAILED)) {
						// Only start a connector if it has not failed.
						connector.start();
					}
				}
			} catch (Exception ex) {
				LifecycleException e = new LifecycleException(
						"Error while stopping connectors.", ex);
				logger.error(e);
			}
		}
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		synchronized (connectorsLock) {
			try {
				for (Connector connector : connectors) {
					connector.pause();
				}
			} catch (Exception ex) {
				LifecycleException e = new LifecycleException(
						"Error while pausing connectors.", ex);
				logger.error(e);
			}
		}

		logger.info("Pausing service " + getInfo());
		setState(LifecycleState.STOPPING);

		if (container != null) {
			synchronized (container) {
				container.stop();
			}
		}

		synchronized (connectorsLock) {
			try {
				for (Connector connector : connectors) {
					if (!connector.getState().equals(LifecycleState.STARTED)) {
						// Connectors only need stopping if they are currently
						// started. They may have failed to start or may have
						// been
						// stopped (e.g. via a JMX call)

						continue;
					}

					connector.stop();
				}
			} catch (Exception ex) {
				LifecycleException e = new LifecycleException(
						"Error while stopping connectors.", ex);
				logger.error(e);
			}
		}

		synchronized (executors) {
			for (Executor executor : findExecutors()) {
				executor.stop();
			}
		}
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		synchronized (connectorsLock) {
			try {
				for (Connector connector : connectors) {
					connector.destroy();
				}
			} catch (Exception ex) {
				LifecycleException e = new LifecycleException(
						"Error while destroying connectors.", ex);
				logger.error(e);
			}
		}

		for (Executor executor : findExecutors()) {
			executor.destroy();
		}

		if (container != null)
			container.destroy();
	}

	public Connector[] findConnectors() {
		return connectors;
	}

	public void addConnector(Connector connector) {

		synchronized (connectorsLock) {
			connector.setService(this);
			Connector results[] = new Connector[connectors.length + 1];
			System.arraycopy(connectors, 0, results, 0, connectors.length);
			results[connectors.length] = connector;
			connectors = results;

			if (getState().isAvailable()) {
				try {
					connector.start();
				} catch (LifecycleException e) {
					logger.error("Failed to start connector " + connector, e);
				}
			}

			// TODO Add property change support
			// Report this property change to interested listeners
			// support.firePropertyChange("connector", null, connector);
		}

	}

	public void removeConnector(Connector connector) {

		synchronized (connectorsLock) {
			int j = -1;
			for (int i = 0; i < connectors.length; i++) {
				if (connector == connectors[i]) {
					j = i;
					break;
				}
			}
			if (j < 0)
				return;
			if (connectors[j].getState().isAvailable()) {
				try {
					connectors[j].stop();
				} catch (LifecycleException e) {
					logger.error("Failed to stop connector " + connector, e);
				}
			}
			connector.setService(null);
			int k = 0;
			Connector results[] = new Connector[connectors.length - 1];
			for (int i = 0; i < connectors.length; i++) {
				if (i != j)
					results[k++] = connectors[i];
			}
			connectors = results;

			// TODO Add property change support
			// Report this property change to interested listeners
			// support.firePropertyChange("connector", connector, null);
		}

	}

	public void addExecutor(Executor ex) {
		synchronized (executors) {
			if (!executors.contains(ex)) {
				executors.add(ex);
				if (getState().isAvailable()) {
					try {
						ex.start();
					} catch (LifecycleException x) {
						logger.error("Executor.start", x);
					}
				}
			}
		}
	}

	public Executor[] findExecutors() {
		synchronized (executors) {
			Executor[] result = new Executor[executors.size()];
			executors.toArray(result);
			return result;
		}
	}

	public Executor getExecutor(String executorName) {
		synchronized (executors) {
			for (Executor executor : executors) {
				if (executorName.equals(executor.getName()))
					return executor;
			}
		}
		return null;
	}

	public void removeExecutor(Executor ex) {
		synchronized (executors) {
			if (executors.remove(ex) && getState().isAvailable()) {
				try {
					ex.stop();
				} catch (LifecycleException e) {
					logger.error("Executor.stop", e);
				}
			}
		}
	}
	
	@Override
	public String toString() {
		return getInfo();
	}

}
