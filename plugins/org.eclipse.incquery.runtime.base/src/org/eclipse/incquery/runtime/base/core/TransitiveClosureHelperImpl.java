/*******************************************************************************
 * Copyright (c) 2010-2012, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Szabo, Gabor Bergmann - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.runtime.base.core;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.incquery.runtime.base.api.FeatureListener;
import org.eclipse.incquery.runtime.base.api.InstanceListener;
import org.eclipse.incquery.runtime.base.api.NavigationHelper;
import org.eclipse.incquery.runtime.base.api.TransitiveClosureHelper;
import org.eclipse.incquery.runtime.base.exception.IncQueryBaseException;
import org.eclipse.incquery.runtime.base.itc.alg.incscc.IncSCCAlg;
import org.eclipse.incquery.runtime.base.itc.igraph.ITcObserver;

/**
 * Implementation class for the {@link TransitiveClosureHelper}.
 * It uses a {@link NavigationHelper} instance to wrap an EMF model 
 * and make it suitable for the {@link IncSCCAlg} algorithm. 
 * 
 * @author Tamas Szabo
 * 
 */
public class TransitiveClosureHelperImpl extends EContentAdapter implements TransitiveClosureHelper,
        ITcObserver<EObject>, FeatureListener, InstanceListener {

    private IncSCCAlg<EObject> sccAlg;
    private Set<EStructuralFeature> features;
    private Set<EClass> classes;
    private EMFDataSource dataSource;
    private ArrayList<ITcObserver<EObject>> tcObservers;
    private NavigationHelper navigationHelper;
    
    public TransitiveClosureHelperImpl(NavigationHelper navigationHelper, Set<EReference> references) throws IncQueryBaseException {
        this.tcObservers = new ArrayList<ITcObserver<EObject>>();
        this.navigationHelper = navigationHelper;
		
		//NavigationHelper only accepts Set<EStructuralFeature> upon registration
		this.features = new HashSet<EStructuralFeature>(references);
		this.navigationHelper.registerEStructuralFeatures(features);
		this.classes = collectEClasses();
		this.navigationHelper.registerEClasses(classes);
        
		this.navigationHelper.registerFeatureListener(features, this);
		this.navigationHelper.registerInstanceListener(classes, this);
		
		this.dataSource = new EMFDataSource(navigationHelper, references, classes);
		
        this.sccAlg = new IncSCCAlg<EObject>(dataSource);
        this.sccAlg.attachObserver(this);
    }
    
	private Set<EClass> collectEClasses() {
		Set<EClass> classes = new HashSet<EClass>();
		for (EStructuralFeature ref : features) {
			classes.add(ref.getEContainingClass());
			classes.add(((EReference) ref).getEReferenceType());
		}
		return classes;
	}

    @Override
    public void attachObserver(ITcObserver<EObject> to) {
        this.tcObservers.add(to);
    }

    @Override
    public void detachObserver(ITcObserver<EObject> to) {
        this.tcObservers.remove(to);
    }

    @Override
    public Set<EObject> getAllReachableTargets(EObject source) {
        return this.sccAlg.getAllReachableTargets(source);
    }

    @Override
    public Set<EObject> getAllReachableSources(EObject target) {
        return this.sccAlg.getAllReachableSources(target);
    }

    @Override
    public boolean isReachable(EObject source, EObject target) {
        return this.sccAlg.isReachable(source, target);
    }

    @Override
    public void tupleInserted(EObject source, EObject target) {
        for (ITcObserver<EObject> to : tcObservers) {
            to.tupleInserted(source, target);
        }
    }

    @Override
    public void tupleDeleted(EObject source, EObject target) {
        for (ITcObserver<EObject> to : tcObservers) {
            to.tupleDeleted(source, target);
        }
    }

    @Override
    public void dispose() {
        this.sccAlg.dispose();
        this.navigationHelper.unregisterInstanceListener(classes, this);
        this.navigationHelper.unregisterFeatureListener(features, this);
        this.navigationHelper.dispose();
    }

	@Override
	public void featureInserted(EObject host, EStructuralFeature feature, Object value) {
		this.dataSource.notifyEdgeInserted(host, (EObject) value);
	}

	@Override
	public void featureDeleted(EObject host, EStructuralFeature feature, Object value) {
		this.dataSource.notifyEdgeDeleted(host, (EObject) value);
	}

	@Override
	public void instanceInserted(EClass clazz, EObject instance) {
		this.dataSource.notifyNodeInserted(instance);
	}

	@Override
	public void instanceDeleted(EClass clazz, EObject instance) {
		this.dataSource.notifyNodeDeleted(instance);
	}
}
