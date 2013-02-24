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
import java.util.Set;

import org.eclipse.incquery.runtime.rete.construction.RetePatternBuildException;
import org.eclipse.incquery.runtime.rete.construction.Stub;
import org.eclipse.incquery.runtime.rete.construction.psystem.PSystem;
import org.eclipse.incquery.runtime.rete.construction.psystem.PVariable;
import org.eclipse.incquery.runtime.rete.construction.psystem.VariableDeferredPConstraint;

/**
 * @author Bergmann Gábor
 * 
 */
public class ExportedParameter<PatternDescription, StubHandle> extends
        VariableDeferredPConstraint<PatternDescription, StubHandle> {
    PVariable parameterVariable;
    Object parameterName;

    /**
     * @param buildable
     * @param parameterVariable
     */
    public ExportedParameter(PSystem<PatternDescription, StubHandle, ?> pSystem, PVariable parameterVariable,
            String parameterName) {
        super(pSystem, Collections.singleton(parameterVariable));
        this.parameterVariable = parameterVariable;
        this.parameterName = parameterVariable.getName();
        // parameterVariable.setExportedParameter(true);
    }

    @Override
    public void doReplaceVariable(PVariable obsolete, PVariable replacement) {
        if (obsolete.equals(parameterVariable))
            parameterVariable = replacement;
    }

    @Override
    protected String toStringRest() {
        Object varName = parameterVariable.getName();
        return parameterName.equals(varName) ? parameterName.toString() : parameterName + "(" + varName + ")";
    }

    @Override
    public Set<PVariable> getDeducedVariables() {
        return Collections.emptySet();
    }

    /**
     * @return the parameterName
     */
    public Object getParameterName() {
        return parameterName;
    }

    /**
     * @return the parameterVariable
     */
    public PVariable getParameterVariable() {
        return parameterVariable;
    }

    @Override
    protected Set<PVariable> getDeferringVariables() {
        return Collections.singleton(parameterVariable);
    }

    @Override
    protected Stub<StubHandle> doCheckOn(Stub<StubHandle> stub) throws RetePatternBuildException {
        return stub;
    }

    @Override
    public void checkSanity() throws RetePatternBuildException {
        super.checkSanity();
        if (!parameterVariable.isDeducable()) {
            String[] args = { parameterName.toString() };
            String msg = "Impossible to match pattern: "
                    + "exported pattern variable {1} can not be determined based on the pattern constraints. "
                    + "HINT: certain constructs (e.g. negative patterns or check expressions) cannot output symbolic parameters.";
            String shortMsg = "Could not deduce value of parameter";
            throw new RetePatternBuildException(msg, args, shortMsg, null);
        }

    }

    @Override
    public void raiseForeverDeferredError(Stub<StubHandle> stub) throws RetePatternBuildException {
        String[] args = { parameterName.toString() };
        String msg = "Pattern Graph Search terminated incompletely: "
                + "exported pattern variable {1} could not be determined based on the pattern constraints. "
                + "HINT: certain constructs (e.g. negative patterns or check expressions) cannot output symbolic parameters.";
        String shortMsg = "Could not deduce value of parameter";
        throw new RetePatternBuildException(msg, args, shortMsg, null);
    }

}
