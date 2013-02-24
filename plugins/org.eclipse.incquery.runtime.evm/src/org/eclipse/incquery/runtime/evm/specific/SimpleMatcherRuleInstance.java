/*******************************************************************************
 * Copyright (c) 2010-2013, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.evm.specific;

import org.apache.log4j.Logger;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.evm.api.RuleInstance;

/**
 * @author Abel Hegedus
 *
 */
public class SimpleMatcherRuleInstance<Match extends IPatternMatch, Matcher extends IncQueryMatcher<Match>> extends RuleInstance<Match> {
    
    private Matcher matcher;
    
    /**
     * 
     */
    public SimpleMatcherRuleInstance(final SimpleMatcherRuleSpecification<Match,Matcher> specification) {
        super(specification);
    }
    
    protected void prepareInstance(final Matcher matcher) {
        prepareMatchUpdateListener();
        prepateAttributeMonitor();
        
        this.matcher = matcher;
        this.matcher.addCallbackOnMatchUpdate(getMatchUpdateListener(), true);
    }
    
    /**
     * @return the matcher
     */
    public IncQueryMatcher<?> getMatcher() {
        return matcher;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.incquery.runtime.evm.api.RuleInstance#dispose()
     */
    @Override
    protected void dispose() {
        super.dispose();
        this.matcher.removeCallbackOnMatchUpdate(getMatchUpdateListener());
    }

    /* (non-Javadoc)
     * @see org.eclipse.incquery.runtime.evm.api.RuleInstance#getLogger()
     */
    @Override
    public Logger getLogger() {
        return matcher.getEngine().getLogger();
    }
}
