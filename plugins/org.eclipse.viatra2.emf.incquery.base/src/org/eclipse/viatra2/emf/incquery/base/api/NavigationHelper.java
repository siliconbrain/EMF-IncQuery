package org.eclipse.viatra2.emf.incquery.base.api;

import java.util.Collection;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature.Setting;

/**
 * 
 * The interface exposes three useful functionalities for EMF models:
 * <br>
 * - Inverse navigation along arbitrary EReferences (heterogenous paths too) 
 * <br>
 * - Finding model elements by attribute value (i.e. inverse navigation along EAttributes) 
 * <br>
 * - Getting all the (direct) instances of a given EClass 
 * <br><br>
 * Note that the helper will maintain these information incrementally 
 * on a given node's subgraph in the model ({@link EObject}, {@link Resource} or {@link ResourceSet}).
 * 
 * @author Tamas Szabo
 *
 */
public interface NavigationHelper {
	
	/**
	 * Find all the EAttributes and their owners for a given <code>value</code> of the attribute.
	 * The method will return these information as a collection of {@link EStructuralFeature.Setting}.
	 * 
	 * @param value the value of the attribute
	 * @return the collection of settings
	 */
	public Collection<Setting> findByAttributeValue(Object value);
	
	/**
	 * Find all the EAttributes and their owners for a given <code>value</code> of the attribute.
	 * The method will return these information as a collection of {@link EStructuralFeature.Setting}.
	 * Note that a setting will be present in the returned collection only if 
	 * its attribute instance can be found in the given set of <code>attributes</code>.
	 * 
	 * @param value the value of the attribute
	 * @param attributes the set of attributes
	 * @return the collection of settings
	 */
	public Collection<Setting> findByAttributeValue(Object value, Set<EAttribute> attributes);
	
	/**
	 * Find all EObjects that have an <code>attribute</code> {@link EAttribute} and its value equals to the given <code>value</code>. 
	 * The method will return these {@link EObject} instances in a {@link Set}.
	 * 
	 * @param value the value of the attribute
	 * @param attribute the EAttribute instance
	 * @return the set of EObject instances
	 */
	public Set<EObject> findByAttributeValue(Object value, EAttribute attribute);
	
	/**
	 * Find all the EAttributes and their owners which have a value of a class that equals to the given one.
	 * The method will return these information as a collection of {@link EStructuralFeature.Setting}. 
	 * 
	 * @param clazz the class of the value
	 * @return the collection of settings
	 */
	public Collection<Setting> findAllAttributeValuesByType(Class<?> clazz);
	
	/**
	 * Find all the EObject instances that have an {@link EReference} instance with the given <code>target</code>.
	 * The method will return these information as a collection of {@link EStructuralFeature.Setting}. 
	 * 
	 * @param target the endpoint of a reference
	 * @return the collection of settings
	 */
	public Collection<Setting> getInverseReferences(EObject target);
	
	/**
	 * Find all the EObject instances that have an EReference instance with the given <code>target</code>.
	 * The method will return these information as a collection of {@link EStructuralFeature.Setting}.
	 * Note that a setting will be present in the returned collection only if 
	 * its reference instance can be found in the given set of <code>references</code>.
	 * 
	 * @param target
	 * @param references
	 * @return
	 */
	public Collection<Setting> getInverseReferences(EObject target, Set<EReference> references);
	
	/**
	 * Find all EObjects that have a <code>reference</code> EReference instance with the given <code>target</code>. 
	 * The method will return these EObject instances in a {@link Set}.
	 * 
	 * @param target the endpoint of a reference
	 * @param reference the EReference instance
	 * @return the set of EObject instances
	 */
	public Set<EObject> getInverseReferences(EObject target, EReference reference);
	
	/**
	 * Get the direct EObject instances of the given EClass instance.
	 * @param clazz the EClass instance
	 * @return the set of EObject instances
	 */
	public Set<EObject> getDirectInstances(EClass clazz);
	
	/**
	 * Get the exact and descendant EObject instances of the given EClass instance. 
	 * 
	 * @param clazz the EClass instance 
	 * @return the set of EObject instances
	 */
	public Set<EObject> getAllInstances(EClass clazz);
	
	/**
	 * Call this method to dispose the NavigationHelper instance.
	 * The NavigationHelper instance will unregister itself from the list of EContentAdapters of the given Notifier instance.
	 */
	public void dispose();
}
