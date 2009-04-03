/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.test.model1;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Parent</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * <ul>
 *   <li>{@link com.googlecode.hibernate.audit.test.model1.Model1Parent#getChildren <em>Children</em>}</li>
 * </ul>
 * </p>
 *
 * @see com.googlecode.hibernate.audit.test.model1.Model1Package#getModel1Parent()
 * @model
 * @generated
 */
public interface Model1Parent extends Model1Person {
    /**
     * Returns the value of the '<em><b>Children</b></em>' containment reference list.
     * The list contents are of type {@link com.googlecode.hibernate.audit.test.model1.Model1Child}.
     * It is bidirectional and its opposite is '{@link com.googlecode.hibernate.audit.test.model1.Model1Child#getParent <em>Parent</em>}'.
     * <!-- begin-user-doc -->
     * <p>
     * If the meaning of the '<em>Children</em>' containment reference list isn't clear,
     * there really should be more of a description here...
     * </p>
     * <!-- end-user-doc -->
     * @return the value of the '<em>Children</em>' containment reference list.
     * @see com.googlecode.hibernate.audit.test.model1.Model1Package#getModel1Parent_Children()
     * @see com.googlecode.hibernate.audit.test.model1.Model1Child#getParent
     * @model opposite="parent" containment="true"
     *        annotation="teneo.jpa appinfo='@OneToMany(mappedBy = \"parent\", indexed=false)'"
     * @generated
     */
    EList<Model1Child> getChildren();

} // Model1Parent
