/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.test.model1;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Child</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.googlecode.hibernate.audit.test.model1.Model1Child#getParent <em>Parent</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.googlecode.hibernate.audit.test.model1.Model1Package#getModel1Child()
 * @model
 * @generated
 */
public interface Model1Child extends Model1Person {
    /**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link com.googlecode.hibernate.audit.test.model1.Model1Parent#getChildren <em>Children</em>}'.
	 * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Parent</em>' container reference isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(Model1Parent)
	 * @see com.googlecode.hibernate.audit.test.model1.Model1Package#getModel1Child_Parent()
	 * @see com.googlecode.hibernate.audit.test.model1.Model1Parent#getChildren
	 * @model opposite="children" required="true" transient="false"
	 * @generated
	 */
    Model1Parent getParent();

    /**
	 * Sets the value of the '{@link com.googlecode.hibernate.audit.test.model1.Model1Child#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
    void setParent(Model1Parent value);

} // Model1Child
