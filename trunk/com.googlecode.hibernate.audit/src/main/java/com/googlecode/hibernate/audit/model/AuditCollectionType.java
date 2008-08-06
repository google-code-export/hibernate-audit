package com.googlecode.hibernate.audit.model;

import org.hibernate.Query;
import org.hibernate.Session;

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

    /**
     * Returns a persistent instance of given type from the database. If "create" is set to false
     * and the type does not exist in the database, the method returns null. If "create" is set to
     * true and the type does not exist in the database, it is persisted, and then returned.
     *
     * @param session - the hibernate session to be used to interact with the database. It is
     *        assumed that a transaction is already started, and it will be committed outside
     *        the scope of this method.
     *
     * @return the persisted type (or null)
     */
    public static AuditCollectionType getInstanceFromDatabase(Class collectionClass,
                                                              Class memberClass,
                                                              boolean create,
                                                              Session session)
    {
        checkTransaction(session);

        String qs =
            "from AuditCollectionType as e where " +
            "e.className  = :memberClass and e.collectionClassName = :collectionClassName";

        Query q = session.createQuery(qs);
        q.setString("memberClass", memberClass.getName());
        q.setString("collectionClassName", collectionClass.getName());

        AuditCollectionType persistedType = (AuditCollectionType)q.uniqueResult();

        if (persistedType != null || !create)
        {
            return persistedType;
        }

        persistedType = new AuditCollectionType(collectionClass, memberClass);
        session.save(persistedType);
        return persistedType;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Column(name = "COLLECTION_CLASS_NAME")
    private String collectionClassName;

    @Transient
    private Class collectionClassInstance;

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
               "<" + getClassName() + ">]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
