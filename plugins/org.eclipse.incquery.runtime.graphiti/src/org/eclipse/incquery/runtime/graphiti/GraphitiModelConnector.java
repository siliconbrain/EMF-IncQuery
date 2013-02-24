/*******************************************************************************
 * Copyright (c) 2010-2012, Tamas Szabo, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Tamas Szabo - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.runtime.graphiti;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.common.notify.Notifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.gef.EditPart;
import org.eclipse.graphiti.mm.pictograms.Diagram;
import org.eclipse.graphiti.mm.pictograms.PictogramElement;
import org.eclipse.graphiti.services.Graphiti;
import org.eclipse.graphiti.ui.editor.DiagramEditor;
import org.eclipse.incquery.runtime.api.IModelConnectorTypeEnum;
import org.eclipse.incquery.tooling.ui.queryexplorer.adapters.EMFModelConnector;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.ui.IEditorPart;

/**
 * Model connector implementation for the Graphiti model editors.
 */
public class GraphitiModelConnector extends EMFModelConnector {

    public GraphitiModelConnector(IEditorPart editorPart) {
        super(editorPart);
    }

    @Override
    public Notifier getNotifier(IModelConnectorTypeEnum modelConnectorTypeEnum) {
        Notifier result = null;
        if (editorPart instanceof DiagramEditor) {
            if (IModelConnectorTypeEnum.RESOURCESET.equals(modelConnectorTypeEnum)) {
                DiagramEditor diagramEditor = (DiagramEditor) editorPart;
                return diagramEditor.getEditingDomain().getResourceSet();
            } else if (IModelConnectorTypeEnum.RESOURCE.equals(modelConnectorTypeEnum)) {
                DiagramEditor diagramEditor = (DiagramEditor) editorPart;
                PictogramElement[] selectedElements = diagramEditor.getSelectedPictogramElements();
                if (selectedElements.length > 0) {
                    PictogramElement element = selectedElements[0];
                    EObject businessObject = Graphiti.getLinkService().getBusinessObjectForLinkedPictogramElement(
                            element);
                    if (businessObject != null) {
                        return businessObject.eResource();
                    }
                }
            }
        }
        return result;
    }

    @Override
    public void showLocation(Object[] locationObjects) {
        // reflective set selection is not needed
        IStructuredSelection preparedSelection = prepareSelection(locationObjects);
        navigateToElements(getKey().getEditorPart(), preparedSelection);
        workbenchPage.bringToTop(getKey().getEditorPart());
        // reflectiveSetSelection(key.getEditorPart(), preparedSelection);
    }

    @Override
    protected TreePath createTreePath(IEditorPart editor, EObject obj) {
        if (editor instanceof DiagramEditor) {
            Diagram diagram = ((DiagramEditor) editor).getDiagramTypeProvider().getDiagram();
            List<PictogramElement> pictogramElements = Graphiti.getLinkService().getPictogramElements(diagram, obj);
            if (!pictogramElements.isEmpty()) {
                List<EditPart> parts = new ArrayList<EditPart>();
                for (PictogramElement element : pictogramElements) {
                    EditPart part = ((DiagramEditor) editor).getEditPartForPictogramElement(element);
                    if (part != null) {
                        parts.add(part);
                    }
                }
                return new TreePath(parts.toArray());
            }
        }
        return null;
    }

    @Override
    protected void navigateToElements(IEditorPart editorPart, IStructuredSelection selection) {
        if (editorPart instanceof DiagramEditor) {
            DiagramEditor providerEditor = (DiagramEditor) editorPart;
            providerEditor.getGraphicalViewer().setSelection(selection);
        }
    }

}
