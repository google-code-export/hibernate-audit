/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.test.model1.impl;

import com.googlecode.hibernate.audit.test.model1.Model1Child;
import com.googlecode.hibernate.audit.test.model1.Model1Factory;
import com.googlecode.hibernate.audit.test.model1.Model1Package;
import com.googlecode.hibernate.audit.test.model1.Model1Parent;

import com.googlecode.hibernate.audit.test.model1.Model1Person;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class Model1PackageImpl extends EPackageImpl implements Model1Package {
    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass model1ParentEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass model1ChildEClass = null;

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private EClass model1PersonEClass = null;

    /**
     * Creates an instance of the model <b>Package</b>, registered with
     * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
     * package URI value.
     * <p>Note: the correct way to create the package is via the static
     * factory method {@link #init init()}, which also performs
     * initialization of the package, or returns the registered package,
     * if one already exists.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see org.eclipse.emf.ecore.EPackage.Registry
     * @see com.googlecode.hibernate.audit.test.model1.Model1Package#eNS_URI
     * @see #init()
     * @generated
     */
    private Model1PackageImpl() {
        super(eNS_URI, Model1Factory.eINSTANCE);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private static boolean isInited = false;

    /**
     * Creates, registers, and initializes the <b>Package</b> for this
     * model, and for any others upon which it depends.  Simple
     * dependencies are satisfied by calling this method on all
     * dependent packages before doing anything else.  This method drives
     * initialization for interdependent packages directly, in parallel
     * with this package, itself.
     * <p>Of this package and its interdependencies, all packages which
     * have not yet been registered by their URI values are first created
     * and registered.  The packages are then initialized in two steps:
     * meta-model objects for all of the packages are created before any
     * are initialized, since one package's meta-model objects may refer to
     * those of another.
     * <p>Invocation of this method will not affect any packages that have
     * already been initialized.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @see #eNS_URI
     * @see #createPackageContents()
     * @see #initializePackageContents()
     * @generated
     */
    public static Model1Package init() {
        if (isInited) return (Model1Package)EPackage.Registry.INSTANCE.getEPackage(Model1Package.eNS_URI);

        // Obtain or create and register package
        Model1PackageImpl theModel1Package = (Model1PackageImpl)(EPackage.Registry.INSTANCE.getEPackage(eNS_URI) instanceof Model1PackageImpl ? EPackage.Registry.INSTANCE.getEPackage(eNS_URI) : new Model1PackageImpl());

        isInited = true;

        // Create package meta-data objects
        theModel1Package.createPackageContents();

        // Initialize created meta-data
        theModel1Package.initializePackageContents();

        // Mark meta-data to indicate it can't be changed
        theModel1Package.freeze();

        return theModel1Package;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getModel1Parent() {
        return model1ParentEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getModel1Parent_Children() {
        return (EReference)model1ParentEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getModel1Child() {
        return model1ChildEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EReference getModel1Child_Parent() {
        return (EReference)model1ChildEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EClass getModel1Person() {
        return model1PersonEClass;
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getModel1Person_Id() {
        return (EAttribute)model1PersonEClass.getEStructuralFeatures().get(0);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public EAttribute getModel1Person_Name() {
        return (EAttribute)model1PersonEClass.getEStructuralFeatures().get(1);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public Model1Factory getModel1Factory() {
        return (Model1Factory)getEFactoryInstance();
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isCreated = false;

    /**
     * Creates the meta-model objects for the package.  This method is
     * guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void createPackageContents() {
        if (isCreated) return;
        isCreated = true;

        // Create classes and their features
        model1ParentEClass = createEClass(MODEL1_PARENT);
        createEReference(model1ParentEClass, MODEL1_PARENT__CHILDREN);

        model1ChildEClass = createEClass(MODEL1_CHILD);
        createEReference(model1ChildEClass, MODEL1_CHILD__PARENT);

        model1PersonEClass = createEClass(MODEL1_PERSON);
        createEAttribute(model1PersonEClass, MODEL1_PERSON__ID);
        createEAttribute(model1PersonEClass, MODEL1_PERSON__NAME);
    }

    /**
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    private boolean isInitialized = false;

    /**
     * Complete the initialization of the package and its meta-model.  This
     * method is guarded to have no affect on any invocation but its first.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    public void initializePackageContents() {
        if (isInitialized) return;
        isInitialized = true;

        // Initialize package
        setName(eNAME);
        setNsPrefix(eNS_PREFIX);
        setNsURI(eNS_URI);

        // Create type parameters

        // Set bounds for type parameters

        // Add supertypes to classes
        model1ParentEClass.getESuperTypes().add(this.getModel1Person());
        model1ChildEClass.getESuperTypes().add(this.getModel1Person());

        // Initialize classes and features; add operations and parameters
        initEClass(model1ParentEClass, Model1Parent.class, "Model1Parent", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getModel1Parent_Children(), this.getModel1Child(), this.getModel1Child_Parent(), "children", null, 0, -1, Model1Parent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(model1ChildEClass, Model1Child.class, "Model1Child", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEReference(getModel1Child_Parent(), this.getModel1Parent(), this.getModel1Parent_Children(), "parent", null, 1, 1, Model1Child.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        initEClass(model1PersonEClass, Model1Person.class, "Model1Person", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
        initEAttribute(getModel1Person_Id(), ecorePackage.getELongObject(), "id", null, 0, 1, Model1Person.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
        initEAttribute(getModel1Person_Name(), ecorePackage.getEString(), "name", null, 0, 1, Model1Person.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

        // Create resource
        createResource(eNS_URI);

        // Create annotations
        // teneo.jpa
        createTeneoAnnotations();
    }

    /**
     * Initializes the annotations for <b>teneo.jpa</b>.
     * <!-- begin-user-doc -->
     * <!-- end-user-doc -->
     * @generated
     */
    protected void createTeneoAnnotations() {
        String source = "teneo.jpa";		
        addAnnotation
          (getModel1Parent_Children(), 
           source, 
           new String[] {
             "appinfo", "@OneToMany(mappedBy = \"parent\", indexed=false)"
           });
    }

} //Model1PackageImpl
