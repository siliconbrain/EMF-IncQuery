package org.eclipse.viatra2.patternlanguage.emf.tests

import com.google.inject.Inject
import org.eclipse.xtext.junit4.util.ParseHelper
import org.eclipse.xtext.junit4.validation.ValidationTestHelper
import org.junit.Test
import org.junit.runner.RunWith
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.xtext.junit4.XtextRunner
import org.eclipse.viatra2.patternlanguage.EMFPatternLanguageInjectorProvider
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.PatternLanguagePackage
import org.eclipse.xtext.diagnostics.Diagnostic

@RunWith(typeof(XtextRunner))
@InjectWith(typeof(EMFPatternLanguageInjectorProvider))
class CompositionTest {
	
	@Inject
	ParseHelper parseHelper
	
	@Inject extension ValidationTestHelper
	
	@Test
	def void testSimpleComposition() {
		parseHelper.parse(
			'import "http://www.eclipse.org/viatra2/patternlanguage/core/PatternLanguage"

			pattern calledPattern(p : Pattern) = {
				Pattern(p);
			}

			pattern callPattern(p : Pattern) = {
				find calledPattern(p);
			}'
		).assertNoErrors
	}
	
	@Test
	def void testRecursiveComposition() {
		parseHelper.parse(
			'import "http://www.eclipse.org/viatra2/patternlanguage/core/PatternLanguage"

			pattern calledPattern(p : Pattern) = {
				Pattern(p);
			} or {
				find calledPattern(p);
			}'
		).assertNoErrors
	}
	
	@Test
	def void testMissingComposition() {
		parseHelper.parse(
			'
			import "http://www.eclipse.org/viatra2/patternlanguage/core/PatternLanguage"

			pattern callPattern(p : Pattern) = {
				find calledPatternMissing(p);
			}'
		).assertError(PatternLanguagePackage::eINSTANCE.patternCompositionConstraint, 
			Diagnostic::LINKING_DIAGNOSTIC, 
			"Couldn't resolve reference to Pattern 'calledPatternMissing'."
		)
	}
}