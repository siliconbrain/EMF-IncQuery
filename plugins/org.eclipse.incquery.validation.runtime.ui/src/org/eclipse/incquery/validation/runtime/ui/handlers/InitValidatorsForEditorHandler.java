/*******************************************************************************
 * Copyright (c) 2010-2012, Zoltan Ujhelyi, Abel Hegedus, Tamas Szabo, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi, Abel Hegedus, Tamas Szabo - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.validation.runtime.ui.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.incquery.runtime.api.IModelConnector;
import org.eclipse.incquery.runtime.api.IModelConnectorTypeEnum;
import org.eclipse.incquery.runtime.exception.IncQueryException;
import org.eclipse.incquery.tooling.ui.queryexplorer.adapters.AdapterUtil;
import org.eclipse.incquery.validation.runtime.ui.ValidationInitUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;

public class InitValidatorsForEditorHandler extends AbstractHandler {

    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
        IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
        IModelConnector modelConnector = AdapterUtil.getModelConnectorFromIEditorPart(editorPart);
        ResourceSet resourceSet = (ResourceSet) modelConnector.getNotifier(IModelConnectorTypeEnum.RESOURCESET);
        if (resourceSet != null) {
            try {
                ValidationInitUtil.initializeAdapters(editorPart, resourceSet);
            } catch (IncQueryException ex) {
                throw new ExecutionException("Could not validate constraints due to a pattern matcher error", ex);
            }
        }
        return null;
    }

}