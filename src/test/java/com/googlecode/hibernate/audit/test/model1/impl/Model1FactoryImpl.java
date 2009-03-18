/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.test.model1.impl;

import com.googlecode.hibernate.audit.test.model1.*;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class Model1FactoryImpl extends EFactoryImpl implements Model1Factory {
    /**
     * Creates the default factory implementation.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public static Model1Factory init() {
        try {
            Model1Factory theModel1Factory = (Model1Factory)EPackage.Registry.INSTANCE.getEFactory("http://hibernate-audit.googlecode.com/test/model1/1.0.0"); 
            if (theModel1Factory != null) {
                return theModel1Factory;
            }
        }
        catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new Model1FactoryImpl();
    }

    /**
     * Creates an instance of the factory.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Model1FactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
            case Model1Package.MODEL1_PARENT: return createModel1Parent();
            case Model1Package.MODEL1_CHILD: return createModel1Child();
            case Model1Package.MODEL1_PERSON: return createModel1Person();
            default:
                throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Model1Parent createModel1Parent() {
        Model1ParentImpl model1Parent = new Model1ParentImpl();
        return model1Parent;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Model1Child createModel1Child() {
        Model1ChildImpl model1Child = new Model1ChildImpl();
        return model1Child;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Model1Person createModel1Person() {
        Model1PersonImpl model1Person = new Model1PersonImpl();
        return model1Person;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Model1Package getModel1Package() {
        return (Model1Package)getEPackage();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @deprecated
     * @generated
     */
    @Deprecated
    public static Model1Package getPackage() {
        return Model1Package.eINSTANCE;
    }

} //Model1FactoryImpl
