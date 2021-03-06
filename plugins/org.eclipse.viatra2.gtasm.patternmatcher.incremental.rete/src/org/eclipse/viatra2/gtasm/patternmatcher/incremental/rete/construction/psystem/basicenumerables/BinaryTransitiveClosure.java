/*******************************************************************************
 * Copyright (c) 2004-2010 Gabor Bergmann and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.construction.psystem.basicenumerables;

import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.construction.RetePatternBuildException;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.construction.Stub;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.construction.psystem.KeyedEnumerablePConstraint;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.construction.psystem.PSystem;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.tuple.Tuple;

/**
 * @author Bergmann GĂˇbor
 *
 * For a binary base pattern, computes the irreflexive transitive closure (base)+
 */
public class BinaryTransitiveClosure<PatternDescription, StubHandle> 
	extends KeyedEnumerablePConstraint<PatternDescription, PatternDescription, StubHandle> {

	/**
	 * @param pSystem
	 * @param variablesTuple
	 * @param pattern
	 */
	public BinaryTransitiveClosure(
			PSystem<PatternDescription, StubHandle, ?> pSystem,
			Tuple variablesTuple, PatternDescription pattern) {
		super(pSystem, variablesTuple, pattern);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.construction.psystem.EnumerablePConstraint#doCreateStub()
	 */
	@Override
	public Stub<StubHandle> doCreateStub() throws RetePatternBuildException {
		Stub<StubHandle> patternProduction = buildable.patternCallStub(variablesTuple, supplierKey);
		return buildable.buildTransitiveClosure(patternProduction);
	}

}
