package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.Transient;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Column;
import java.io.Serializable;

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

    @Column(name = "COLLECTION_CLASS_NAME")
    private String collectionClassName;

    @Transient
    private Class collectionClass;

    // Constructors --------------------------------------------------------------------------------

    /**
     * Required by Hibernate.
     */
    public AuditCollectionType()
    {
    }

    /**
     * Only for use by classes of this package, do not expose publicly.
     */
    AuditCollectionType(Class collectionClass, Class memberClass)
    {
        super(memberClass);
        this.collectionClass = collectionClass;
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
        if (collectionClass != null)
        {
            return collectionClass;
        }

        if (collectionClassName == null)
        {
            return null;
        }
        try
        {
            collectionClass = Class.forName(collectionClassName);
        }
        catch(ClassNotFoundException e)
        {
            throw new IllegalArgumentException("cannot resolve class " + collectionClassName, e);
        }

        return collectionClass;

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
     * Returns the id (as Long converted to String) of the corresponding AuditType.
     * 
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    @Override
    public String valueToString(Object o)
    {
        // TODO: shaky, implemented in a hurry, review this
        // TODO: we're ignoring o and that's not alright, shows there's some problem with the logic
        throw new RuntimeException("NOT YET IMPLEMENTED");
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
               "<" + getClassName() + ">]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
