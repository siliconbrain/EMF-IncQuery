/*******************************************************************************
 * Copyright (c) 2010-2013, Zoltan Ujhelyi, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.incquery.viewers.runtime.sources;

import java.util.Collection;
import java.util.Map;

import org.eclipse.core.databinding.observable.list.IListChangeListener;
import org.eclipse.core.databinding.observable.list.IObservableList;
import org.eclipse.core.databinding.observable.list.ListChangeEvent;
import org.eclipse.core.databinding.observable.list.ListDiff;
import org.eclipse.core.databinding.observable.list.ListDiffEntry;
import org.eclipse.incquery.viewers.runtime.model.Containment;
import org.eclipse.incquery.viewers.runtime.model.Item;
import org.eclipse.incquery.viewers.runtime.model.ViewerDataModel;
import org.eclipse.jface.viewers.AbstractTreeViewer;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;

/**
 * @author Zoltan Ujhelyi
 *
 */
public class TreeContentProvider extends ListContentProvider implements ITreeContentProvider {

    private final class ContainmentListChangeListener implements IListChangeListener {

        private AbstractTreeViewer viewer;

        public ContainmentListChangeListener(AbstractTreeViewer viewer) {
            super();
            this.viewer = viewer;
        }

        @Override
        public void handleListChange(ListChangeEvent event) {
            ListDiff diff = event.diff;
            for (ListDiffEntry entry : diff.getDifferences()) {
                Containment edge = (Containment) entry.getElement();
                if (entry.isAddition()) {
                    viewer.add(edge.getSource(), edge.getTarget());
                    elementMap.put(edge.getSource(), edge.getTarget());
                    parentMap.put(edge.getTarget(), edge.getSource());
                } else {
                    viewer.remove(edge.getSource(), new Object[] { edge.getTarget() });
                    elementMap.remove(edge.getSource(), edge.getTarget());
                    parentMap.remove(edge.getTarget());
                }
            }
        }
    }
    
    IObservableList containmentList;
    ContainmentListChangeListener containmentListener;
    Multimap<Item, Item> elementMap;
    Map<Item, Item> parentMap;
    private AbstractTreeViewer viewer;

    protected void initializeContent(Viewer viewer, ViewerDataModel vmodel) {
        this.viewer = (AbstractTreeViewer) viewer;
        super.initializeContent(viewer, vmodel);

        
        containmentList = vmodel.initializeObservableContainmentList();
        elementMap = HashMultimap.create();
        parentMap = Maps.newHashMap();

        for (Object obj : containmentList) {
            Containment containment = (Containment) obj;
            elementMap.put(containment.getSource(), containment.getTarget());
            parentMap.put(containment.getTarget(), containment.getSource());
        }
        
        containmentListener = new ContainmentListChangeListener((AbstractTreeViewer) viewer);
        containmentList.addListChangeListener(containmentListener);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
     */
    @Override
    public Object[] getChildren(Object parentElement) {
        if (parentElement instanceof Item) {
            Collection<Item> children = elementMap.get((Item) parentElement);
            return children.toArray(new Item[children.size()]);
        }
        return null;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
     */
    @Override
    public Object getParent(Object element) {
        return parentMap.get(element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
     */
    @Override
    public boolean hasChildren(Object element) {
        return !(elementMap.get((Item) element).isEmpty());
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.incquery.querybasedui.runtime.sources.ListContentProvider#removeListeners()
     */
    @Override
    protected void removeListeners() {
        if (containmentList != null && !containmentList.isDisposed() && containmentListener != null) {
            containmentList.removeListChangeListener(containmentListener);
        }
        super.removeListeners();
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.incquery.querybasedui.runtime.sources.ListContentProvider#handleListChanges(org.eclipse.core.databinding
     * .observable.list.ListDiff)
     */
    @Override
    protected void handleListChanges(ListDiff diff) {
        for (ListDiffEntry entry : diff.getDifferences()) {
            if (entry.isAddition()) {
                viewer.add(new TreePath(new Object[0]), entry.getElement());
            } else {
                viewer.remove(entry.getElement());
            }
        }
    }

}
