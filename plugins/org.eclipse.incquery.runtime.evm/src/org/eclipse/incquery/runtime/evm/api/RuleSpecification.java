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
package org.eclipse.incquery.runtime.evm.api;

import static com.google.common.base.Preconditions.checkNotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.incquery.runtime.api.IPatternMatch;
import org.eclipse.incquery.runtime.api.IncQueryEngine;

import com.google.common.base.Objects;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;

/**
 * @author Abel Hegedus
 * 
 *         implement rule specification - Activation Life Cycle - Jobs related to Activation State - create Rule
 *         Instance with Matcher(Factory)/Pattern
 */
public abstract class RuleSpecification<Match extends IPatternMatch> {

    private final ActivationLifeCycle lifeCycle;
    private final Multimap<ActivationState, Job<Match>> jobs;
    private final Comparator<Match> comparator;
    private final Set<ActivationState> enabledStates;

    public RuleSpecification(final ActivationLifeCycle lifeCycle,
            final Set<Job<Match>> jobs) {
        this(lifeCycle, jobs, null);
    }

    /**
     * 
     */
    public RuleSpecification(final ActivationLifeCycle lifeCycle,
            final Set<Job<Match>> jobs, final Comparator<Match> comparator) {
        this.lifeCycle = checkNotNull(ActivationLifeCycle.copyOf(lifeCycle),
                "Cannot create rule specification with null life cycle!");
        this.jobs = HashMultimap.create();
        Set<ActivationState> states = new TreeSet<ActivationState>();
        if (jobs != null && !jobs.isEmpty()) {
            for (Job<Match> job : jobs) {
                ActivationState state = job.getActivationState();
                this.jobs.put(state, job);
                states.add(state);
            }
        }
        this.enabledStates = ImmutableSet.copyOf(states);
        this.comparator = comparator;
    }
    
    protected abstract RuleInstance<Match> instantiateRule(final IncQueryEngine engine);
    
    /**
     * 
     * @return the lifeCycle
     */
    public ActivationLifeCycle getLifeCycle() {
        return lifeCycle;
    }

    /**
     * @return the enabledStates
     */
    public Set<ActivationState> getEnabledStates() {
        return enabledStates;
    }

    public Collection<Job<Match>> getJobs(final ActivationState state) {
        return jobs.get(state);
    }

    /**
     * @return the jobs
     */
    public Multimap<ActivationState, Job<Match>> getJobs() {
        return jobs;
    }

    /**
     * @return the comparator
     */
    public Comparator<Match> getComparator() {
        return comparator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("lifecycle", lifeCycle).add("jobs", jobs).add("comparator", comparator).toString();
    }
}
