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

package org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.boundary;

import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.matcher.IPatternMatcherContext.GeneralizationQueryDirection;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.matcher.IPatternMatcherRuntimeContext;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.network.Network;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.network.Receiver;
import org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.remote.Address;


public class ReferenceFeeder extends Feeder {

	protected Object typeObject;

	/**
	 * @param receiver
	 * @param context
	 * @param network
	 * @param boundary
	 * @param typeObject
	 */
	public ReferenceFeeder(Address<? extends Receiver> receiver,
			IPatternMatcherRuntimeContext<?> context, Network network,
			ReteBoundary<?> boundary, Object typeObject) {
		super(receiver, context, network, boundary);
		this.typeObject = typeObject;
	}




	/* (non-Javadoc)
	 * @see org.eclipse.viatra2.gtasm.patternmatcher.incremental.rete.boundary.Feeder#feed()
	 */
	@Override
	public void feed() {
		if (typeObject != null) {
			if (context.allowedGeneralizationQueryDirection() == GeneralizationQueryDirection.BOTH)			
				context.enumerateDirectBinaryEdgeInstances(typeObject, pairCrawler());
			else
				context.enumerateAllBinaryEdgeInstances(typeObject, pairCrawler());		
		} else {
			context.enumerateAllBinaryEdges(pairCrawler());
		}
	}
	
}
