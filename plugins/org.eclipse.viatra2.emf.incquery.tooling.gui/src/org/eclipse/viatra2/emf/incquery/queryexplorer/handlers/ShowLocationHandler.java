package org.eclipse.viatra2.emf.incquery.queryexplorer.handlers;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.viatra2.emf.incquery.queryexplorer.content.matcher.ObservablePatternMatch;

public class ShowLocationHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil.getCurrentSelection(event);
		if (selection instanceof TreeSelection) {
			showLocation((TreeSelection) selection);
		}
		return null;
	}
	
	public static void showLocation(TreeSelection selection) {
		Object obj = selection.getFirstElement();
		
		if (obj != null && obj instanceof ObservablePatternMatch) {
			ObservablePatternMatch pm = (ObservablePatternMatch) obj;
			
			IEditorPart editorPart = pm.getParent().getParent().getEditorPart();
			Object[] locationObjects = pm.getLocationObjects();
			TreePath[] paths = new TreePath[locationObjects.length];
			int i = 0;
			
			for (Object o: locationObjects) {
				TreePath path = createTreePath((EObject) o);
				paths[i] = path;
				i++;
			}

			TreeSelection treeSelection = new TreeSelection(paths);
			ISelectionProvider selectionProvider = editorPart.getEditorSite().getSelectionProvider();
			selectionProvider.setSelection(treeSelection);
			
			//bring editor part to top
			editorPart.getSite().getPage().bringToTop(editorPart);
			
			//Reflection API is used here!!!
			try {
				Method m = editorPart.getClass().getMethod("setSelectionToViewer", Collection.class);
				m.invoke(editorPart, treeSelection.toList());
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			} 
		}
	}
	
	private static TreePath createTreePath(EObject obj) {
		List<Object> nodes = new ArrayList<Object>();
		nodes.add(obj);
		EObject tmp = obj.eContainer();
		
		while (tmp != null) {
			nodes.add(0, tmp);
			tmp = tmp.eContainer();
		}
		
		return new TreePath(nodes.toArray());
	}
}
