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
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.corticerasf.dice.utils.ExceptionUtils;

/**
 * Global constants that are applicable to multiple packages within DefaultServerManager.
 * 
 * @author J. Godara
 */
public class Globals {
	
	private static final Logger logger = Logger.getLogger(Globals.class);

	public static final String DICE_HOME_PROP = "dice.home";
	public static final String DICE_BASE_PROP = "dice.base";
	public static final String DICE_CONFIG_PROP = "dice.config";
	public static final String DICE_STARTUP_CLASS = "dice.startup.class";
	public static final String DICE_SERVER_CLASS = "dice.server.class";
	public static final String DICE_SERVER_SERVICE_CLASS = "dice.server.service.class";
	public static final String DICE_SERVER_SERVICE_ENGINE_CLASS = "dice.server.service.container.class";
	public static final String DICE_SERVER_SERCIVE_ENGINE_HOST_CLASS = "dice.server.service.container.host.class";

	public static final String USER_DIR_PROP = "user.dir";

	private static Properties properties = null;

	static {
		loadApplicationProperties();
	}
	
	public static String getProperty(String name) {
		return properties.getProperty(name);
	}

	private static void loadApplicationProperties() {

		InputStream is = null;
		Throwable error = null;
		
		try {
			File home = new File(getDiceBase());
			File conf = new File(home, "conf");
			File props = new File(conf, "server.properties");
			is = new FileInputStream(props);
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
			error = t;
		}

		if (is != null) {
			logger.info("Start reading server configuration.");
			
			try {
				properties = new Properties();
				properties.load(is);
			} catch (Throwable t) {
				ExceptionUtils.handleThrowable(t);
				error = t;
			} finally {
				try {
					is.close();
				} catch (Throwable t) {
					logger.warn("Could not close properties file...", t);
					ExceptionUtils.handleThrowable(t);
				}
			}
		}
		
		if (is == null || error != null) {
			logger.warn("Could not read server properties...", error);
			properties = new Properties();
		}
		
		logger.info("Done reading server configuration.");
		
		Enumeration<?> enumeration = properties.propertyNames();
		while (enumeration.hasMoreElements()) {
			String propName = (String) enumeration.nextElement();
			String value = getProperty(propName);
			if (!StringUtils.isEmpty(value))
				System.setProperty(propName, value);
		}

	}
	
	public static String getDiceBase() {
		return System.getProperty(DICE_BASE_PROP, getCorticeraHome());
	}
	
	public static String getCorticeraHome() {
		return System.getProperty(DICE_HOME_PROP, 
									System.getProperty(USER_DIR_PROP));
	}

}
