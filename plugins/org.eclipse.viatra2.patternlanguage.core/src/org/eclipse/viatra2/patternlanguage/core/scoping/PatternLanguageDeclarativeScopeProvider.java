/*******************************************************************************
 * Copyright (c) 2011 Zoltan Ujhelyi and Daniel Varro
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Zoltan Ujhelyi - initial API and implementation
 *******************************************************************************/
package org.eclipse.viatra2.patternlanguage.core.scoping;

import java.util.Collections;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.viatra2.patternlanguage.core.helper.CorePatternLanguageHelper;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.Pattern;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.PatternBody;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.PatternCall;
import org.eclipse.viatra2.patternlanguage.core.patternLanguage.Variable;
import org.eclipse.xtext.naming.QualifiedName;
import org.eclipse.xtext.resource.EObjectDescription;
import org.eclipse.xtext.resource.IEObjectDescription;
import org.eclipse.xtext.scoping.IScope;
import org.eclipse.xtext.scoping.impl.FilteringScope;
import org.eclipse.xtext.scoping.impl.SimpleScope;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

/**
 * <p>
 * An extended abstract declarative scope provider to facilitate the reusing of
 * abstract declarative scope providers together with XBase scope provider.
 * </p>
 * <p>
 * See <a href=
 * "http://www.eclipse.org/forums/index.php/mv/msg/219841/699521/#msg_699521"
 * >http://www.eclipse.org/forums/index.php/mv/msg/219841/699521/#msg_699521</a>
 * for details.
 * </p>
 * 
 * @author Zoltan Ujhelyi
 * 
 */
public class PatternLanguageDeclarativeScopeProvider extends
		MyAbstractDeclarativeScopeProvider {

	private final class UndefinedVariable implements Predicate<Variable> {
		@Override
		public boolean apply(Variable input) {
			System.out.println(input);
			return input.getName() != null && !input.getName().isEmpty();
		}
	}

	private final class CreateObjectDescFunction implements
			Function<Variable, IEObjectDescription> {
		public IEObjectDescription apply(Variable from) {
			return EObjectDescription.create(from.getName(), from);
		}
	}

	/**
	 * Custom scoping for patternRef in {@link PatternCall}. Currently returns
	 * all Pattern that is visible from the current context.
	 * 
	 * @param ctx
	 * @param ref
	 * @return
	 */
	public IScope scope_PatternCall_patternRef(PatternCall ctx, EReference ref) {
		IScope scope = delegateGetScope(ctx, ref);
		return new FilteringScope(scope, new Predicate<IEObjectDescription>() {
			@Override
			public boolean apply(IEObjectDescription input) {
				// filter not local, private patterns (private patterns in other
				// resources)
				// this information stored in the userdata of the
				// EObjectDescription
				// EObjectDescription only created for not local eObjects, so
				// check for resource equality is unnecessary.
				if ("true".equals(input.getUserData("private"))) {
					return false;
				}
				return true;
			}
		});
	}

	public IScope scope_VariableReference_variable(EObject ctx, EReference ref) {
		EObject it = ctx;
		PatternBody body = null;
		while (it != null && !(it instanceof Pattern)) {
			if (it instanceof PatternBody) {
				body = (PatternBody) it;
			}
			it = it.eContainer();
		}
		if (it == null) {
			return IScope.NULLSCOPE;
		}
		Pattern pattern = (Pattern) it;
		UndefinedVariable variableFilter = new UndefinedVariable();
		CreateObjectDescFunction createObjectDescFunction = new CreateObjectDescFunction();
		IScope parameterScope = new SimpleScope(IScope.NULLSCOPE,
				Iterables.transform(Iterables.filter(pattern.getParameters(),
						variableFilter), createObjectDescFunction));
		IScope localScope = new SimpleScope(parameterScope,
				Iterables.transform(
						Iterables.filter(body.getVariables(), variableFilter),
						createObjectDescFunction));
		return localScope;
	}
}
