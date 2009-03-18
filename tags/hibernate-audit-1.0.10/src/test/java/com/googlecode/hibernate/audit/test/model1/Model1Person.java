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
 * A representation of the model object '<em><b>Person</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.googlecode.hibernate.audit.test.model1.Model1Person#getId <em>Id</em>}</li>
 *   <li>{@link com.googlecode.hibernate.audit.test.model1.Model1Person#getName <em>Name</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.googlecode.hibernate.audit.test.model1.Model1Package#getModel1Person()
 * @model
 * @generated
 */
public interface Model1Person extends EObject {
    /**
     * Returns the value of the '<em><b>Id</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Id</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Id</em>' attribute.
     * @see #setId(Long)
     * @see com.googlecode.hibernate.audit.test.model1.Model1Package#getModel1Person_Id()
     * @model id="true"
     * @generated
     */
    Long getId();

    /**
     * Sets the value of the '{@link com.googlecode.hibernate.audit.test.model1.Model1Person#getId <em>Id</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Id</em>' attribute.
     * @see #getId()
     * @generated
     */
    void setId(Long value);

    /**
     * Returns the value of the '<em><b>Name</b></em>' attribute.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Name</em>' attribute isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Name</em>' attribute.
     * @see #setName(String)
     * @see com.googlecode.hibernate.audit.test.model1.Model1Package#getModel1Person_Name()
     * @model
     * @generated
     */
    String getName();

    /**
     * Sets the value of the '{@link com.googlecode.hibernate.audit.test.model1.Model1Person#getName <em>Name</em>}' attribute.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @param value the new value of the '<em>Name</em>' attribute.
     * @see #getName()
     * @generated
     */
    void setName(String value);

} // Model1Person
