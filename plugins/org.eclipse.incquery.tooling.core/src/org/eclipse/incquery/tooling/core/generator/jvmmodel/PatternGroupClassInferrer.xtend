/*******************************************************************************
 * Copyright (c) 2010-2012, Mark Czotter, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mark Czotter - initial API and implementation
 *   Andras Okros - minor changes
 *******************************************************************************/
package org.eclipse.incquery.tooling.core.generator.jvmmodel

import com.google.inject.Inject
import java.util.HashSet
import org.eclipse.incquery.runtime.api.impl.BaseGeneratedPatternGroup
import org.eclipse.incquery.runtime.exception.IncQueryException
import org.eclipse.incquery.tooling.core.generator.util.EMFJvmTypesBuilder
import org.eclipse.incquery.tooling.core.generator.util.EMFPatternLanguageJvmModelInferrerUtil
import org.eclipse.incquery.patternlanguage.patternLanguage.PatternModel
import org.eclipse.xtext.common.types.JvmConstructor
import org.eclipse.xtext.common.types.JvmGenericType
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.util.TypeReferences
import org.eclipse.xtext.xbase.jvmmodel.IJvmModelAssociations
import org.eclipse.incquery.patternlanguage.helper.CorePatternLanguageHelper

/**
 * Model Inferrer for Pattern grouping. Infers a Group class for every PatternModel.
 */
class PatternGroupClassInferrer {
	
	@Inject extension EMFJvmTypesBuilder
	@Inject extension EMFPatternLanguageJvmModelInferrerUtil
	@Inject extension IJvmModelAssociations
	@Inject extension TypeReferences types
	
	def inferPatternGroup(PatternModel model) {
		val groupClass = model.toClass(model.groupClassName) [
			it.packageName = model.packageName
			it.final = true
			it.superTypes += model.newTypeRef(typeof (BaseGeneratedPatternGroup))
		]
		groupClass.members += model.inferConstructor
		groupClass
	}
	
	def String groupClassName(PatternModel model) {
		val fileName = model.modelFileName 
		return "GroupOfFile" + fileName.toFirstUpper
	}
	
	def JvmConstructor inferConstructor(PatternModel model) {
		val incQueryException = model.newTypeRef(typeof (IncQueryException)) 
		val matcherReferences = gatherMatchers(model)
		model.toConstructor [
			it.visibility = JvmVisibility::PUBLIC
			it.simpleName = groupClassName(model)
			it.exceptions += incQueryException
			it.setBody([
				for (matcherRef : matcherReferences) {
					append('''matcherFactories.add(''')
					serialize(matcherRef, model)
					append('''.factory());''')
					newLine
				}
			])
		]
	}
	
	def gatherMatchers(PatternModel model) {
		val result = new HashSet<JvmTypeReference>()
		for (pattern : model.patterns) {
			if (!CorePatternLanguageHelper::isPrivate(pattern)) {
				val jvmElements = pattern.jvmElements
				val matcherClass = jvmElements.findFirst([e | e instanceof JvmGenericType])	
				if (matcherClass instanceof JvmGenericType) {
					val sourceElementRef = types.createTypeRef(matcherClass as JvmGenericType)
					result.add(sourceElementRef)
				}
			}
		}
		result
	}
	
}