/*******************************************************************************
 * Copyright (c) 2010-2013, Adam Dudas, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Adam Dudas - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.base.core;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author Adam Dudas
 *
 */
public class AttributeSetHelperImpl {
	public static <A> Set<A> closure(Set<A> attributes, Map<Set<A>, Set<A>> dependencies) {
		Set<A> closureSet = new HashSet<A>(attributes);
		Set<A> newSet;
		
		do {
			newSet = closureStep(closureSet, dependencies);
		} while (!closureSet.containsAll(newSet));
		
		return closureSet;
	}
	
	private static <A> Set<A> closureStep(Set<A> originalSet, Map<Set<A>, Set<A>> dependencies) {
		Set<A> newSet = new HashSet<A>(originalSet);
		
		for (Entry<Set<A>, Set<A>> dependency : dependencies.entrySet()) {
			if (originalSet.containsAll(dependency.getKey()))
				newSet.addAll(dependency.getValue());
		}
		
		return newSet;
	}
}
