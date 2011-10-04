/*
 * generated by Xtext
 */
package org.eclipse.viatra2.patternlanguage.ui.outline;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.viatra2.patternlanguage.eMFPatternLanguage.PatternModel;
import org.eclipse.xtext.ui.editor.outline.impl.DefaultOutlineTreeProvider;
import org.eclipse.xtext.ui.editor.outline.impl.DocumentRootNode;

import com.google.common.collect.Iterables;

/**
 * customization of the default outline structure
 * 
 */
public class EMFPatternLanguageOutlineTreeProvider extends
		DefaultOutlineTreeProvider {

	protected void _createChildren(DocumentRootNode parentNode,
			PatternModel model) {
		for (EObject element : Iterables.concat(
				model.getImportPackages(), model.getPatterns())) {
			createNode(parentNode, element);
		}
	}
}
