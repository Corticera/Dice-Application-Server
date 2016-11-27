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

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.corticerasf.dice.Engine;
import org.corticerasf.dice.Service;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.container.ContainerBase;
import org.corticerasf.dice.lifecycle.LifecycleException;

public class StandardEngine extends ContainerBase implements Engine {

	private static final Logger logger = Logger.getLogger(StandardEngine.class);
	
	public StandardEngine() {
		super();

		backgroundProcessorDelay = 10;
	}

	private String defaultHost;
	private Service service;
	private String info = this.getClass().getName() + "/1.0";

	public void setDefaultHost(String defaultHost) {
		if (StringUtils.isEmpty(defaultHost)) {
			this.defaultHost = null;
		} else {
			this.defaultHost = defaultHost.toLowerCase(Locale.ENGLISH);
		}
	}

	public String getDefaultHost() {
		return defaultHost;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public Service getService() {
		return service;
	}

	@Override
	public String getInfo() {
		return info;
	}

	/**
	 * Disallow any attempt to set a parent because engine is always top in the
	 * container hierarchy.
	 */
	@Override
	public void setParent(Container parent) {
		throw new IllegalArgumentException(
				"This container cannot have a parent.");
	}
	
	@Override
	protected synchronized void startInternal() throws LifecycleException {
		
		logger.info("Starting Servlet Engine: Corticera v1.0");
		
		super.startInternal();
	}
	
	@Override
	public String toString() {
		return "StandardEngine[" + getName() + "]";
	}

}
