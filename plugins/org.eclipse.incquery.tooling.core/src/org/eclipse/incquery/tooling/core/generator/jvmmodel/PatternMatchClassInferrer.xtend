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
import java.util.Arrays
import org.eclipse.incquery.runtime.api.impl.BasePatternMatch
import org.eclipse.incquery.runtime.exception.IncQueryException
import org.eclipse.incquery.tooling.core.generator.util.EMFJvmTypesBuilder
import org.eclipse.incquery.tooling.core.generator.util.EMFPatternLanguageJvmModelInferrerUtil
import org.eclipse.incquery.patternlanguage.patternLanguage.Pattern
import org.eclipse.incquery.patternlanguage.patternLanguage.Variable
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmVisibility
import org.eclipse.xtext.common.types.util.TypeReferences
import org.eclipse.xtext.naming.IQualifiedNameProvider
import org.eclipse.xtext.xbase.compiler.output.ITreeAppendable
import org.eclipse.incquery.runtime.api.IPatternMatch

/**
 * {@link IPatternMatch} implementation inferer.
 * 
 * @author Mark Czotter
 */
class PatternMatchClassInferrer {
	
	@Inject extension EMFJvmTypesBuilder
	@Inject extension IQualifiedNameProvider
	@Inject extension EMFPatternLanguageJvmModelInferrerUtil
	@Inject extension JavadocInferrer
	@Inject TypeReferences typeReference
	
	/**
	 * Infers the {@link IPatternMatch} implementation class from {@link Pattern} parameters.
	 */
	def JvmDeclaredType inferMatchClass(Pattern pattern, boolean isPrelinkingPhase, String matchPackageName) {
		val matchClass = pattern.toClass(pattern.matchClassName) [
   			it.packageName = matchPackageName
   			it.documentation = pattern.javadocMatchClass.toString
   			//it.final = true
   			it.setAbstract(true)
   			it.superTypes += pattern.newTypeRef(typeof (BasePatternMatch))
   			//it.superTypes += pattern.newTypeRef(typeof (IPatternMatch))
   		]
   		matchClass.inferMatchClassFields(pattern)
   		matchClass.inferMatchClassConstructors(pattern)
   		matchClass.inferMatchClassGetters(pattern)
   		matchClass.inferMatchClassSetters(pattern)
   		matchClass.inferMatchClassMethods(pattern)
   		matchClass.inferCheckBodies(pattern)
  		matchClass.inferMatchInnerClasses(pattern)
   		
   		return matchClass
   	}
   	
   	/**
   	 * Infers fields for Match class based on the input 'pattern'.
   	 */
   	def inferMatchClassFields(JvmDeclaredType matchClass, Pattern pattern) {
   		for (Variable variable : pattern.parameters) {
   			matchClass.members += pattern.toField(variable.fieldName, variable.calculateType)
   		}
		matchClass.members += pattern.toField("parameterNames", pattern.newTypeRef(typeof (String)).addArrayTypeDimension) [
 			it.setStatic(true);
   			it.setInitializer([append('''{«FOR variable : pattern.parameters SEPARATOR ', '»"«variable.name»"«ENDFOR»}''')])
   		]
   	}
   	
   	/**
   	 * Infers constructors for Match class based on the input 'pattern'.
   	 */
   	def inferMatchClassConstructors(JvmDeclaredType matchClass, Pattern pattern) {
   		matchClass.members += pattern.toConstructor() [
   			it.simpleName = pattern.matchClassName
   			it.visibility = JvmVisibility::PRIVATE //DEFAULT
   			for (Variable variable : pattern.parameters) {
   				val javaType = variable.calculateType
   				it.parameters += variable.toParameter(variable.parameterName, javaType)
   			}
   			it.setBody([append('''
   				«FOR variable : pattern.parameters»
   				this.«variable.fieldName» = «variable.parameterName»;
   				«ENDFOR»
   			''')])
   		]
   	}
   	
