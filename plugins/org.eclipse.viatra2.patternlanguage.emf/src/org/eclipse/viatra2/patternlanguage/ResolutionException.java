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
package org.eclipse.viatra2.patternlanguage;

public class ResolutionException extends Exception {

	private static final long serialVersionUID = 5920889201819465489L;

	public ResolutionException() {
		super();
	}

	public ResolutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public ResolutionException(String message) {
		super(message);
	}

	public ResolutionException(Throwable cause) {
		super(cause);
	}

	
}
