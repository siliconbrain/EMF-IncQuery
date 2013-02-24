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

package org.eclipse.incquery.runtime.rete.tuple;

/**
 * @author Gabor Bergmann
 * 
 *         An instance of clearable pattern memory.
 */
public interface Clearable {
    /**
     * Clear all partial matchings stored in memory
     * 
     */
    void clear();
}
