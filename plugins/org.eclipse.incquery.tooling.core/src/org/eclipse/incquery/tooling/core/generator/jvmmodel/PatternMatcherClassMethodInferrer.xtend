/*******************************************************************************
 * Copyright (c) 2010-2012, Mark Czotter, Istvan Rath and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Mark Czotter - initial API and implementation
 *******************************************************************************/

package org.eclipse.incquery.tooling.core.generator.jvmmodel

import com.google.inject.Inject
import java.util.Collection
import java.util.HashSet
import java.util.Set
import org.eclipse.incquery.runtime.api.IMatchProcessor
import org.eclipse.incquery.tooling.core.generator.util.EMFJvmTypesBuilder
import org.eclipse.incquery.tooling.core.generator.util.EMFPatternLanguageJvmModelInferrerUtil
import org.eclipse.incquery.runtime.rete.misc.DeltaMonitor
import org.eclipse.incquery.runtime.rete.tuple.Tuple
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable

class PatternMatcherClassMethodInferrer {
	
	@Inject extension JavadocInferrer
	@Inject extension EMFPatternLanguageJvmModelInferrerUtil
	@Inject extension EMFJvmTypesBuilder
	
	/**
   	 * Infers methods for Matcher class based on the input 'pattern'.
   	 */
   	def inferMethods(JvmDeclaredType type, Pattern pattern, JvmTypeReference matchClassReference) {
   		// Adding type-safe matcher calls
		// if the pattern not defines parameters, the Matcher class contains only the hasMatch method
		if (!pattern.parameters.isEmpty) {
			 type.members += pattern.toMethod("getAllMatches", pattern.newTypeRef(typeof(Collection), cloneWithProxies(matchClassReference))) [
   				it.documentation = pattern.javadocGetAllMatchesMethod.toString
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
   				it.setBody([append('''
   					return rawGetAllMatches(new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»});''')
   				])
   			]
   			type.members += pattern.toMethod("getOneArbitraryMatch", cloneWithProxies(matchClassReference)) [
   				it.documentation = pattern.javadocGetOneArbitraryMatchMethod.toString
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
   				it.setBody([append('''
   					return rawGetOneArbitraryMatch(new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»});''')
   				])
   			]
   			type.members += pattern.toMethod("hasMatch", pattern.newTypeRef(typeof(boolean))) [
   				it.documentation = pattern.javadocHasMatchMethod.toString
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
   				it.setBody([append('''
   					return rawHasMatch(new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»});''')
   				])
   			]
   			type.members += pattern.toMethod("countMatches", pattern.newTypeRef(typeof(int))) [
   				it.documentation = pattern.javadocCountMatchesMethod.toString
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
   				it.setBody([append('''
   					return rawCountMatches(new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»});''')
   				])
   			]
   			type.members += pattern.toMethod("forEachMatch", null) [
   				it.documentation = pattern.javadocForEachMatchMethod.toString
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
				it.parameters += pattern.toParameter("processor", pattern.newTypeRef(typeof (IMatchProcessor), cloneWithProxies(matchClassReference).wildCardSuper))
   				it.setBody([append('''
   					rawForEachMatch(new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»}, processor);''')
   				])
   			]
   			type.members += pattern.toMethod("forOneArbitraryMatch", pattern.newTypeRef(typeof(boolean))) [
   				it.documentation = pattern.javadocForOneArbitraryMatchMethod.toString
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
   				it.parameters += pattern.toParameter("processor", pattern.newTypeRef(typeof (IMatchProcessor), cloneWithProxies(matchClassReference).wildCardSuper))
   				it.setBody([append('''
   					return rawForOneArbitraryMatch(new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»}, processor);''')
   				])
   			]
   			type.members += pattern.toMethod("newFilteredDeltaMonitor", pattern.newTypeRef(typeof(DeltaMonitor), cloneWithProxies(matchClassReference))) [
   				it.documentation = pattern.javadocNewFilteredDeltaMonitorMethod.toString
    			it.parameters += pattern.toParameter("fillAtStart", pattern.newTypeRef(typeof (boolean)))
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
   				it.setBody([append('''
   					return rawNewFilteredDeltaMonitor(fillAtStart, new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»});''')
   				])
   			]
   			type.members += pattern.toMethod("newMatch", cloneWithProxies(matchClassReference)) [
    			it.documentation = pattern.javadocNewMatchMethod.toString
   				for (parameter : pattern.parameters){
					it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
   				}
   				it.setBody([append('''
   					return new ''')
	   				serialize(matchClassReference, pattern)
	   				append('''
	   				.«pattern.matchImmutableInnerClassName»(«FOR p : pattern.parameters SEPARATOR ', '»«p.parameterName»«ENDFOR»);
	   				''')
   				])
	   		]
   			for (variable : pattern.parameters){
   				val typeOfVariable = variable.calculateType
   				type.members += pattern.toMethod("rawAccumulateAllValuesOf"+variable.name, pattern.newTypeRef(typeof(Set), typeOfVariable)) [
	   				it.documentation = variable.javadocGetAllValuesOfMethod.toString
	   				it.parameters += pattern.toParameter("parameters", pattern.newTypeRef(typeof (Object)).addArrayTypeDimension)
   					it.visibility = JvmVisibility::PROTECTED
	   				it.setBody([
						referClass(pattern, typeof(Set), typeOfVariable)
						append(''' results = new ''')
	   					referClass(pattern, typeof(HashSet), typeOfVariable)
	   					append('''
	   					();
	   					rawAccumulateAllValues(«variable.positionConstant», parameters, results);
	   					return results;''')
					])
	   			]
	   			type.members += pattern.toMethod("getAllValuesOf"+variable.name, pattern.newTypeRef(typeof(Set), typeOfVariable)) [
	   				it.documentation = variable.javadocGetAllValuesOfMethod.toString
	   				it.setBody([append('''
	   					return rawAccumulateAllValuesOf«variable.name»(emptyArray());''')
	   				])
	   			]
	   			if(pattern.parameters.size > 1){
		   			type.members += pattern.toMethod("getAllValuesOf"+variable.name, pattern.newTypeRef(typeof(Set), typeOfVariable)) [
		   				it.documentation = variable.javadocGetAllValuesOfMethod.toString
		   				it.parameters += pattern.toParameter("partialMatch", cloneWithProxies(matchClassReference))
		   				it.setBody([append('''
		   					return rawAccumulateAllValuesOf«variable.name»(partialMatch.toArray());''')])
		   			]
		   			type.members += pattern.toMethod("getAllValuesOf"+variable.name, pattern.newTypeRef(typeof(Set), typeOfVariable)) [
		   				it.documentation = variable.javadocGetAllValuesOfMethod.toString
		   				for (parameter : pattern.parameters){
		   					if(parameter != variable){
								it.parameters += parameter.toParameter(parameter.parameterName, parameter.calculateType)				
			   				}
		   				}
		   				it.setBody([
		   					append('''return rawAccumulateAllValuesOf«variable.name»(new Object[]{«FOR p : pattern.parameters SEPARATOR ', '»«
		   						if (p.parameterName == variable.parameterName) "null" else p.parameterName  
		   						»«ENDFOR»});'''
		   					)
		   				])
		   			]
	   			}
   			}
		} else {
			type.members += pattern.toMethod("hasMatch", pattern.newTypeRef(typeof(boolean))) [
   				it.documentation = pattern.javadocHasMatchMethodNoParameter.toString
   				it.setBody([append('''
   					return rawHasMatch(new Object[]{});''')
   				])
   			]
		}
		
		type.inferMatcherClassToMatchMethods(pattern, matchClassReference)
   	}
   	
