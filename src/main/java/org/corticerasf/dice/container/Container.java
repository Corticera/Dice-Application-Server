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
package org.corticerasf.dice.container;

import org.corticerasf.dice.Loader;
import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.pipeline.Piped;

public interface Container extends Lifecycle, Piped {

	public static final String ADD_CHILD_EVENT = "addChild";
	public static final String REMOVE_CHILD_EVENT = "removeChild";
	public static final String ADD_VALVE_EVENT = "addValve";
	public static final String REMOVE_VALVE_EVENT = "removeValve";
	
	public String getInfo();
	
	public String getName();
	
	public void setName(String name);
	
	public Container getParent();
	
	public void setParent(Container parent);
	
//	TODO Add DirectoryContext support
//	public DirContext getResources();
//	
//	public void setResources(DirContext resources);
	
	public void backgroundProcess();
	
	public void addChild(Container child);
	
	public Container getChild(String name);
	
	public void removeChild(Container child);
	
	public Container[] findChildren();
	
	public void addContainerListener(ContainerListener listener);
	
	public void removeContainerListener(ContainerListener listener);
	
	public ContainerListener[] findContainerListeners();
	
	public void fireContainerEvent(String type, Object data);
	
	public void setStartStopThreads(int threads);
	
	public void setBackgroungProcessorDelay(int backgroundProcessorDelay);
	
	public int getBackgroundProcessorDelay();
	
	public Loader getLoader();
	
	public void setLoader(Loader loader);
	
	// TODO Add accessLogger
	
}
