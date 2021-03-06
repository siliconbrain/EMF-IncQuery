package org.eclipse.viatra2.emf.incquery.queryexplorer.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.edit.domain.IEditingDomainProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.viatra2.emf.incquery.queryexplorer.QueryExplorer;
import org.eclipse.xtext.xbase.ui.editor.XbaseEditor;

import com.google.inject.Inject;
import com.google.inject.Injector;

@SuppressWarnings("restriction")
public class LoadModelHandler extends AbstractHandler {

	@Inject
	Injector injector;
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		
		try {
			IEditorPart editorPart = HandlerUtil.getActiveEditor(event);
			
			if (editorPart instanceof IEditingDomainProvider) {
				IEditingDomainProvider providerEditor = (IEditingDomainProvider) editorPart;
				
				ResourceSet resourceSet = providerEditor.getEditingDomain().getResourceSet();
				if (resourceSet.getResources().size() > 0) {
					HandlerUtil.getActivePart(event).getSite().getPage().addPartListener(QueryExplorer.getInstance().getModelPartListener());
					QueryExplorer.getInstance().getMatcherTreeViewerRoot().addPatternMatcherRoot(editorPart, resourceSet);
				}
			}
			else if (editorPart instanceof XbaseEditor) {
				IFile file = (IFile) HandlerUtil.getActiveEditorInput(event).getAdapter(IFile.class);	
				if (file != null) {
					RuntimeMatcherRegistrator registrator = new RuntimeMatcherRegistrator(file);
					injector.injectMembers(registrator);
					registrator.run();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
				
		return null;
	}
}
