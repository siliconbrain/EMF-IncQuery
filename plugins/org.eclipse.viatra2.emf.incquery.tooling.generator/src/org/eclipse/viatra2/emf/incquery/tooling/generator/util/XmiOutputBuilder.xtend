package org.eclipse.viatra2.emf.incquery.tooling.generator.util

import java.util.ArrayList
import org.apache.log4j.Logger
import org.eclipse.core.resources.IProject
import org.eclipse.core.resources.IResource
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.viatra2.emf.incquery.runtime.util.XmiModelUtil
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.Pattern
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.PatternCompositionConstraint
import org.eclipse.viatra2.patternlanguage.eMFPatternLanguage.EMFPatternLanguageFactory
import org.eclipse.viatra2.patternlanguage.eMFPatternLanguage.PackageImport
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.viatra2.patternlanguage.core.helper.CorePatternLanguageHelper

/**
 * @author Mark Czotter
 */
class XmiOutputBuilder {
	
	Logger logger = Logger::getLogger(getClass())
	
	/**
	 * Builds one model file (XMI) from the input into the folder.
	 */
	def build(ResourceSet resourceSet, IProject project) {
		try {
			val folder = project.getFolder(XmiModelUtil::XMI_OUTPUT_FOLDER)
			val file = folder.getFile(XmiModelUtil::GLOBAL_EIQ_FILENAME)
			if (!folder.exists) {
				folder.create(IResource::DEPTH_INFINITE, false, null)
			}
			if (file.exists) {
				file.delete(true, null)
			}
			// create the model in memory
			val xmiModelRoot = EMFPatternLanguageFactory::eINSTANCE.createPatternModel()
			val xmiResource = resourceSet.createResource(URI::createPlatformResourceURI(file.fullPath.toOSString, true))
			// add import declarations 
			val importDeclarations = newHashSet()
			/*
			 * The following change avoids two different errors:
			 *  * concurrentmodification of the growing list of resources
			 *  * and a bug wrt Guice flatten and EMF BasicEList
			 */ 
			//val packageImports = resourceSet.resources.map(r | r.allContents.toIterable.filter(typeof (PackageImport))).flatten
			val resources = new ArrayList(resourceSet.resources)
			for (r : resources) {
				val packageImports = r.allContents.toIterable.filter(typeof (PackageImport))
				if (!packageImports.empty) {
					for (importDecl : packageImports) {
						if (!importDeclarations.contains(importDecl.EPackage)) {
							importDeclarations.add(importDecl.EPackage)
							xmiModelRoot.importPackages.add(importDecl)
						}
					}
				} 
			}
			// first add all patterns
			val fqnToPatternMap = newHashMap();
			for (pattern : resourceSet.resources.map(r | r.allContents.toIterable.filter(typeof (Pattern))).flatten) {
				val p = (EcoreUtil2::copy(pattern)) as Pattern //casting required to avoid build error
				val fqn = CorePatternLanguageHelper::getFullyQualifiedName(pattern)
				p.setName(fqn)
				if (fqnToPatternMap.get(fqn) != null) {
					logger.error("Pattern already set in the Map: " + fqn)
				} else {
					fqnToPatternMap.put(fqn, p)
					xmiModelRoot.patterns.add(p)
				}	
			}
			// then iterate over all added PatternCompositonConstraint and change the patternRef
			for (constraint : xmiModelRoot.eAllContents.toIterable.filter(typeof (PatternCompositionConstraint))) {
				val fqn = CorePatternLanguageHelper::getFullyQualifiedName(constraint.patternRef)
				val p = fqnToPatternMap.get(fqn)
				if (p == null) {
					logger.error("Pattern not found: " +fqn)
				} else {
					constraint.setPatternRef(p as Pattern)
				}
			}
			// save the xmi file 
			xmiResource.contents.add(xmiModelRoot)
			xmiResource.save(null)
		} catch(Exception e) {
			logger.error("Exception during XMI build!", e)
		}
	}
	
}
