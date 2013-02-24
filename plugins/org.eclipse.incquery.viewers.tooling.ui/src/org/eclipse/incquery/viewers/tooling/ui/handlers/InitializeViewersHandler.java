/*******************************************************************************
 * Copyright (c) 2010-2013, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Istvan Rath - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.viewers.tooling.ui.handlers;

import java.util.ArrayList;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.tooling.ui.queryexplorer.content.matcher.ObservablePatternMatcher;
import org.eclipse.incquery.tooling.ui.queryexplorer.content.matcher.ObservablePatternMatcherRoot;
import org.eclipse.incquery.viewers.tooling.ui.views.ZestView;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Temporary handler class to initialize the sandbox viewer.
 * 
 * Problems: only works on ObservablePatternMatcherRoot.
 * @author istvanrath
 *
 */
public class InitializeViewersHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        
        ISelection selection = HandlerUtil.getActiveMenuSelection(event);
        if (selection instanceof TreeSelection) {
            ObservablePatternMatcherRoot root = (ObservablePatternMatcherRoot) ((TreeSelection) selection).getFirstElement();
            
            try {
                IEditorPart editorPart = root.getEditorPart();
                if (editorPart instanceof IEditingDomainProvider) {
                    IEditingDomainProvider providerEditor = (IEditingDomainProvider) editorPart;
                    ResourceSet resourceSet = providerEditor.getEditingDomain().getResourceSet();
                    if (resourceSet.getResources().size() > 0) {
                        
                        // calculate patterns that need to be passed to the ZestView
                        
                        ArrayList<Pattern> patterns = new ArrayList<Pattern>();
                        
                        for (ObservablePatternMatcher opm: root.getMatchers()) {
                            patterns.add( opm.getMatcher().getPattern() );
                        }
                        
                        
                        if (ZestView.getInstance() != null) {
                            ZestView.getInstance().setContents(resourceSet, patterns);
                        }
                    }
                }
            } catch (IncQueryException e) {
                throw new ExecutionException("Error initializing pattern matcher.", e);
            }
        }
        
        return null;
    }

}
