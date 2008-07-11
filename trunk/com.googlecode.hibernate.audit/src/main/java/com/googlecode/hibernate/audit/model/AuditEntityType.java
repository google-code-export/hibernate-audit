package com.googlecode.hibernate.audit.model;

import javax.persistence.Transient;
import javax.persistence.Entity;

/**
 * This subclass only adds behavior, not state, so we don't need to employ any inheritance mapping
 * strategies, this class doesn not exist from the persistence point of view.
 *
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
public class AuditEntityType extends AuditType
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Transient
    private Class idClass;

    // Constructors --------------------------------------------------------------------------------

    /**
     * The only way to publicly creating an instance of this class should be via a constructor
     * whose signature requires the entity id type. This is required for consistency checks, and
     * failure to provide it could lead to corrupted state.
     *
     * @param idClass - the type of the entity id.
     */
    public AuditEntityType(Class idClass)
    {
        this.idClass = idClass;
    }

    /**
     * Convenience copy construcotr.
     */
    public AuditEntityType(Class idClass, AuditType source)
    {
        this.idClass = idClass;
        setId(source.getId());
        setClassName(source.getClassName());
        setLabel(source.getLabel());
    }

    // Public --------------------------------------------------------------------------------------

    public Class getIdClass()
    {
        return idClass;
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
        getClassInstance();

        if (idClass == null)
        {
            throw new IllegalStateException("type of the entity's id is unknown");
        }

        if (!idClass.isInstance(o))
        {
            throw new IllegalArgumentException("the argument cannot be an id for " + c.getName());
        }

        // TODO this should be handled statically, I won't have to need to create a new instance
        AuditType tool = new AuditType(idClass);
        return tool.valueToString(o);
    }

    /**
     * Based on the entity id, we need to recreate the complete state of the entity.
     *
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    @Override
    public Object stringToValue(String s)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
