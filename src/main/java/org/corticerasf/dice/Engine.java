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

import org.corticerasf.dice.container.Container;

/**
 * An <b>Engine</b> represents entire DefaultServerManager Servlet Engine.
 * <p>
 * The containers inside the Engine are generally implementations of Host
 * (representing a virtual host) or Context (representing an individual servlet
 * context), depending upon the implementation..
 * <p>
 * If used, an Engine is always the top level container in DefaultServerManager hierarchy.
 * Therefore, the implementation's <code>setParent()</code> method should throw
 * <code>IllegalArgumentException</code>.
 * 
 * @author J. Godara
 */
public interface Engine extends Container {

	public String getName();

	public void setName(String name);

	public void setDefaultHost(String defaultHost);

	public String getDefaultHost();

	public void setService(Service service);

	public Service getService();

}