   	/**
   	 * Infers tupleToMatch, arrayToMatch methods for Matcher class based on the input 'pattern'.
   	 */   	
   	def inferMatcherClassToMatchMethods(JvmDeclaredType matcherClass, Pattern pattern, JvmTypeReference matchClassRef) {
	   	val tupleToMatchMethod = pattern.toMethod("tupleToMatch", cloneWithProxies(matchClassRef)) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.visibility = JvmVisibility::PROTECTED
   			it.parameters += pattern.toParameter("t", pattern.newTypeRef(typeof (Tuple)))
   		]
   		val arrayToMatchMethod = pattern.toMethod("arrayToMatch", cloneWithProxies(matchClassRef)) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.visibility = JvmVisibility::PROTECTED
   			it.parameters += pattern.toParameter("match", pattern.newTypeRef(typeof (Object)).addArrayTypeDimension)
   		]
   		val arrayToMatchMutableMethod = pattern.toMethod("arrayToMatchMutable", cloneWithProxies(matchClassRef)) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.visibility = JvmVisibility::PROTECTED
   			it.parameters += pattern.toParameter("match", pattern.newTypeRef(typeof (Object)).addArrayTypeDimension)
   		]
   		tupleToMatchMethod.setBody([it | pattern.inferTupleToMatchMethodBody(it)])
   		arrayToMatchMethod.setBody([it | pattern.inferArrayToMatchMethodBody(it)])
   		arrayToMatchMutableMethod.setBody([it | pattern.inferArrayToMatchMutableMethodBody(it)])
   		matcherClass.members += tupleToMatchMethod
   		matcherClass.members += arrayToMatchMethod
   		matcherClass.members += arrayToMatchMutableMethod
   	}
  	
  	/**
  	 * Infers the tupleToMatch method body.
  	 */
  	def inferTupleToMatchMethodBody(Pattern pattern, ITreeAppendable appendable) {
   		appendable.append('''
   			try {
   				return new «pattern.matchClassName».«pattern.matchImmutableInnerClassName»(«FOR p : pattern.parameters SEPARATOR ', '»(«p.calculateType.qualifiedName») t.get(«p.positionConstant»)«ENDFOR»);	
   			} catch(ClassCastException e) {''')
   		inferErrorLogging("Element(s) in tuple not properly typed!", "e", appendable)
   		appendable.append('''
   				//throw new IncQueryRuntimeException(e.getMessage());
   				return null;
   			}
   		''')
  	}
  	
  	/**
  	 * Infers the arrayToMatch method body.
  	 */
  	def inferArrayToMatchMethodBody(Pattern pattern, ITreeAppendable appendable) {
  		appendable.append('''
   			try {
   				return new «pattern.matchClassName».«pattern.matchImmutableInnerClassName»(«FOR p : pattern.parameters SEPARATOR ', '»(«p.calculateType.qualifiedName») match[«p.positionConstant»]«ENDFOR»);
   			} catch(ClassCastException e) {''')
   		inferErrorLogging("Element(s) in array not properly typed!", "e", appendable)
   		appendable.append('''
   				//throw new IncQueryRuntimeException(e.getMessage());
   				return null;
   			}
   		''')
  	}
  	/**
  	 * Infers the arrayToMatch method body.
  	 */
  	def inferArrayToMatchMutableMethodBody(Pattern pattern, ITreeAppendable appendable) {
  		appendable.append('''
   			try {
   				return new «pattern.matchClassName».«pattern.matchMutableInnerClassName»(«FOR p : pattern.parameters SEPARATOR ', '»(«p.calculateType.qualifiedName») match[«p.positionConstant»]«ENDFOR»);
   			} catch(ClassCastException e) {''')
   		inferErrorLogging("Element(s) in array not properly typed!", "e", appendable)
   		appendable.append('''
   				//throw new IncQueryRuntimeException(e.getMessage());
   				return null;
   			}
   		''')
  	}
	  	
  	/**
  	 * Infers the appropriate logging based on the parameters.
  	 * 
  	 */
  	def inferErrorLogging(String message, String exceptionName,  ITreeAppendable appendable) {
  		if(exceptionName == null){
	  		appendable.append('''engine.getLogger().error("«message»");''')
  		} else {
  			appendable.append('''engine.getLogger().error("«message»",«exceptionName»);''')
  		}
	}
	
}