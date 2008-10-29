package com.googlecode.hibernate.audit.model;

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
            throw new IllegalArgumentException(this + " cannot resolve class " + idClassName, e);
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
            throw new IllegalStateException(this + " cannot figure out its id type");
        }

        if (!idClassInstance.isInstance(o))
        {
            throw new IllegalArgumentException(
                this + " cannot accept " + o + " as a value for its " +
                idClassInstance.getName() + " id");
        }

        try
        {
            Method m = idClassInstance.getMethod("toString");
            return (String)m.invoke(o);
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException(
                this + " failed to invoke " + idClassInstance.getName() + "'s toString()", e);
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
