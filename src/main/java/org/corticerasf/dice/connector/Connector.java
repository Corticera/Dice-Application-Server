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
package org.corticerasf.dice.connector;

import org.apache.log4j.Logger;
import org.corticerasf.dice.Service;
import org.corticerasf.dice.lifecycle.LifecycleBase;
import org.corticerasf.dice.lifecycle.LifecycleException;
import org.corticerasf.dice.protocols.ProtocolHandler;

public class Connector extends LifecycleBase {

	private static final Logger logger = Logger.getLogger(Connector.class);

	public Connector() {
		this(null);
	}

	public Connector(String protocol) {
		// TODO Add multiple implementations of ProtocolHandler based on the
		// protocol. E.g: Protocol HTTP/1.1 would be handled by some
		// implementation while the other protocol is handled by other
		// implementation.
		try {
			Class<?> clazz = Class.forName(protocolHandlerClassName);
			protocolHandler = (ProtocolHandler) clazz.newInstance();
		} catch (Exception e) {
			logger.error("Cannot instantiate protocol handler.", e);
		}
	}

	protected Service service = null;

	private ProtocolHandler protocolHandler = null;

	private String protocolHandlerClassName = "org.corticerasf.dice.protocols.http11.HTTPProtocolHandler";

	@Override
	protected void initInternal() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void startInternal() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stopInternal() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		// TODO Auto-generated method stub

	}

	public void pause() {

	}

	public Service getService() {
		return service;
	}

	public void setService(Service service) {
		this.service = service;
	}

	public String getProtocolHandlerClassName() {
		return protocolHandlerClassName;
	}

	public void setProtocolHandlerClassName(String protocolHandlerClassName) {
		this.protocolHandlerClassName = protocolHandlerClassName;
	}

}
