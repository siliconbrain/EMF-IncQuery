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

package org.eclipse.viatra2.emf.incquery.runtime.api.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature;
import org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.matcher.ReteEngine;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.matcher.RetePatternMatcher;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.misc.DeltaMonitor;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.tuple.Tuple;

/**
 * Base implementation of IncQueryMatcher.
 * @author Bergmann Gábor
 *
 * @param <Signature>
 */
/**
 * @author Bergmann Gábor
 *
 * @param <Signature>
 */
public abstract class BaseMatcher<Signature extends IPatternSignature> implements IncQueryMatcher<Signature> {

	// FIELDS AND CONSTRUCTOR
	
	protected ReteEngine<?> engine;
	protected RetePatternMatcher patternMatcher;
	private Map<String, Integer> posMapping;

	public BaseMatcher(ReteEngine<?> engine, RetePatternMatcher patternMatcher) {
		super();
		this.engine = engine;
		this.patternMatcher = patternMatcher;
	}


	// HELPERS
	
	protected abstract Signature tupleToSignature(Tuple t);

	private Object[] emptyArray() {
		return new Object[getParameterNames().length];
	}

	private boolean[] notNull(Object[] signature) {
		boolean[] notNull = new boolean[signature.length];
		for (int i=0; i<signature.length; ++i) notNull[i] = signature[i] != null;
		return notNull;
	}
	
	protected Map<String, Integer> getPosMapping() {
		if (posMapping == null)
		{
			posMapping = new HashMap<String, Integer>();
			int parameterPosition = 0;
			for (String parameterName : getParameterNames()) {
				posMapping.put(parameterName, parameterPosition++);
			}
		}
		return posMapping;
	}	
 
	// BASE IMPLEMENTATION

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getPositionOfParameter(java.lang.String)
	 */
	@Override
	public Integer getPositionOfParameter(String parameterName) {
		return getPosMapping().get(parameterName);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getAllMatchesAsArray()
	 */
	@Override
	public Collection<Object[]> getAllMatchesAsArray() {
		return getAllMatchesAsArray(emptyArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getAllMatchesAsSignature()
	 */
	@Override
	public Collection<Signature> getAllMatchesAsSignature() {
		return getAllMatchesAsSignature(emptyArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getAllMatchesAsArray(java.lang.Object[])
	 */
	@Override
	public Collection<Object[]> getAllMatchesAsArray(Object[] signature) {
		ArrayList<Tuple> m = patternMatcher.matchAll(signature, notNull(signature));
		ArrayList<Object[]> matches = new ArrayList<Object[]>();		
		//clones the tuples into Object arrays to prevent the Tuples from modifications outside of the ReteMatcher 
		for(Tuple t: m) matches.add(t.getElements());
		return matches;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getAllMatchesAsSignature(java.lang.Object[])
	 */
	@Override
	public Collection<Signature> getAllMatchesAsSignature(Object[] signature) {
		ArrayList<Tuple> m = patternMatcher.matchAll(signature, notNull(signature));
		ArrayList<Signature> matches = new ArrayList<Signature>();		
		//clones the tuples into Object arrays to prevent the Tuples from modifications outside of the ReteMatcher 
		for(Tuple t: m) matches.add(tupleToSignature(t));
		return matches;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getAllMatchesAsArray(org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature)
	 */
	@Override
	public Collection<Object[]> getAllMatchesAsArray(Signature signature) {
		return getAllMatchesAsArray(signature.toArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getAllMatchesAsSignature(org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature)
	 */
	@Override
	public Collection<Signature> getAllMatchesAsSignature(Signature signature) {
		return getAllMatchesAsSignature(signature.toArray());
	}
	// with input binding as pattern-specific parameters: not declared in interface

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getOneMatchAsArray()
	 */
	@Override
	public Object[] getOneMatchAsArray() {
		return getOneMatchAsArray(emptyArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getOneMatchAsSignature()
	 */
	@Override
	public Signature getOneMatchAsSignature() {
		return getOneMatchAsSignature(emptyArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getOneMatchAsArray(java.lang.Object[])
	 */
	@Override
	public Object[] getOneMatchAsArray(Object[] signature) {
		Tuple t = patternMatcher.matchOne(signature, notNull(signature));
		if(t != null) 
			return t.getElements();
		else
			return null; 	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getOneMatchAsSignature(java.lang.Object[])
	 */
	@Override
	public Signature getOneMatchAsSignature(Object[] signature) {
		Tuple t = patternMatcher.matchOne(signature, notNull(signature));
		if(t != null) 
			return tupleToSignature(t);
		else
			return null; 	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getOneMatchAsArray(org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature)
	 */
	@Override
	public Object[] getOneMatchAsArray(Signature signature) {
		return getOneMatchAsArray(signature.toArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#getOneMatchAsSignature(org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature)
	 */
	@Override
	public Signature getOneMatchAsSignature(Signature signature) {
		return getOneMatchAsSignature(signature.toArray());
	}
	// with input binding as pattern-specific parameters: not declared in interface

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#hasMatch(java.lang.Object[])
	 */
	@Override
	public boolean hasMatch(Object[] signature) {
		return patternMatcher.count(signature, notNull(signature)) > 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#hasMatch(org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature)
	 */
	@Override
	public boolean hasMatch(Signature signature) {
		return hasMatch(signature.toArray());
	}
	// with input binding as pattern-specific parameters: not declared in interface

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#countMatches()
	 */
	@Override
	public int countMatches() {
		return countMatches(emptyArray());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#countMatches(java.lang.Object[])
	 */
	@Override
	public int countMatches(Object[] signature) {
		return patternMatcher.count(signature, notNull(signature));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#countMatches(org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature)
	 */
	@Override
	public int countMatches(Signature signature) {
		return countMatches(signature.toArray());
	}
	// with input binding as pattern-specific parameters: not declared in interface

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#newDeltaMonitor(boolean)
	 */
	@Override
	public DeltaMonitor<Signature> newDeltaMonitor(boolean fillAtStart) {
		DeltaMonitor<Signature> dm = new DeltaMonitor<Signature>(engine.getReteNet().getHeadContainer()) {
			@Override
			public Signature statelessConvert(Tuple t) {
				return tupleToSignature(t);
			}
		};
		patternMatcher.connect(dm, fillAtStart);
		return dm;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#addCallbackAfterUpdates(java.lang.Runnable)
	 */
	@Override
	public boolean addCallbackAfterUpdates(Runnable callback) {
		return engine.getAfterUpdateCallbacks().add(callback);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#removeCallbackAfterUpdates(java.lang.Runnable)
	 */
	@Override
	public boolean removeCallbackAfterUpdates(Runnable callback) {
		return engine.getAfterUpdateCallbacks().remove(callback);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryMatcher#signatureToArray(org.eclipse.viatra2.emf.incquery.runtime.api.IPatternSignature)
	 */
	@Override
	public Object[] signatureToArray(Signature signature) {
		return signature.toArray();
	}
}