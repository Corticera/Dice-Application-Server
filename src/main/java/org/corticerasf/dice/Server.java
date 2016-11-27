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

import org.corticerasf.dice.lifecycle.Lifecycle;

/**
 * A <code>Server</code> element represents the entire Corticera
 * servlet container.  Its attributes represent the characteristics of
 * the servlet container as a whole.  A <code>Server</code> may contain
 * one or more <code>Services</code>, and the top level set of naming
 * resources.
 * <p>
 * Normally, an implementation of this interface will also implement
 * <code>Lifecycle</code>, such that when the <code>start()</code> and
 * <code>stop()</code> methods are called, all of the defined
 * <code>Services</code> are also started or stopped.
 * <p>
 * In between, the implementation must open a server socket on the port number
 * specified by the <code>port</code> property.  When a connection is accepted,
 * the first line is read and compared with the specified shutdown command.
 * If the command matches, shutdown of the server is initiated.
 * <strong>NOTE</strong> - The concrete implementation of this class should
 * register the (singleton) instance with the <code>ServerFactory</code>
 * class in its constructor(s).
 * 
 * TODO: Implement JNDI
 * 
 * @author J. Godara
 */
public interface Server extends Lifecycle {
	
	public String getInfo();
	
	public int getPort();
	
	public void setPort(int port);
	
	public void setAddress(String address);
	
	public String getAddress();
	
	public void setShutdown(String shutdown);
	
	public String getShutdown();
	
	public void setServerManager(ServerManager serverManager);
	
	public ServerManager getServerManager();
	
	public void await();
	
	public Service[] findServices();
	
	public Service findService(String serviceName);
	
	public void addService(Service service);
	
	public void removeService(Service service);
	
	public void setParentClassLoader(ClassLoader loader);
	
	public ClassLoader getParentClassLoader();

}
