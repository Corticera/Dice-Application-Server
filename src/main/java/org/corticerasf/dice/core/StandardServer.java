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

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.security.AccessControlException;
import java.util.Random;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.corticerasf.dice.Server;
import org.corticerasf.dice.ServerManager;
import org.corticerasf.dice.Service;
import org.corticerasf.dice.lifecycle.LifecycleBase;
import org.corticerasf.dice.lifecycle.LifecycleException;
import org.corticerasf.dice.lifecycle.LifecycleState;

public class StandardServer extends LifecycleBase implements Server {

	private static final Logger logger = Logger.getLogger(StandardServer.class);

	private String address = null;
	private int port = 8808;
	private ServerManager serverManager = null;
	private String shutdown = "SHUTDOWN";
	private Random random = null;
	private Service[] services = new Service[0];
	private ClassLoader parentClassLoader = null;

	private volatile Thread awaitThread = null;
	private volatile boolean stopAwait = false;
	private volatile ServerSocket awaitSocket = null;

	private final Object servicesLock = new Object();

	public String getInfo() {
		return getClass().getName() + "/1.0";
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddress() {
		return address;
	}

	public void setShutdown(String shutdown) {
		this.shutdown = shutdown;
	}

	public String getShutdown() {
		return shutdown;
	}

	public void setServerManager(ServerManager serverManager) {
		this.serverManager = serverManager;
	}

	public ServerManager getServerManager() {
		return serverManager;
	}

	@Override
	protected void initInternal() throws LifecycleException {
		for (Service service : findServices()) {
			service.init();
		}
	}

	@Override
	protected void startInternal() throws LifecycleException {
		setState(LifecycleState.STARTING);
		
		synchronized (servicesLock) {
			for (Service service : findServices()) {
				service.start();
			}
		}
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		
		setState(LifecycleState.STOPPING);
		fireLifecycleEvent(CONFIGURE_STOP_EVENT, null);
		
		for (Service service : findServices()) {
			service.stop();
		}
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		for (Service service : findServices()) {
			service.destroy();
		}
	}

	public void await() {

		if (port == -1) {
			try {
				awaitThread = Thread.currentThread();
				while (!stopAwait) {
					try {
						Thread.sleep(10000);
					} catch (InterruptedException ex) {
					}
				}
			} finally {
				awaitThread = null;
			}
			return;
		}

		try {
			awaitSocket = new ServerSocket(port, 1,
					InetAddress.getByName(address));
		} catch (IOException ex) {
			logger.error(
					"Could not start server[" + address + ":" + port + "]", ex);
			return;
		}

		try {
			awaitThread = Thread.currentThread();

			while (!stopAwait) {
				ServerSocket serverSocket = awaitSocket;
				if (serverSocket == null) {
					break;
				}

				Socket socket = null;
				StringBuilder command = new StringBuilder();
				try {
					InputStream stream;
					try {
						socket = serverSocket.accept();
						socket.setSoTimeout(15 * 1000); // 15 seconds
						stream = socket.getInputStream();
					} catch (SocketTimeoutException stex) {
						logger.warn("Server.accept() timed out.", stex);
						continue;
					} catch (AccessControlException acex) {
						logger.warn("Server.accept() security exception", acex);
						continue;
					} catch (IOException ex) {
						if (stopAwait) {
							break;
						}
						logger.error("Server.await(): accept: ", ex);
						break;
					}

					// Read a set of characters from the socket
					int expected = 1024; // Cut off to avoid DoS attack
					while (expected < shutdown.length()) {
						if (random == null)
							random = new Random();
						expected += (random.nextInt() % 1024);
					}
					while (expected > 0) {
						int ch = -1;
						try {
							ch = stream.read();
						} catch (IOException e) {
							logger.warn("Server.await(): read: ", e);
							ch = -1;
						}
						// Control character or EOF (-1) terminates loop
						if (ch < 32 || ch == 127) {
							break;
						}
						command.append((char) ch);
						expected--;
					}
				} finally {
					if (socket != null) {
						try {
							socket.close();
						} catch (IOException e) {
						}
					}
				}
			}
		} finally {
			ServerSocket serverSocket = awaitSocket;
			awaitSocket = null;
			awaitThread = null;

			if (serverSocket != null) {
				try {
					serverSocket.close();
				} catch (IOException ex) {
				}
			}
		}
	}

	public Service[] findServices() {
		return services;
	}

	public void addService(Service service) {

		service.setServer(this);

		synchronized (servicesLock) {
			Service results[] = new Service[services.length + 1];
			System.arraycopy(services, 0, results, 0, services.length);
			results[services.length] = service;
			services = results;

			if (getState().isAvailable()) {
				try {
					service.start();
				} catch (LifecycleException e) {
					// Ignore
				}
			}

			// TODO Add property change support
			// Report this property change to interested listeners
			// support.firePropertyChange("service", null, service);
		}

	}

	public void removeService(Service service) {

		synchronized (servicesLock) {
			int j = -1;
			for (int i = 0; i < services.length; i++) {
				if (service == services[i]) {
					j = i;
					break;
				}
			}
			if (j < 0)
				return;
			try {
				services[j].stop();
			} catch (LifecycleException e) {
				// Ignore
			}
			int k = 0;
			Service results[] = new Service[services.length - 1];
			for (int i = 0; i < services.length; i++) {
				if (i != j)
					results[k++] = services[i];
			}
			services = results;

			// TODO Add property change support
			// Report this property change to interested listeners
			// support.firePropertyChange("service", service, null);
		}

	}

	public Service findService(String name) {

		if (StringUtils.isEmpty(name)) {
			return null;
		}
		synchronized (servicesLock) {
			for (int i = 0; i < services.length; i++) {
				if (name.equals(services[i].getName())) {
					return (services[i]);
				}
			}
		}
		return null;

	}

	public void setParentClassLoader(ClassLoader loader) {
		this.parentClassLoader = loader;
	}

	public ClassLoader getParentClassLoader() {
		if (parentClassLoader != null)
			return parentClassLoader;
		
		if (serverManager != null)
			return serverManager.getParentClassLoader();
		
		return ClassLoader.getSystemClassLoader();
	}

}
