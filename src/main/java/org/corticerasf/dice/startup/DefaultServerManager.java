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

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.corticerasf.dice.Globals;
import org.corticerasf.dice.ServerManager;

public class DefaultServerManager extends ServerManager {
	
	private static final Logger logger = Logger.getLogger(DefaultServerManager.class);

	@Override
	protected void initDirectories() {
		
		String corticeraHome = System.getProperty(Globals.DICE_HOME_PROP);
		if (StringUtils.isEmpty(corticeraHome)) {
			String j2eeHome = System.getProperty("com.sun.enterprise.home");
			if (!StringUtils.isEmpty(j2eeHome)) {
				corticeraHome = j2eeHome;
			} else if (!StringUtils.isEmpty(System.getProperty(Globals.DICE_BASE_PROP))) {
				corticeraHome = System.getProperty(Globals.DICE_BASE_PROP);
			}
		}
		
		if (StringUtils.isEmpty(corticeraHome))
			corticeraHome = System.getProperty(Globals.USER_DIR_PROP);
		
		if (!StringUtils.isEmpty(corticeraHome)) {
			File home = new File(corticeraHome);
			if (!home.isAbsolute()) {
				try {
					corticeraHome = home.getCanonicalPath();
				} catch (IOException ex) {
					corticeraHome = home.getAbsolutePath();
				}
			}
			
			System.setProperty(Globals.DICE_HOME_PROP, corticeraHome);
		}
		
		if (StringUtils.isEmpty(System.getProperty(Globals.DICE_BASE_PROP))) {
			System.setProperty(Globals.DICE_BASE_PROP, corticeraHome);
		} else {
			String corticeraBase = System.getProperty(Globals.DICE_BASE_PROP);
			File base = new File(corticeraBase);
			if (!base.isAbsolute()) {
				try {
					corticeraBase = base.getCanonicalPath();
				} catch (IOException e) {
					corticeraBase = base.getAbsolutePath();
				}
			}
			
			System.setProperty(Globals.DICE_BASE_PROP, corticeraBase);
		}
		
		String temp = System.getProperty("java.io.tmpdir");
		File tempDir = new File(temp);
		if (temp == null || (!tempDir.exists()) || (!tempDir.isDirectory())) {
			logger.error("Cannot find specified temporary folder at " + temp);
		}
		
	}

	@Override
	protected void initNaming() {
		
		if (logger.isDebugEnabled())
			logger.debug("Initializing naming...");
		
	}

}
