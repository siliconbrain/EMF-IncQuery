/*******************************************************************************
 * Copyright (c) 2004-2008 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.network;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.remote.Address;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.tuple.Tuple;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.util.Options;


/**
 * @author Gabor Bergmann
 * 
 */
public class Network {
	int threads;
	
	protected ArrayList<ReteContainer> containers;
	ReteContainer headContainer;
	private int firstContainer = 0;	
	private int nextContainer = 0;

	// the following fields exist only if threads > 0
	protected Map<ReteContainer, Long> globalTerminationCriteria = null; 
	protected Map<ReteContainer, Long> reportedClocks = null;
	protected Lock updateLock = null; // grab during normal update operations
	protected Lock structuralChangeLock = null; // grab if the network structure is to
											// be changed

	/**
	 * @param threads the number of threads to operate the network with; 
	 * 	0 means single-threaded operation, 
	 * 	1 starts an asynchronous thread to operate the RETE net, 
	 * 	>1 uses multiple RETE containers.
	 */
	public Network(int threads) {
		super();
		this.threads = threads;
		
		containers = new ArrayList<ReteContainer>();
		firstContainer = (threads > 1) ? Options.firstFreeContainer : 0;
		nextContainer = firstContainer;

		if (threads > 0) {
			globalTerminationCriteria = new HashMap<ReteContainer, Long>();
			reportedClocks = new HashMap<ReteContainer, Long>();
			ReadWriteLock rwl = new ReentrantReadWriteLock();
			updateLock = rwl.readLock();
			structuralChangeLock = rwl.writeLock();
			for (int i = 0; i < threads; ++i) containers.add(new ReteContainer(this, true));					
		} else 
			containers.add(new ReteContainer(this, false));

		headContainer = containers.get(0);
	}

	/**
	 * Kills this Network along with all containers and message consumption
	 * cycles.
	 */
	public void kill() {
		for (ReteContainer container : containers) {
			container.kill();
		}
		containers.clear();
	}

	/**
	 * Returns the head container, that is guaranteed to reside in the same JVM
	 * as the Network object.
	 * 
	 * @return
	 */
	public ReteContainer getHeadContainer() {
		return headContainer; 
	}

	/**
	 * Returns the next container in round-robin fashion. Configurable not to
	 * yield head container.
	 */
	public ReteContainer getNextContainer() {
		if (nextContainer >= containers.size())
			nextContainer = firstContainer;
		return containers.get(nextContainer++);
	}

	/**
	 * Internal message delivery method.
	 * @pre threads > 0	 
	 */
	private void sendUpdate(Address<? extends Receiver> receiver,
			Direction direction, Tuple updateElement) {
		ReteContainer affectedContainer = receiver.getContainer();
		synchronized (globalTerminationCriteria) {
			long newCriterion = affectedContainer.sendUpdateToLocalAddress(
					receiver, direction, updateElement);
			terminationCriterion(affectedContainer, newCriterion);
		}
	}
	
	/**
	 * Internal message delivery method for single-threaded operation
	 * @pre threads == 0	 
	 */
	private void sendUpdateSingleThreaded(Address<? extends Receiver> receiver,
			Direction direction, Tuple updateElement) {
		ReteContainer affectedContainer = receiver.getContainer();
		affectedContainer.sendUpdateToLocalAddressSingleThreaded(receiver, direction, updateElement);
	}

	/**
	 * Internal message delivery method.
	 * @pre threads > 0	  
	 */
	private void sendUpdates(Address<? extends Receiver> receiver,
			Direction direction, Collection<Tuple> updateElements) {
		if (updateElements.isEmpty())
			return;
		ReteContainer affectedContainer = receiver.getContainer();
		synchronized (globalTerminationCriteria) {
			long newCriterion = affectedContainer.sendUpdatesToLocalAddress(
					receiver, direction, updateElements);
			terminationCriterion(affectedContainer, newCriterion);
		}
	}

	/**
	 * Sends an update message to the receiver node, indicating a newly found or
	 * lost partial matching. The node may reside in any of the containers
	 * associated with this network. To be called from a user thread during
	 * normal operation, NOT during construction.
	 * 
	 * @return the value of the target container's clock at the time when the
	 *         message was accepted into its message queue
	 */
	public void sendExternalUpdate(Address<? extends Receiver> receiver,
			Direction direction, Tuple updateElement) {
		if (threads > 0)
		{
			updateLock.lock();
			sendUpdate(receiver, direction, updateElement);
			updateLock.unlock();
		}
		else {
			sendUpdateSingleThreaded(receiver, direction, updateElement);
			//getHeadContainer().
		}
	}

	/**
	 * Sends an update message to the receiver node, indicating a newly found or
	 * lost partial matching. The node may reside in any of the containers
	 * associated with this network. To be called from a user thread during
	 * construction.
	 * 
	 * @pre: structuralChangeLock MUST be grabbed by the sequence 
	 * (but not necessarily this thread, as the sequence may span through network 
	 * calls, that's why it's not enforced here )
	 * 
	 * @return the value of the target container's clock at the time when the
	 *         message was accepted into its message queue
	 */
	public void sendConstructionUpdate(Address<? extends Receiver> receiver,
			Direction direction, Tuple updateElement) {
		// structuralChangeLock.lock();
		if (threads > 0) 
			sendUpdate(receiver, direction, updateElement);
		else
			receiver.getContainer().sendUpdateToLocalAddressSingleThreaded(receiver, direction, updateElement);
		// structuralChangeLock.unlock();
	}

