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

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.corticerasf.dice.container.Contained;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.lifecycle.LifecycleBase;
import org.corticerasf.dice.lifecycle.LifecycleException;
import org.corticerasf.dice.lifecycle.LifecycleState;
import org.corticerasf.dice.utils.ExceptionUtils;

public class StandardPipeline extends LifecycleBase implements Pipeline, Contained {
	
	private static final Logger logger = Logger.getLogger(StandardPipeline.class);
	
	protected Container container;
	protected Valve basic;
	protected Valve first;
	
	public StandardPipeline(Container container) {
		setContainer(container);
	}

	public Container getContainer() {
		return container;
	}

	public void setContainer(Container container) {
		this.container = container;
	}

	public Valve getBasic() {
		return basic;
	}

	public void setBasic(Valve valve) {
		Valve oldBasic = basic;
		if (oldBasic == valve)
			return;
		
		if (oldBasic != null) {
			if (getState().isAvailable() && (oldBasic instanceof Lifecycle)) {
				try {
					((Lifecycle) oldBasic).stop();
				} catch (LifecycleException ex) {
					logger.error("Cannot stop old valve.", ex);
				}
			}
			if (oldBasic instanceof Contained) {
				try {
					((Contained) oldBasic).setContainer(null);
				} catch (Throwable t) {
					ExceptionUtils.handleThrowable(t);
				}
			}
		}
		
		if (valve == null) return;
		
		if (valve instanceof Contained)
			((Contained) valve).setContainer(container);
		
		if (getState().isAvailable() && (valve instanceof Lifecycle)) {
			try {
				((Lifecycle) valve).start();
			} catch (LifecycleException ex) {
				logger.error("Cannot start new valve.", ex);
				return;
			}
		}
		
		Valve current = first;
		while (current != null) {
			if (current.getNext() == oldBasic) {
				current.setNext(valve);
				break;
			}
			current = current.getNext();
		}
		
		this.basic = valve;
	}

	public void addValve(Valve valve) {
		if (valve instanceof Contained) 
			((Contained) valve).setContainer(container);
		
		if (getState().isAvailable()) {
			if (valve instanceof Lifecycle) {
				try {
					((Lifecycle) valve).start();
				} catch (LifecycleException ex) {
					logger.error("Error starting valve.", ex);
				}
			}
		}
		
		if (first == null) {
			first = valve;
			valve.setNext(basic);
		} else {
			Valve current = first;
			while (current != null) {
				if (current.getNext() == basic) {
					current.setNext(valve);
					valve.setNext(basic);
					return;
				}
				
				current = current.getNext();
			}
		}
		
		container.fireContainerEvent(Container.ADD_VALVE_EVENT, valve);
	}

	public Valve[] getValves() {
		List<Valve> valves = new ArrayList<Valve>();
		
		Valve current = first;
		if (current == null)
			current = basic;
		
		while (current != null) {
			valves.add(current);
			current = current.getNext();
		}
		
		return valves.toArray(new Valve[0]);
	}

	public void removeValve(Valve valve) {

		Valve current = null;
		if (first == valve) {
			first = first.getNext();
		} else {
			current = first;
		}
		
		while (current != null) {
			if (current.getNext() == valve) {
				current.setNext(valve.getNext());
				break;
			}
			
			current = current.getNext();
		}
		
		if (first == basic)
			first = null;
		
		if (valve instanceof Contained)
			((Contained) valve).setContainer(null);
		
		if (valve instanceof Lifecycle) {
			if (getState().isAvailable()) {
				try {
					((Lifecycle) valve).stop();
				} catch (LifecycleException ex) {
					logger.error("Cannot stop valve.", ex);
				}
			}
			try {
				((Lifecycle) valve).destroy();
			} catch (LifecycleException ex) {
				logger.error("Cannot destroy valve.", ex);
			}
		}
		
		container.fireContainerEvent(Container.REMOVE_VALVE_EVENT, valve);
		
	}

	@Override
	protected void initInternal() throws LifecycleException {
		// NOOP
	}

	@Override
	protected synchronized void startInternal() throws LifecycleException {
		Valve current = first;
		if (current == null) {
			current = basic;
		}
		
		while (current != null) {
			if (current.getNext() instanceof Lifecycle)
				((Lifecycle) current.getNext()).start();
			current = current.getNext();
		}
		
		setState(LifecycleState.STARTING);
	}

	@Override
	protected synchronized void stopInternal() throws LifecycleException {
		Valve current = first;
		if (current == null) {
			current = basic;
		}
		
		while (current != null) {
			if (current.getNext() instanceof Lifecycle)
				((Lifecycle) current.getNext()).stop();
			current = current.getNext();
		}
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		for (Valve valve : getValves()) {
			removeValve(valve);
		}
	}

	public Valve getFirst() {
		if (first != null)
			return first;
		
		return basic;
	}

}
