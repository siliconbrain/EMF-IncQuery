package org.eclipse.viatra2.patternlanguage.emf.tests.composition

import org.eclipse.xtext.junit4.util.ParseHelper
import org.junit.Before
import org.junit.Test
import org.eclipse.viatra2.patternlanguage.core.validation.IssueCodes
import org.eclipse.xtext.junit4.XtextRunner
import org.junit.runner.RunWith
import org.eclipse.xtext.junit4.InjectWith
import org.eclipse.viatra2.patternlanguage.EMFPatternLanguageInjectorProvider
import com.google.inject.Inject
import org.eclipse.viatra2.patternlanguage.validation.EMFPatternLanguageJavaValidator
import org.eclipse.xtext.junit4.validation.ValidatorTester
import com.google.inject.Injector
import org.eclipse.viatra2.patternlanguage.emf.tests.util.AbstractValidatorTest
import org.junit.Ignore
@RunWith(typeof(XtextRunner))
@InjectWith(typeof(EMFPatternLanguageInjectorProvider))

class TransitiveClosureTest extends AbstractValidatorTest{
		
	@Inject
	ParseHelper parseHelper
	@Inject
	EMFPatternLanguageJavaValidator validator
	@Inject
	Injector injector
	
	ValidatorTester<EMFPatternLanguageJavaValidator> tester
	
	@Before
	def void initialize() {
		tester = new ValidatorTester(validator, injector)
	}
	@Test
	def void validClosure() {
		val model = parseHelper.parse(
			'import "http://www.eclipse.org/viatra2/patternlanguage/core/PatternLanguage"

			pattern patternDependency(p1 : Pattern, p2 : Pattern) = {
				Pattern.bodies.constraints(p1,c);
				PatternCompositionConstraint.call.patternRef(c,p2);
			}

			pattern transitive(p : Pattern) = {
				find patternDependency+(p,p2);	// p2 should be a single variable, e.g. _p2
				Pattern(p2);					// Then this line can be deleted.
			}'
		) 
		tester.validate(model).assertOK;
	}	
	@Test
	def void wrongArityClosure() {
		val model = parseHelper.parse(
			'import "http://www.eclipse.org/viatra2/patternlanguage/core/PatternLanguage"

			pattern patternDependency(p1 : Pattern, p2 : Pattern, c) = {
				Pattern.bodies.constraints(p1,c);
				PatternCompositionConstraint.call.patternRef(c,p2);
			}

			pattern transitive(p : Pattern) = {
				find patternDependency+(p,p2,c);	// p2 and c should be single variables, e.g. _p2, _s
				Pattern(p2);						// Then these lines...
				Constraint(c);						// ...can be deleted.
			}'
		)
		tester.validate(model).assertAll(getErrorCode(IssueCodes::TRANSITIVE_PATTERNCALL_ARITY));
	}	
	@Test
	def void negatedClosure() {
		val model = parseHelper.parse(
			'import "http://www.eclipse.org/viatra2/patternlanguage/core/PatternLanguage"

			pattern patternDependency(p1 : Pattern, p2 : Pattern) = {
				Pattern.bodies.constraints(p1,c);
				PatternCompositionConstraint.call.patternRef(c,p2);
			}

			pattern transitive(p : Pattern) = {
				Pattern(p);
				neg find patternDependency+(p,p2);
			}'
		)
		tester.validate(model).assertAll(getErrorCode(IssueCodes::TRANSITIVE_PATTERNCALL_NOT_APPLICABLE));
	}	
	@Test
	def void aggregatedClosure() {
		val model = parseHelper.parse(
			'import "http://www.eclipse.org/viatra2/patternlanguage/core/PatternLanguage"

			pattern patternDependency(p1 : Pattern, p2 : Pattern) = {
				Pattern.bodies.constraints(p1,c);
				PatternCompositionConstraint.call.patternRef(c,p2);
			}

			pattern transitive(p : Pattern) = {
				Pattern(p);
				3 == count find patternDependency+(p,p2);	// p2 should be a single variable, e.g. _p2
				Pattern(p2);								// Then this line can be deleted.
			}'
		)
		tester.validate(model).assertAll(getErrorCode(IssueCodes::TRANSITIVE_PATTERNCALL_NOT_APPLICABLE));
	}	
}