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
package org.corticerasf.dice.pipeline;

import java.io.IOException;

import javax.servlet.ServletException;

import org.corticerasf.dice.connector.CorticeraRequest;
import org.corticerasf.dice.connector.CorticeraResponse;
import org.corticerasf.dice.container.Contained;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.lifecycle.LifecycleBase;
import org.corticerasf.dice.lifecycle.LifecycleException;
import org.corticerasf.dice.lifecycle.LifecycleState;

public abstract class ValveBase extends LifecycleBase implements Valve, Contained {
	
	protected final static String info = 
			"org.corticerasf.dice.ValveBase/1.0";
	
	protected Valve next;
	protected Container container;

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public String getInfo() {
		return info;
	}

	public Valve getNext() {
		return next;
	}

	public void setNext(Valve next) {
		this.next = next;
	}
	
	public void backgroundProcess() {
		// NOOP by default
	}

	public abstract void invoke(CorticeraRequest request, CorticeraResponse response)
			throws IOException, ServletException;

	public void event(CorticeraRequest request, CorticeraResponse response,
			PipelineEvent event) throws IOException, ServletException {
		getNext().event(request, response, event);
	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		setState(LifecycleState.STARTING);
	}

	@Override
	protected synchronized void stopInternal() throws LifecycleException {
		setState(LifecycleState.STOPPING);
	}
}
