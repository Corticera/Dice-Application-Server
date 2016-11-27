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

import org.corticerasf.dice.connector.Connector;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.lifecycle.Lifecycle;

public interface Service extends Lifecycle {
	
	public String getInfo();
	
	public String getName();
	
	public void setName(String name);
	
	public Server getServer();
	
	public void setServer(Server server);
	
	public Container getContainer();
	
	public void setContainer(Container container);
	
	public Connector[] findConnectors();
	
	public void addConnector(Connector connector);
	
	public void removeConnector(Connector connector);
	
	public void addExecutor(Executor executor);
	
	public Executor[] findExecutors();
	
	public Executor getExecutor(String name);
	
	public void removeExecutor(Executor executor);

}
