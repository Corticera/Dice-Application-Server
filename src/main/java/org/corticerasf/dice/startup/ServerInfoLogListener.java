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

import org.apache.log4j.Logger;
import org.corticerasf.dice.Globals;
import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.lifecycle.LifecycleEvent;
import org.corticerasf.dice.lifecycle.LifecycleListener;

public class ServerInfoLogListener implements LifecycleListener {
	
	private static final Logger logger = Logger.getLogger(ServerInfoLogListener.class);

	public void lifecycleEvent(LifecycleEvent event) {
		if (event.getType().equals(Lifecycle.BEFORE_INIT_EVENT)) {
			logger.info("Corticera Application Server v1.0");
			
			logger.info("Operating System Name: " + System.getProperty("os.name"));
			logger.info("Operating System Version: " + System.getProperty("os.version"));
			logger.info("Operating System Architecture: " + System.getProperty("os.arch"));
			
			logger.info("Java Home: " + System.getProperty("java.home"));
			logger.info("Java VM Version: " + System.getProperty("java.runtime.version"));
			logger.info("Java VM Vendor: " + System.getProperty("java.vm.vendor"));
			
			logger.info("Corticera Home: " + Globals.getCorticeraHome());
			logger.info("Corticera Base: " + Globals.getDiceBase());
		}
	}

}
