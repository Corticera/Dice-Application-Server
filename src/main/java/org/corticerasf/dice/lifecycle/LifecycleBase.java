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
package org.corticerasf.dice.lifecycle;

import org.apache.log4j.Logger;
import org.corticerasf.dice.utils.ExceptionUtils;
import org.corticerasf.dice.utils.LifecycleSupport;

public abstract class LifecycleBase implements Lifecycle {
	
	private static final Logger logger = Logger.getLogger(LifecycleBase.class);

	private LifecycleSupport lifecycle = new LifecycleSupport(this);
	
	private volatile LifecycleState state = LifecycleState.NEW;
	
	public void addLifecycleListener(LifecycleListener listener) {
		lifecycle.addLifecycleListener(listener);
	}
	
	public LifecycleListener[] findLifecycleListeners() {
		return lifecycle.findLifecycleListeners();
	}
	
	public void removeLifecycleListener(LifecycleListener listener) {
		lifecycle.removeLifecycleListener(listener);		
	}
	
	public void fireLifecycleEvent(String type, Object data) {
		lifecycle.fireLifecycleEvent(type, data);;
	}
	
	public final synchronized void init() throws LifecycleException {
		if (!state.equals(LifecycleState.NEW)) {
			invalidTransition(BEFORE_INIT_EVENT);
		}
		
		try {
			setStateInternal(LifecycleState.INITIALIZING, null, false);
			initInternal();
			setStateInternal(LifecycleState.INITIALIZED, null, false);
		} catch (LifecycleException ex) {
			ExceptionUtils.handleThrowable(ex);
			throw new LifecycleException("Lifecycle initialization failed!!!", ex);
		}
		
	}
	
	protected abstract void initInternal() throws LifecycleException;
	
	public final synchronized void start() throws LifecycleException {
		
		if (LifecycleState.STARTING_PREP.equals(state) || LifecycleState.STARTING.equals(state)
				|| LifecycleState.STARTED.equals(state)) {
			
			Exception e = new LifecycleException();
			logger.error("The lifecycle has already started.", e);
			return;
			
		}
		
		if (LifecycleState.NEW.equals(state)) {
			init();
		} else if (LifecycleState.FAILED.equals(state)) {
			stop();
		} else if (!LifecycleState.INITIALIZED.equals(state)
				&& !LifecycleState.STOPPED.equals(state)) {
			invalidTransition(BEFORE_START_EVENT);
		}
		
		try {
			setStateInternal(LifecycleState.STARTING_PREP, null, false);
			startInternal();
			// This failure would be controlled. The component has set itself to
			// failed to call stop for cleanup purpose.
			if (LifecycleState.FAILED.equals(state)) {
				stop();
			} else if (!LifecycleState.STARTING.equals(state)) {
				invalidTransition(AFTER_START_EVENT);
			} else {
				setStateInternal(LifecycleState.STARTED, null, false);
			}
		} catch (Throwable t) {
			ExceptionUtils.handleThrowable(t);
			setStateInternal(LifecycleState.FAILED, null, false);
			throw new LifecycleException("The lifecycle failed to start.", t);
		}
		
	}
	
	protected abstract void startInternal() throws LifecycleException;
	
	public final synchronized void stop() throws LifecycleException {

        if (LifecycleState.STOPPING_PREP.equals(state) || LifecycleState.STOPPING.equals(state) ||
                LifecycleState.STOPPED.equals(state)) {

        	Exception e = new LifecycleException();
            logger.error("The lifecycle has already stopped.", e);
            return;
        }

        if (state.equals(LifecycleState.NEW)) {
            state = LifecycleState.STOPPED;
            return;
        }

        if (!state.equals(LifecycleState.STARTED) && !state.equals(LifecycleState.FAILED)) {
            invalidTransition(Lifecycle.BEFORE_STOP_EVENT);
        }

        try {
            if (state.equals(LifecycleState.FAILED)) {
                // Don't transition to STOPPING_PREP as that would briefly mark the
                // component as available but do ensure the BEFORE_STOP_EVENT is
                // fired
                fireLifecycleEvent(BEFORE_STOP_EVENT, null);
            } else {
                setStateInternal(LifecycleState.STOPPING_PREP, null, false);
            }

            stopInternal();

            // Shouldn't be necessary but acts as a check that sub-classes are
            // doing what they are supposed to.
            if (!state.equals(LifecycleState.STOPPING) && !state.equals(LifecycleState.FAILED)) {
                invalidTransition(Lifecycle.AFTER_STOP_EVENT);
            }

            setStateInternal(LifecycleState.STOPPED, null, false);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            setStateInternal(LifecycleState.FAILED, null, false);
            throw new LifecycleException("Failed to stop the lifecycle!", t);
        } finally {
            if (this instanceof Lifecycle.SingleUse) {
                // Complete stop process first
                setStateInternal(LifecycleState.STOPPED, null, false);
                destroy();
            }
        }
    }
	
