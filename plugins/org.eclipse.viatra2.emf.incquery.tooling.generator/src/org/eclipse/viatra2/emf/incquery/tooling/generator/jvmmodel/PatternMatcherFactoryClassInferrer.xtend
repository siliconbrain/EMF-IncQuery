package org.eclipse.viatra2.emf.incquery.tooling.generator.jvmmodel

import com.google.inject.Inject
import org.eclipse.viatra2.emf.incquery.runtime.api.IncQueryEngine
import org.eclipse.viatra2.emf.incquery.runtime.api.impl.BaseGeneratedMatcherFactory
import org.eclipse.viatra2.emf.incquery.runtime.exception.IncQueryRuntimeException
import org.eclipse.viatra2.emf.incquery.tooling.generator.util.EMFJvmTypesBuilder
import org.eclipse.viatra2.emf.incquery.tooling.generator.util.EMFPatternLanguageJvmModelInferrerUtil
import org.eclipse.viatra2.patternlanguage.core.helper.CorePatternLanguageHelper
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.Pattern
import org.eclipse.xtext.common.types.JvmDeclaredType
import org.eclipse.xtext.common.types.JvmTypeReference
import org.eclipse.xtext.common.types.JvmVisibility

/**
 * {@link IMatcherFactory} implementation inferrer.
 * 
 * @author Mark Czotter
 */
class PatternMatcherFactoryClassInferrer {
	
	@Inject extension EMFJvmTypesBuilder
	@Inject extension EMFPatternLanguageJvmModelInferrerUtil
	@Inject extension JavadocInferrer

	/**
	 * Infers the {@link IMatcherFactory} implementation class from {@link Pattern}.
	 */		
	def JvmDeclaredType inferMatcherFactoryClass(Pattern pattern, boolean isPrelinkingPhase, String matcherFactoryPackageName, JvmTypeReference matchClassRef, JvmTypeReference matcherClassRef) {
		val matcherFactoryClass = pattern.toClass(pattern.matcherFactoryClassName) [
  			it.packageName = matcherFactoryPackageName
  			it.documentation = pattern.javadocMatcherFactoryClass.toString
  			it.superTypes += pattern.newTypeRef(typeof (BaseGeneratedMatcherFactory), cloneWithProxies(matchClassRef), cloneWithProxies(matcherClassRef))
  		]
  		matcherFactoryClass.inferMatcherFactoryMethods(pattern, matcherClassRef)
  		return matcherFactoryClass
  	}
  	
	/**
   	 * Infers methods for MatcherFactory class based on the input 'pattern'.
   	 */
  	def inferMatcherFactoryMethods(JvmDeclaredType matcherFactoryClass, Pattern pattern, JvmTypeReference matcherClassRef) {
  		matcherFactoryClass.members += pattern.toMethod("instantiate", cloneWithProxies(matcherClassRef)) [
			it.visibility = JvmVisibility::PROTECTED
			it.annotations += pattern.toAnnotation(typeof (Override))
			it.parameters += pattern.toParameter("engine", pattern.newTypeRef(typeof (IncQueryEngine)))
			it.exceptions += pattern.newTypeRef(typeof (IncQueryRuntimeException))
			it.body = [append('''
				return new «pattern.matcherClassName»(engine);
			''')]
		]
//		matcherFactoryClass.members += pattern.toMethod("patternString", pattern.newTypeRef(typeof (String))) [
//			it.visibility = JvmVisibility::PROTECTED
//			it.annotations += pattern.toAnnotation(typeof (Override))
//			it.body = ['''
//«««				Serialize the PatternModel
//«««				«pattern.eContainer.serializeToJava»
//«««				return patternString;  
//				throw new UnsupportedOperationException();
//			''']
//		]
		matcherFactoryClass.members += pattern.toMethod("getBundleName", pattern.newTypeRef(typeof (String))) [
			it.visibility = JvmVisibility::PROTECTED
			it.annotations += pattern.toAnnotation(typeof (Override))
			it.body = [append('''
				return "«pattern.bundleName»";
			''')]
		]
		matcherFactoryClass.members += pattern.toMethod("patternName", pattern.newTypeRef(typeof (String))) [
			it.visibility = JvmVisibility::PROTECTED
			it.annotations += pattern.toAnnotation(typeof (Override))
			it.body = [append('''
				return "«CorePatternLanguageHelper::getFullyQualifiedName(pattern)»";
			''')]
		]
  	}
	
}