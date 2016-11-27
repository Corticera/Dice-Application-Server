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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;
import org.corticerasf.dice.Loader;
import org.corticerasf.dice.container.Container;
import org.corticerasf.dice.container.ContainerEvent;
import org.corticerasf.dice.container.ContainerListener;
import org.corticerasf.dice.lifecycle.Lifecycle;
import org.corticerasf.dice.lifecycle.LifecycleBase;
import org.corticerasf.dice.lifecycle.LifecycleException;
import org.corticerasf.dice.lifecycle.LifecycleState;
import org.corticerasf.dice.pipeline.Pipeline;
import org.corticerasf.dice.pipeline.StandardPipeline;

public class ContainerBase extends LifecycleBase implements Container {

	private static final Logger logger = Logger.getLogger(ContainerBase.class);

	protected String name = "ContainerBase/v1.0";
	private Container parent = null;
	private Map<String, Container> children = new HashMap<String, Container>();
	private boolean startChildren = true;
	private List<ContainerListener> listeners = new ArrayList<ContainerListener>();
	private Loader loader = null;

	protected ThreadPoolExecutor startStopExecutor;
	private int startStopThreads = 1;
	private volatile boolean threadDone = false;
	
	protected Pipeline pipeline = new StandardPipeline(this);

	private Thread daemon;

	protected int backgroundProcessorDelay = -1;

