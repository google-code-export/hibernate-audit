/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.test.model1;

import org.eclipse.emf.ecore.EFactory;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see com.googlecode.hibernate.audit.test.model1.Model1Package
 * @generated
 */
public interface Model1Factory extends EFactory {
    /**
     * The singleton instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    Model1Factory eINSTANCE = com.googlecode.hibernate.audit.test.model1.impl.Model1FactoryImpl.init();

    /**
     * Returns a new object of class '<em>Parent</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Parent</em>'.
     * @generated
     */
    Model1Parent createModel1Parent();

    /**
     * Returns a new object of class '<em>Child</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Child</em>'.
     * @generated
     */
    Model1Child createModel1Child();

    /**
     * Returns a new object of class '<em>Person</em>'.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return a new object of class '<em>Person</em>'.
     * @generated
     */
    Model1Person createModel1Person();

    /**
     * Returns the package supported by this factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @return the package supported by this factory.
     * @generated
     */
    Model1Package getModel1Package();

} //Model1Factory
