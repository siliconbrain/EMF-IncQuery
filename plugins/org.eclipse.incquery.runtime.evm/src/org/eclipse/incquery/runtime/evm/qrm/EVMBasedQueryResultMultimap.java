/*******************************************************************************
 * Copyright (c) 2010-2012, Abel Hegedus, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Abel Hegedus - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.evm.qrm;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.incquery.runtime.api.EngineManager;
import org.eclipse.incquery.runtime.api.IMatchProcessor;
import org.eclipse.incquery.runtime.api.IMatcherFactory;
import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IncQueryEngine;
import org.eclipse.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.incquery.runtime.base.api.QueryResultMultimap;
import org.eclipse.incquery.runtime.evm.api.ActivationState;
import org.eclipse.incquery.runtime.evm.api.EventDrivenVM;
import org.eclipse.incquery.runtime.evm.api.Job;
import org.eclipse.incquery.runtime.evm.api.RuleEngine;
import org.eclipse.incquery.runtime.evm.specific.DefaultActivationLifeCycle;
import org.eclipse.incquery.runtime.evm.specific.SimpleMatcherRuleSpecification;
import org.eclipse.incquery.runtime.evm.specific.StatelessJob;
import org.eclipse.incquery.runtime.evm.specific.UpdateCompleteBasedScheduler;
import org.eclipse.incquery.runtime.exception.IncQueryException;

/**
 * @author Abel Hegedus
 * 
 */
public abstract class EVMBasedQueryResultMultimap<Match extends IPatternMatch, KeyType, ValueType> extends
        QueryResultMultimap<KeyType, ValueType> {

    private final Set<Job<Match>> jobs;

    private final RuleEngine engine;

    /**
     * @param agenda
     */
    protected EVMBasedQueryResultMultimap(final RuleEngine engine) {
        super(engine.getIncQueryEngine().getLogger());
        this.engine = engine;
        this.jobs = new HashSet<Job<Match>>();
        jobs.add(new StatelessJob<Match>(ActivationState.APPEARED, new IMatchProcessor<Match>() {
            @Override
            public void process(final Match match) {
                KeyType key = getKeyFromMatch(match);
                ValueType value = getValueFromMatch(match);
                internalPut(key, value);
            }
        }));

        jobs.add(new StatelessJob<Match>(ActivationState.DISAPPEARED, new IMatchProcessor<Match>() {
            @Override
            public void process(final Match match) {
                KeyType key = getKeyFromMatch(match);
                ValueType value = getValueFromMatch(match);
                internalRemove(key, value);
            }
        }));
    }

    /**
     * @throws IncQueryException
     * 
     */
    protected EVMBasedQueryResultMultimap(final IncQueryEngine engine) {
        this(EventDrivenVM.createExecutionSchema(engine,
                UpdateCompleteBasedScheduler.getIQBaseSchedulerFactory(engine)));
    }

    /**
     * @throws IncQueryException
     *             if the {@link IncQueryEngine} creation fails on the {@link Notifier}
     * 
     */
    protected EVMBasedQueryResultMultimap(final Notifier notifier) throws IncQueryException {
        this(EngineManager.getInstance().getIncQueryEngine(notifier));
    }

    public <Matcher extends IncQueryMatcher<Match>> void addMatcherToMultimapResults(
            final IMatcherFactory<Matcher> factory) {
        engine.addRule(new SimpleMatcherRuleSpecification<Match, Matcher>(factory,
                DefaultActivationLifeCycle.DEFAULT_NO_UPDATE, jobs));
    }

    protected abstract KeyType getKeyFromMatch(final Match match);

    protected abstract ValueType getValueFromMatch(final Match match);

}