	public String getInfo() {
		return this.getClass().getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Container getParent() {
		return parent;
	}

	public void setParent(Container parent) {
		this.parent = parent;
	}

	public void backgroundProcess() {
		fireLifecycleEvent(PERIODIC_EVENT, null);
	}

	public void addChild(Container child) {

		if (logger.isDebugEnabled())
			logger.debug("Adding child " + child + " to " + this);

		synchronized (children) {
			if (getChild(child.getName()) != null)
				throw new IllegalArgumentException("addChild: '"
						+ child.getInfo() + "' is already a child of '"
						+ getInfo() + "'.");

			child.setParent(this);
			children.put(child.getName(), child);
		}

		// Start the child.
		try {
			if (getState().isAvailable()
					|| getState().equals(LifecycleState.STARTING_PREP)
					&& startChildren) {
				child.start();
			}
		} catch (LifecycleException ex) {
			logger.error("Cannot start child container.", ex);
			throw new IllegalArgumentException("Cannot start child container.",
					ex);
		} finally {
			fireContainerEvent(ADD_CHILD_EVENT, child);
		}

	}

	public Container getChild(String name) {
		return children.get(name);
	}

	public void removeChild(Container child) {
		if (child == null)
			return;

		try {
			if (child.getState().isAvailable()) {
				child.stop();
			}
		} catch (LifecycleException ex) {
			logger.error("Cannot stop container '" + child.getInfo() + "'.", ex);
		}

		try {
			if (!child.getState().equals(LifecycleState.DESTROYING)) {
				child.destroy();
			}
		} catch (LifecycleException e) {
			logger.error("Cannot destroy child '" + child.getInfo() + "'.", e);
		}

		synchronized (children) {
			if (children.get(child.getName()) == null)
				return;
			children.remove(child.getName());
		}

		fireContainerEvent(REMOVE_CHILD_EVENT, child);

	}

	public Container[] findChildren() {
		Container[] result = new Container[children.size()];
		Iterator<Container> it = children.values().iterator();
		int i = 0;
		while (it.hasNext()) {
			Object next = it.next();
			if (next instanceof Container) {
				result[i++] = (Container) next;
			}
		}
		return result;
	}

	public void addContainerListener(ContainerListener listener) {
		listeners.add(listener);
	}

	public void removeContainerListener(ContainerListener listener) {
		listeners.remove(listener);
	}

	public ContainerListener[] findContainerListeners() {
		ContainerListener[] result = new ContainerListener[listeners.size()];
		return listeners.toArray(result);
	}

	public void fireContainerEvent(String type, Object data) {

		if (listeners.size() < 1)
			return;

		ContainerEvent event = new ContainerEvent(this, type, data);
		for (ContainerListener listener : listeners) {
			listener.containerEvent(event);
		}

	}

	@Override
	protected void initInternal() throws LifecycleException {		
		BlockingQueue<Runnable> startStopQueue = new LinkedBlockingDeque<Runnable>();

		startStopExecutor = new ThreadPoolExecutor(getStartStopThreads(),
				getStartStopThreads(), 10, TimeUnit.SECONDS, startStopQueue,
				new StartStopThreadFactory(getName() + "-start-stop-"));
	}

	@Override
	protected void startInternal() throws LifecycleException {

		// Start the child containers
		Container[] children = findChildren();
		List<Future<Void>> results = new ArrayList<Future<Void>>();
		for (Container child : children) {
			results.add(startStopExecutor.submit(new ChildProcessor(child)));
		}

		boolean fail = false;
		for (Future<Void> result : results) {
			try {
				result.get();
			} catch (Exception e) {
				logger.error("Container thread start failed.", e);
				fail = true;
			}

		}
		if (fail) {
			throw new LifecycleException("Container thread start failed.");
		}

		setState(LifecycleState.STARTING);

		startThread();
	}

	@Override
	protected void stopInternal() throws LifecycleException {
		stopThread();

		setState(LifecycleState.STOPPING);
	}

	@Override
	protected void destroyInternal() throws LifecycleException {
		if (startStopExecutor != null)
			startStopExecutor.shutdown();
	}

	public void setStartStopThreads(int threads) {
		startStopThreads = threads;

		ThreadPoolExecutor executor = startStopExecutor;
		if (executor != null) {
			int newThreads = getStartStopThreads();
			executor.setMaximumPoolSize(newThreads);
			executor.setCorePoolSize(newThreads);
		}
	}

	public void setBackgroungProcessorDelay(int backgroundProcessorDelay) {
		this.backgroundProcessorDelay = backgroundProcessorDelay;
	}

	public int getBackgroundProcessorDelay() {
		return backgroundProcessorDelay;
	}

	private int getStartStopThreads() {
		int result = startStopThreads;

		if (result > 0)
			return result;

		result = Runtime.getRuntime().availableProcessors() + result;
		if (result < 1)
			result = 1;

		return result;
	}

	private void startThread() {
		if (daemon != null)
			return;

		if (backgroundProcessorDelay <= 0)
			return;

		threadDone = false;
		String threadName = "ContainerBackgroundProcessor[" + getInfo() + "]";
		daemon = new Thread(new ContainerBackgroundProcessor(), threadName);
		daemon.setDaemon(true);
		daemon.start();
	}

	private void stopThread() {
		if (daemon == null)
			return;

		threadDone = true;
		daemon.interrupt();
		try {
			daemon.join();
		} catch (InterruptedException ex) {
		}

		daemon = null;
	}

	private static class StartStopThreadFactory implements ThreadFactory {

		private ThreadGroup group = null;
		private AtomicInteger counter = new AtomicInteger(1);
		private String namePrefix;

		public StartStopThreadFactory(String namePrefix) {
			SecurityManager securityManager = System.getSecurityManager();
			group = (securityManager != null ? securityManager.getThreadGroup()
					: Thread.currentThread().getThreadGroup());
			this.namePrefix = namePrefix;
		}

		public Thread newThread(Runnable r) {
			Thread thread = new Thread(group, r, namePrefix
					+ counter.getAndIncrement());
			thread.setDaemon(true);
			return thread;
		}

	}

	protected class ChildProcessor implements Callable<Void> {

		Container child = null;

		public ChildProcessor(Container child) {
			this.child = child;
		}

		public Void call() throws Exception {
			child.start();
			return null;
		}
	}

	protected class ContainerBackgroundProcessor implements Runnable {

		public void run() {
			Throwable t = null;
			String threadDeathMessage = "Thread "
					+ Thread.currentThread().getName() + " died unexpectedly!";
			try {
				while (!threadDone) {
					try {
						Thread.sleep(backgroundProcessorDelay * 1000L);
					} catch (InterruptedException ex) {
					}

					if (!threadDone)
						processChildren(getObject());
				}
			} catch (RuntimeException ex) {
				t = ex;
				throw ex;
			} catch (Error e) {
				t = e;
				throw e;
			} finally {
				if (!threadDone)
					logger.error(threadDeathMessage, t);
			}
		}

		protected void processChildren(Container container) {
			for (Container child : container.findChildren()) {
				child.backgroundProcess();
			}
		}

	}

	public Container getObject() {
		return this;
	}

	public Loader getLoader() {
		if (loader != null)
			return loader;
		if (parent.getLoader() != null)
			return parent.getLoader();
		
		return null;
	}

	public synchronized void setLoader(Loader loader) {

		if (this.loader == loader)
			return;
		
		if (getState().isAvailable() && this.loader != null 
				&& this.loader instanceof Lifecycle) {
			try {
				((Lifecycle) this.loader).stop();
			} catch (LifecycleException ex) {
				logger.error("Cannot stop loader.", ex);
			}
		}
		
		if (getState().isAvailable() && loader != null
				&& loader instanceof Lifecycle) {
			try {
				((Lifecycle) loader).start();
			} catch (LifecycleException ex) {
				logger.error("Cannot start loader.", ex);
			}
		}
		
		this.loader = loader;
		
	}
	
	@Override
	public String toString() {
		return getInfo();
	}

	public void setPipleline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

}
