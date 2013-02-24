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

package org.eclipse.incquery.runtime.rete.construction.psystem.basicdeferred;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.incquery.runtime.rete.construction.RetePatternBuildException;
import org.eclipse.incquery.runtime.rete.construction.Stub;
import org.eclipse.incquery.runtime.rete.construction.helpers.BuildHelper;
import org.eclipse.incquery.runtime.rete.construction.psystem.PSystem;
import org.eclipse.incquery.runtime.rete.construction.psystem.PVariable;
import org.eclipse.incquery.runtime.rete.tuple.Tuple;
import org.eclipse.incquery.runtime.rete.tuple.TupleMask;

/**
 * @author Bergmann Gábor
 * 
 */
public class PatternMatchCounter<PatternDescription, StubHandle> extends
        PatternCallBasedDeferred<PatternDescription, StubHandle> {

    private PVariable resultVariable;

    /**
     * @param buildable
     * @param affectedVariables
     */
    public PatternMatchCounter(PSystem<PatternDescription, StubHandle, ?> pSystem, Tuple actualParametersTuple,
            PatternDescription pattern, PVariable resultVariable) {
        super(pSystem, actualParametersTuple, pattern, Collections.singleton(resultVariable));
        this.resultVariable = resultVariable;
    }

    @Override
    public Set<PVariable> getDeducedVariables() {
        return Collections.singleton(resultVariable);
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.incquery.runtime.rete.construction.psystem.BasePConstraint#getFunctionalKeys()
     */
    @Override
    public Set<Set<PVariable>> getFunctionalKeys() {
    	final HashSet<Set<PVariable>> result = new HashSet<Set<PVariable>>();
    	result.add(getDeferringVariables());
		return result;
    }

    @Override
    protected void doDoReplaceVariables(PVariable obsolete, PVariable replacement) {
        if (resultVariable.equals(obsolete))
            resultVariable = replacement;
    }

    @Override
    protected Set<PVariable> getCandidateQuantifiedVariables() {
        return actualParametersTuple.<PVariable> getDistinctElements();
    }

    @Override
    protected Stub<StubHandle> doCheckOn(Stub<StubHandle> stub) throws RetePatternBuildException {
        Stub<StubHandle> sideStub = getSideStub();
        BuildHelper.JoinHelper<StubHandle> joinHelper = getJoinHelper(stub, sideStub);
        Integer resultPositionLeft = stub.getVariablesIndex().get(resultVariable);
        TupleMask primaryMask = joinHelper.getPrimaryMask();
        TupleMask secondaryMask = joinHelper.getSecondaryMask();
        if (resultPositionLeft == null) {
            return buildable.buildCounterBetaNode(stub, sideStub, primaryMask, secondaryMask,
                    joinHelper.getComplementerMask(), resultVariable);
        } else {
            int resultPositionFinal = primaryMask.indices.length; // append to the last position
            primaryMask = TupleMask.append(primaryMask,
                    TupleMask.selectSingle(resultPositionLeft, primaryMask.sourceWidth));
            return buildable.buildCountCheckBetaNode(stub, sideStub, primaryMask, secondaryMask, resultPositionFinal);
        }

    }

    @Override
    protected String toStringRest() {
        return pSystem.getContext().printPattern(pattern) + "@" + actualParametersTuple.toString() + "->"
                + resultVariable.toString();
    }

}
