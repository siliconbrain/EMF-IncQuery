/*******************************************************************************
 * Copyright (c) 2010-2012, Tamas Szabo, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Szabo, Abel Hegedus - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.runtime.evm.notification;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.evm.api.Activation;
import org.eclipse.incquery.runtime.evm.api.ActivationLifeCycleEvent;
import org.eclipse.incquery.runtime.evm.api.ActivationState;

/**
 * Classes implement this interface to provide notifications about the changes in the collection of activations within
 * the AbstractRule Engine.
 * 
 * @author Tamas Szabo
 * 
 */
public abstract class ActivationNotificationProvider implements IActivationNotificationProvider {

    private Set<IActivationNotificationListener> activationNotificationListeners;

    public ActivationNotificationProvider() {
        this.activationNotificationListeners = new HashSet<IActivationNotificationListener>();
    }

    @Override
    public boolean addActivationNotificationListener(final IActivationNotificationListener listener,
            final boolean fireNow) {
        boolean notContained = this.activationNotificationListeners.add(listener);
        if (notContained) {
            listenerAdded(listener, fireNow);
        }
        return notContained;
    }

    protected abstract void listenerAdded(final IActivationNotificationListener listener, final boolean fireNow);

    @Override
    public boolean removeActivationNotificationListener(final IActivationNotificationListener listener) {
        return this.activationNotificationListeners.remove(listener);
    }

    public void notifyActivationChanged(final Activation<? extends IPatternMatch> activation,
            final ActivationState oldState, final ActivationLifeCycleEvent event) {
        for (IActivationNotificationListener listener : this.activationNotificationListeners) {
            listener.activationChanged(activation, oldState, event);
        }
    }

}
