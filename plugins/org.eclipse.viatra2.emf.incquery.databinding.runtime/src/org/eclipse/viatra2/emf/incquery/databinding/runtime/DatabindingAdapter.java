package org.eclipse.viatra2.emf.incquery.databinding.runtime;

import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.viatra2.emf.incquery.runtime.api.IPatternMatch;

/**
 * The class is used to observ given parameters of a pattern.
 * 
 * @author Tamas Szabo
 *
 * @param <T> the type parameter of the match
 */
public abstract class DatabindingAdapter<T extends IPatternMatch> {
	
	/**
	 * Returns the array of observable values.
	 * 
	 * @return the array of values
	 */
	public abstract String[] getParameterNames();
	
	/**
	 * Returns an observable value for the given match and parameterName.
	 * 
	 * @param match the match object
	 * @param parameterName the parameter name
	 * @return an observable value
	 */
	public abstract IObservableValue getObservableParameter(T match, String parameterName);
}
