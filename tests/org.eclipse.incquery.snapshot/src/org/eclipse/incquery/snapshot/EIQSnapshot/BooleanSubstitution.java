/**
 */
package org.eclipse.incquery.snapshot.EIQSnapshot;


/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Boolean Substitution</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link org.eclipse.incquery.snapshot.EIQSnapshot.BooleanSubstitution#isValue <em>Value</em>}</li>
 * </ul>
 * </p>
 *
 * @see org.eclipse.incquery.snapshot.EIQSnapshot.EIQSnapshotPackage#getBooleanSubstitution()
 * @model
 * @generated
 */
public interface BooleanSubstitution extends MatchSubstitutionRecord {
	/**
	 * Returns the value of the '<em><b>Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <p>
	 * If the meaning of the '<em>Value</em>' attribute isn't clear,
	 * there really should be more of a description here...
	 * </p>
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Value</em>' attribute.
	 * @see #setValue(boolean)
	 * @see org.eclipse.incquery.snapshot.EIQSnapshot.EIQSnapshotPackage#getBooleanSubstitution_Value()
	 * @model
	 * @generated
	 */
	boolean isValue();

	/**
	 * Sets the value of the '{@link org.eclipse.incquery.snapshot.EIQSnapshot.BooleanSubstitution#isValue <em>Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Value</em>' attribute.
	 * @see #isValue()
	 * @generated
	 */
	void setValue(boolean value);

} // BooleanSubstitution
