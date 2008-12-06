package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Column;
import java.io.Serializable;
import java.util.Collection;

/**
 * @see AuditType
 * @see AuditEntityType
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@DiscriminatorValue("C")
public class AuditCollectionType extends AuditType
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Column(name = "COLLECTION_CLASS_NM")
    private String collectionClassName;

    @Transient
    private Class collectionClassInstance;

    @Transient
    private String memberEntityName;

    // Constructors --------------------------------------------------------------------------------

    /**
     * Required by Hibernate.
     */
    public AuditCollectionType()
    {
    }

    public AuditCollectionType(Class collectionClass, Class memberClass)
    {
        super(memberClass);

        if (!Collection.class.isAssignableFrom(collectionClass))
        {
            throw new IllegalArgumentException(collectionClass + " is not a Collection type");
        }

        this.collectionClassInstance = collectionClass;
        this.collectionClassName = collectionClass.getName();
    }

    // Public --------------------------------------------------------------------------------------

    public String getCollectionClassName()
    {
        return collectionClassName;
    }

    public void setCollectionClassName(String s)
    {
        this.collectionClassName = s;
    }

    public Class getCollectionClassInstance()
    {
        if (collectionClassInstance != null)
        {
            return collectionClassInstance;
        }

        if (collectionClassName == null)
        {
            return null;
        }
        try
        {
            collectionClassInstance = Class.forName(collectionClassName);
        }
        catch(ClassNotFoundException e)
        {
            throw new IllegalArgumentException("cannot resolve class " + collectionClassName, e);
        }

        return collectionClassInstance;
    }

    public String getMemberEntityName()
    {
        // TODO broken implementaion, it only works with FQCN, not arbitrary entity names
        if (memberEntityName == null)
        {
            inferMemberEntityName();
        }
        return memberEntityName;
    }

    @Override
    public boolean isPrimitiveType()
    {
        return false;
    }

    @Override
    public boolean isCollectionType()
    {
        return true;
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    @Override
    public String valueToString(Object o)
    {
        return null;
    }

    @Override
    public Serializable stringToValue(String s)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    @Override
    public String toString()
    {
        return "CollectionType[" +
               (getId() == null ? "TRANSIENT" : getId()) + "][" + getCollectionClassName() +
               "<" + getClassName() + ">]@" + Integer.toHexString(System.identityHashCode(this));
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private void inferMemberEntityName()
    {
        Class c = getClassInstance();

        if (c == null)
        {
            memberEntityName = null;
        }

        memberEntityName = c.getName();
    }
}