	/**
	 * Sends multiple update messages atomically to the receiver node,
	 * indicating a newly found or lost partial matching. The node may reside in
	 * any of the containers associated with this network. To be called from a
	 * user thread during construction.
	 * 
	 * @pre: structuralChangeLock MUST be grabbed by the sequence 
	 * (but not necessarily this thread, as the sequence may span through network 
	 * calls, that's why it's not enforced here )
	 * 
	 * @return the value of the target container's clock at the time when the
	 *         message was accepted into its message queue
	 */
	public void sendConstructionUpdates(Address<? extends Receiver> receiver,
			Direction direction, Collection<Tuple> updateElements) {
		// structuralChangeLock.lock();
		if (threads > 0)
			sendUpdates(receiver, direction, updateElements);
		else
			receiver.getContainer().sendUpdatesToLocalAddressSingleThreaded(receiver, direction, updateElements);	
		// structuralChangeLock.unlock();
	}

	/**
	 * Establishes connection between a supplier and a receiver node, regardless
	 * which container they are in. Not to be called remotely, because this
	 * method enforces the structural lock.
	 * 
	 * @param supplier
	 * @param receiver
	 * @param synchronise
	 *            indicates whether the receiver should be synchronised to the
	 *            current contents of the supplier
	 */
	public void connectRemoteNodes(Address<? extends Supplier> supplier,
			Address<? extends Receiver> receiver, boolean synchronise) {
		if (threads > 0) structuralChangeLock.lock();
		receiver.getContainer().connectRemoteNodes(supplier, receiver, synchronise);
		if (threads > 0) structuralChangeLock.unlock();
	}

	/**
	 * Severs connection between a supplier and a receiver node, regardless
	 * which container they are in. Not to be called remotely, because this
	 * method enforces the structural lock.
	 * 
	 * @param supplier
	 * @param receiver
	 * @param desynchronise
	 *            indicates whether the current contents of the supplier should
	 *            be subtracted from the receiver
	 */
	public void disconnectRemoteNodes(Address<? extends Supplier> supplier,
			Address<? extends Receiver> receiver, boolean desynchronise) {
		if (threads > 0) structuralChangeLock.lock();
		receiver.getContainer().disconnectRemoteNodes(supplier, receiver, desynchronise);
		if (threads > 0) structuralChangeLock.unlock();
	}

	/**
	 * Containers use this method to report whenever they run out of messages in
	 * their queue.
	 * 
	 * To be called from the thread of the reporting container.
	 * 
	 * @pre threads > 0.
	 * @param reportingContainer
	 *            the container reporting the emptiness of its message queue.
	 * @param clock
	 *            the value of the container's clock when reporting.
	 * @param localTerminationCriteria
	 *            the latest clock values this container has received from other
	 *            containers since the last time it reported termination.
	 */
	void reportLocalUpdateTermination(ReteContainer reportingContainer,
			long clock, Map<ReteContainer, Long> localTerminationCriteria) {
		synchronized (globalTerminationCriteria) {
			for (ReteContainer affectedContainer : localTerminationCriteria
					.keySet()) {
				long newCriterion = localTerminationCriteria
						.get(affectedContainer);

				terminationCriterion(affectedContainer, newCriterion);
			}

			reportedClocks.put(reportingContainer, clock);
			Long criterion = globalTerminationCriteria.get(reportingContainer);
			if (criterion != null && criterion < clock)
				globalTerminationCriteria.remove(reportingContainer);

			if (globalTerminationCriteria.isEmpty())
				globalTerminationCriteria.notifyAll();
		}
	}

	/**
	 * @pre threads > 0
	 */
	private void terminationCriterion(ReteContainer affectedContainer,
			long newCriterion) {
		synchronized (globalTerminationCriteria) {
			Long oldCriterion = globalTerminationCriteria
					.get(affectedContainer);
			Long oldClock = reportedClocks.get(affectedContainer);
			long relevantClock = oldClock == null ? 0 : oldClock;
			if ((relevantClock <= newCriterion)
					&& (oldCriterion == null || oldCriterion < newCriterion)) {
				globalTerminationCriteria.put(affectedContainer, newCriterion);
			}
		}
	}

	/**
	 * Waits until all rete update operations are settled in all containers.
	 * Returns immediately, if no updates are pending.
	 * 
	 * To be called from any user thread.
	 */
	public void waitForReteTermination() {
		if (threads > 0)
		{
			synchronized (globalTerminationCriteria) {
				while (!globalTerminationCriteria.isEmpty()) {
					try {
						globalTerminationCriteria.wait();
					} catch (InterruptedException e) {
	
					}
				}
			}
		}
		else headContainer.messageConsumptionSingleThreaded();
	}

	/**
	 * Waits to execute action until all rete update operations are settled in
	 * all containers. Runs action and returns immediately, if no updates are
	 * pending. The given action is guaranteed to be run when the terminated
	 * state still persists.
	 * 
	 * @param action
	 *            the action to be run when reaching the steady-state.
	 * 
	 *            To be called from any user thread.
	 */
	public void waitForReteTermination(Runnable action) {
		if (threads > 0)
		{
			synchronized (globalTerminationCriteria) {
				while (!globalTerminationCriteria.isEmpty()) {
					try {
						globalTerminationCriteria.wait();
					} catch (InterruptedException e) {
	
					}
				}
				action.run();
			}
		}
		else {
			headContainer.messageConsumptionSingleThreaded();
			action.run();			
		}

	}

	/**
	 * @return the structuralChangeLock
	 */
	public Lock getStructuralChangeLock() {
		return structuralChangeLock;
	}

}