	protected abstract void stopInternal() throws LifecycleException;
	
	public final synchronized void destroy() throws LifecycleException {
        if (LifecycleState.FAILED.equals(state)) {
            try {
                // Triggers clean-up
                stop();
            } catch (LifecycleException e) {
                // Just log. Still want to destroy.
            	logger.warn("Failed to destroy the lifecycle!", e);
            }
        }

        if (LifecycleState.DESTROYING.equals(state) ||
                LifecycleState.DESTROYED.equals(state)) {

            if (logger.isDebugEnabled()) {
                Exception e = new LifecycleException();
                logger.debug("The lifecycle has already been destroyed.", e);
            } else if (logger.isInfoEnabled() && !(this instanceof Lifecycle.SingleUse)) {
                // Rather than have every component that might need to call
                // destroy() check for SingleUse, don't log an info message if
                // multiple calls are made to destroy()
            	logger.info("The lifecycle has already been destroyed.");
            }

            return;
        }

        if (!state.equals(LifecycleState.STOPPED) &&
                !state.equals(LifecycleState.FAILED) &&
                !state.equals(LifecycleState.NEW) &&
                !state.equals(LifecycleState.INITIALIZED)) {
            invalidTransition(Lifecycle.BEFORE_DESTROY_EVENT);
        }

        try {
            setStateInternal(LifecycleState.DESTROYING, null, false);
            destroyInternal();
            setStateInternal(LifecycleState.DESTROYED, null, false);
        } catch (Throwable t) {
            ExceptionUtils.handleThrowable(t);
            setStateInternal(LifecycleState.FAILED, null, false);
            throw new LifecycleException("Failed to destroy the lifecycle!", t);
        }
    }


    protected abstract void destroyInternal() throws LifecycleException;

    public LifecycleState getState() {
        return state;
    }

    public String getStateName() {
        return getState().toString();
    }


    /**
     * Provides a mechanism for sub-classes to update the component state.
     * Calling this method will automatically fire any associated
     * {@link Lifecycle} event. It will also check that any attempted state
     * transition is valid for a sub-class.
     *
     * @param state The new state for this component
     */
    protected synchronized void setState(LifecycleState state)
            throws LifecycleException {
        setStateInternal(state, null, true);
    }


    /**
     * Provides a mechanism for sub-classes to update the component state.
     * Calling this method will automatically fire any associated
     * {@link Lifecycle} event. It will also check that any attempted state
     * transition is valid for a sub-class.
     *
     * @param state The new state for this component
     * @param data  The data to pass to the associated {@link Lifecycle} event
     */
    protected synchronized void setState(LifecycleState state, Object data)
            throws LifecycleException {
        setStateInternal(state, data, true);
    }
	
	private void setStateInternal(LifecycleState state, Object data, boolean check) throws LifecycleException {
		
		if (check) {
			
			if (state == null) {
				invalidTransition("null");
				return;
			}
			
			if (!(state == LifecycleState.FAILED ||
                    (this.state == LifecycleState.STARTING_PREP &&
                            state == LifecycleState.STARTING) ||
                    (this.state == LifecycleState.STOPPING_PREP &&
                            state == LifecycleState.STOPPING) ||
                    (this.state == LifecycleState.FAILED &&
                            state == LifecycleState.STOPPING))) {
                // No other transition permitted
                invalidTransition(state.name());
			}
			
		}

        this.state = state;
        String lifecycleEvent = state.getLifecycleEvent();
        if (lifecycleEvent != null) {
            fireLifecycleEvent(lifecycleEvent, data);
        }
		
	}
	
	private void invalidTransition(String state) throws LifecycleException {
		throw new LifecycleException("The lifecycle transitioned to invalid state " + state);
	}
	
}
