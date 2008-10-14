package com.googlecode.hibernate.audit.model;

import org.hibernate.Session;
import org.hibernate.Query;

import javax.persistence.Transient;
import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Column;
import java.io.Serializable;
import java.lang.reflect.Method;

/**
 * @see AuditType
 * @see AuditCollectionType
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
@DiscriminatorValue("E")
public class AuditEntityType extends AuditType
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    /**
     * DO NOT access from outside package and DO NOT relax the access restrictions!
     *
     * Returns a persistent instance of given type from the database. If "create" is set to false
     * and the type does not exist in the database, the method returns null. If "create" is set to
     * true and the type does not exist in the database, it is persisted, and then returned.
     *
     * @param entityClass - the entity type.
     * @param idClass - disregarded while querying, only used if creation is necessary.
     *
     * @param session - the hibernate session to be used to interact with the database. It is
     *        assumed that a transaction is already started, and it will be committed outside
     *        the scope of this method.
     *
     * @return the persisted type (or null)
     */
    @Deprecated
    static AuditEntityType getInstanceFromDatabase(Class entityClass,
                                                   Class idClass,
                                                   boolean create,
                                                   Session session)
    {
        checkTransaction(session);

        String qs = "from AuditEntityType as e where e.className  = :className";
        Query q = session.createQuery(qs);
        q.setString("className", entityClass.getName());

        AuditEntityType persistedType = (AuditEntityType)q.uniqueResult();

        if (persistedType != null || !create)
        {
            return persistedType;
        }

        persistedType = new AuditEntityType(idClass, entityClass);
        session.save(persistedType);
        return persistedType;
    }

    // Attributes ----------------------------------------------------------------------------------

    @Column(name = "ENTITY_ID_CLASS_NAME")
    private String idClassName;

    @Transient
    private Class idClassInstance;

    @Transient
    private String entityName;

    // Constructors --------------------------------------------------------------------------------

    /**
     * Required by Hibernate.
     */
    AuditEntityType()
    {
    }

    /**
     * The only way to publicly creating an instance of this class should be via a constructor
     * whose signature requires the entity id type. This is required for consistency checks, and
     * failure to provide it could lead to corrupted state.
     *
     * @param idClassInstance - the type of the entity id.
     */
    public AuditEntityType(Class idClassInstance, Class entityClass)
    {
        super(entityClass);
        this.idClassInstance = idClassInstance;

        if (idClassInstance != null)
        {
            this.idClassName = idClassInstance.getName();
        }
    }

    /**
     * Convenience copy construcotr.
     */
    public AuditEntityType(Class idClassInstance, AuditType source)
    {
        this.idClassInstance = idClassInstance;
        setId(source.getId());
        setClassName(source.getClassName());
        setLabel(source.getLabel());
    }

    // Public --------------------------------------------------------------------------------------

    public String getIdClassName()
    {
        return idClassName;
    }

    public void setIdClassName(String idClassName)
    {
        this.idClassName = idClassName;
    }

    public Class getIdClassInstance()
    {
        if (idClassInstance != null)
        {
            return idClassInstance;
        }

        if (idClassName == null)
        {
            return null;
        }
        try
        {
            idClassInstance = Class.forName(idClassName);
        }
        catch(ClassNotFoundException e)
        {
            throw new IllegalArgumentException("cannot resolve class " + idClassName, e);
        }

        return idClassInstance;
    }

    public String getEntityName()
    {
        // TODO broken implementaion, it only works with FQCN, not arbitrary entity names
        if (entityName == null)
        {
            inferEntityName();
        }
        return entityName;
    }

    @Override
    public boolean isPrimitiveType()
    {
        return false;
    }

    @Override
    public boolean isEntityType()
    {
        return true;
    }

    /**
     * Because this AuditType represents an Hibernate entity, then the entity ID is accepted as
     * "value".
     *
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    @Override
    public String valueToString(Object o)
    {
        getIdClassInstance();

        if (idClassInstance == null)
        {
            throw new IllegalStateException("type of the entity's id is unknown");
        }

        if (!idClassInstance.isInstance(o))
        {
            throw new IllegalArgumentException("the argument cannot be an id for " +
                                               classInstance.getName());
        }

        try
        {
            Method m = idClassInstance.getMethod("toString");
            return (String)m.invoke(o);
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException(
                "failed to invoke " + idClassInstance.getName() + "'s toString()", e);
        }
    }

    /**
     * Based on the entity id, we need to recreate the complete state of the entity.
     *
     * @exception NumberFormatException if the conversion to number failed.
     * @exception IllegalArgumentException if the conversion fails for some other reason.
     */
    @Override
    public Serializable stringToValue(String s)
    {
        if (Long.class.equals(getIdClassInstance()))
        {
            return Long.parseLong(s);
        }
        else
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }
    }

    @Override
    public String toString()
    {
        return "EntityType[" +
               (getId() == null ? "TRANSIENT" : getId()) + "][" + getClassName() + ", " +
               getIdClassName() + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private void inferEntityName()
    {
        Class c = getClassInstance();

        if (c == null)
        {
            entityName = null;
        }

        entityName = c.getName();
    }

    // Inner classes -------------------------------------------------------------------------------
}
