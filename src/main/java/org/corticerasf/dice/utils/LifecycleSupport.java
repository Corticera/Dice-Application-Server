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
package org.corticerasf.dice.utils;

import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.lifecycle.LifecycleEvent;
import org.corticerasf.dice.lifecycle.LifecycleListener;

public final class LifecycleSupport {
	
	public LifecycleSupport(Lifecycle lifecycle) {
		super();
		this.lifecycle = lifecycle;
	}
	
	private Lifecycle lifecycle;
	
	private LifecycleListener[] listeners = new LifecycleListener[0];
	
	private final Object lockingObject = new Object();
	
	public void addLifecycleListener(LifecycleListener listener) {
		
		synchronized (lockingObject) {
			
			LifecycleListener[] results = new LifecycleListener[listeners.length + 1];
			
			for (int i = 0 ; i < listeners.length ; i++) 
				results[i] = listeners[i];
			
			results[listeners.length] = listener;
			listeners = results;
		}
		
	}
	
	public LifecycleListener[] findLifecycleListeners() {
		return listeners;
	}
	
	public void fireLifecycleEvent(String type, Object data) {
		
		LifecycleEvent event = new LifecycleEvent(lifecycle, type, data);
		LifecycleListener[] events = listeners;
		for (LifecycleListener listener : events)
			listener.lifecycleEvent(event);
		
	}
	
	public void removeLifecycleListener(LifecycleListener listener) {
		
		synchronized (lockingObject) {
            int n = -1;
            for (int i = 0; i < listeners.length; i++) {
                if (listeners[i] == listener) {
                    n = i;
                    break;
                }
            }
            if (n < 0)
                return;
            LifecycleListener results[] =
              new LifecycleListener[listeners.length - 1];
            int j = 0;
            for (int i = 0; i < listeners.length; i++) {
                if (i != n)
                    results[j++] = listeners[i];
            }
            listeners = results;
        }
		
	}

}