   	/**
   	 * Infers getters for Match class based on the input 'pattern'.
   	 */
   	def inferMatchClassGetters(JvmDeclaredType matchClass, Pattern pattern) {
		matchClass.members += pattern.toMethod("get", pattern.newTypeRef(typeof (Object))) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.parameters += pattern.toParameter("parameterName", pattern.newTypeRef(typeof (String)))
   			it.setBody([append('''
   				«FOR variable : pattern.parameters»
   				if ("«variable.name»".equals(parameterName)) return this.«variable.fieldName»;
   				«ENDFOR»
   				return null;
   			''')])
   		]
   		for (Variable variable : pattern.parameters) {
			matchClass.members += pattern.toMethod(variable.getterMethodName, variable.calculateType) [
	   			it.setBody([append('''
	   				return this.«variable.fieldName»;
	   			''')])
	   		]
   		}
   	}
   	
   	/**
   	 * Infers setters for Match class based on the input 'pattern'.
   	 */
   	def inferMatchClassSetters(JvmDeclaredType matchClass, Pattern pattern) {
   		matchClass.members += pattern.toMethod("set", pattern.newTypeRef(typeof (boolean))) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.parameters += pattern.toParameter("parameterName", pattern.newTypeRef(typeof (String)))
   			it.parameters += pattern.toParameter("newValue", pattern.newTypeRef(typeof (Object)))
   			it.setBody([append('''
   				if (!isMutable()) throw new java.lang.UnsupportedOperationException();
   				«FOR variable : pattern.parameters»
   				«val type = variable.calculateType»
   				«val typeName = type.qualifiedName»
   				if ("«variable.name»".equals(parameterName) «IF typeReference.is(type, typeof(Object))»&& newValue instanceof «typeName»«ENDIF») {
   					this.«variable.fieldName» = («typeName») newValue;
   					return true;
   				}
   				«ENDFOR»
   				return false;
   			''')])
   		]
   		for (Variable variable : pattern.parameters) {
   			matchClass.members += pattern.toMethod(variable.setterMethodName, null) [
   				it.parameters += pattern.toParameter(variable.parameterName, variable.calculateType)
   				it.setBody([append('''
   					if (!isMutable()) throw new java.lang.UnsupportedOperationException();
   					this.«variable.fieldName» = «variable.parameterName»;
   				''')])
   			]
   		}
   	}
   	
	/**
   	 * Infers methods for Match class based on the input 'pattern'.
   	 */
   	def inferMatchClassMethods(JvmDeclaredType matchClass, Pattern pattern) {
   		matchClass.members += pattern.toMethod("patternName", pattern.newTypeRef(typeof(String))) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.setBody([append('''
   				return "«pattern.fullyQualifiedName»";
   			''')])
   		]
		// add extra methods like equals, hashcode, toArray, parameterNames
		matchClass.members += pattern.toMethod("parameterNames", pattern.newTypeRef(typeof (String)).addArrayTypeDimension) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.setBody([append('''
   				return «pattern.matchClassName».parameterNames;
   			''')])
   		]
   		matchClass.members += pattern.toMethod("toArray", pattern.newTypeRef(typeof (Object)).addArrayTypeDimension) [
   			it.annotations += pattern.toAnnotation(typeof (Override))
   			it.setBody([append('''
   				return new Object[]{«FOR variable : pattern.parameters SEPARATOR ', '»«variable.fieldName»«ENDFOR»};
   			''')])
   		]
		matchClass.members += pattern.toMethod("prettyPrint", pattern.newTypeRef(typeof (String))) [
			it.annotations += pattern.toAnnotation(typeof (Override))
			it.setBody([
				if (pattern.parameters.empty)
					append('''return "[]";''')
				else 
					append('''
				StringBuilder result = new StringBuilder();
				«FOR variable : pattern.parameters SEPARATOR " + \", \");\n" AFTER ");\n"»
					result.append("\"«variable.name»\"=" + prettyPrintValue(«variable.fieldName»)«ENDFOR»
				return result.toString();
			''')])
		]
		matchClass.members += pattern.toMethod("hashCode", pattern.newTypeRef(typeof (int))) [
			it.annotations += pattern.toAnnotation(typeof (Override))
			it.setBody([append('''
				final int prime = 31;
				int result = 1;
				«FOR variable : pattern.parameters»
				result = prime * result + ((«variable.fieldName» == null) ? 0 : «variable.fieldName».hashCode()); 
				«ENDFOR»
				return result; 
			''')])
		]
		matchClass.members += pattern.toMethod("equals", pattern.newTypeRef(typeof (boolean))) [
			it.annotations += pattern.toAnnotation(typeof (Override))
			it.parameters += pattern.toParameter("obj", pattern.newTypeRef(typeof (Object)))
			it.setBody([append('''
				if (this == obj)
					return true;
				if (!(obj instanceof «pattern.matchClassName»)) { // this should be infrequent				
					if (obj == null)
						return false;
					if (!(obj instanceof ''')
				referClass(pattern, typeof(IPatternMatch))
				append('''
				))
						return false;
				''')
				append("	")
				referClass(pattern, typeof(IPatternMatch))
				append(" ") append('''
				otherSig  = (''')
				referClass(pattern, typeof(IPatternMatch))
				append('''
				) obj;
					if (!pattern().equals(otherSig.pattern()))
						return false;
					return ''')
				referClass(pattern, typeof(Arrays))
				append('''
				.deepEquals(toArray(), otherSig.toArray());
				}
				«IF !pattern.parameters.isEmpty»
				«pattern.matchClassName» other = («pattern.matchClassName») obj;
				«FOR variable : pattern.parameters» 
				if («variable.fieldName» == null) {if (other.«variable.fieldName» != null) return false;}
				else if (!«variable.fieldName».equals(other.«variable.fieldName»)) return false;
				«ENDFOR»
				«ENDIF»
				return true;''')])
		]
		matchClass.members += pattern.toMethod("pattern", pattern.newTypeRef(typeof (Pattern))) [
			it.annotations += pattern.toAnnotation(typeof (Override))
			it.setBody([
				append('''
				try {
					return «pattern.matcherClassName».factory().getPattern();
				} catch (''') 
				referClass(pattern, typeof (IncQueryException)) 
				append(" ")
				append(''' 
				ex) {
				 	// This cannot happen, as the match object can only be instantiated if the matcher factory exists
				 	throw new ''')
				referClass(pattern, typeof (IllegalStateException))
				append('''
					(ex);
				}
			''')])
		]
  	}
   	
   	/**
   	 * Infers an equals method based on the 'pattern' parameter.
   	 */
   	def equalsMethodBody(Pattern pattern, ITreeAppendable appendable) {
   	}
	
	def inferCheckBodies(JvmDeclaredType matchClass, Pattern pattern) {
//		var i=1;
//		for (body : pattern.bodies) {
//			for (variable : body.variables) {
//				matchClass.members += variable.toField(variable.name, variable.calculateType)
//			}
//			for (CheckConstraint constraint : body.constraints.filter(typeof(CheckConstraint))) {
//				matchClass.members += pattern.toMethod("check" + i, pattern.newTypeRef(typeof(Boolean))) [
//					it.body = constraint.expression
//				]
//			}
//		}
	}
	
 	/**
   	 * Infers inner classes for Match class based on the input 'pattern'.
   	 */
  	def inferMatchInnerClasses(JvmDeclaredType matchClass, Pattern pattern) {
  		matchClass.members += matchClass.makeMatchInnerClass(pattern, pattern.matchMutableInnerClassName, true);
  		matchClass.members += matchClass.makeMatchInnerClass(pattern, pattern.matchImmutableInnerClassName, false);
	}
	
 	/**
   	 * Infers a single inner class for Match class
   	 */
	def makeMatchInnerClass(JvmDeclaredType matchClass, Pattern pattern, String innerClassName, boolean isMutable) {
		pattern.toClass(innerClassName) [
			it.visibility = JvmVisibility::DEFAULT
			it.setStatic(true)
			it.setFinal(true)
			it.superTypes += typeReference.createTypeRef(matchClass)
			
			it.members+= pattern.toConstructor() [
	   			it.simpleName = innerClassName
	   			it.visibility = JvmVisibility::DEFAULT
	   			for (Variable variable : pattern.parameters) {
	   				val javaType = variable.calculateType
	   				it.parameters += variable.toParameter(variable.parameterName, javaType)
	   			}
	   			it.setBody([append('''
	   				super(«FOR variable : pattern.parameters SEPARATOR ", "»«variable.parameterName»«ENDFOR»);
	   			''')])
			]
			it.members += pattern.toMethod("isMutable", pattern.newTypeRef(typeof (boolean))) [
				it.visibility = JvmVisibility::PUBLIC
				it.annotations += pattern.toAnnotation(typeof (Override))
				it.setBody([append('''return «isMutable»;''')])			
			]		
		]
	}
	
	
	
}