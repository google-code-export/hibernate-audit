package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import java.io.Serializable;

/**
 * This subclass only adds behavior, not state, so we don't need to employ any inheritance mapping
 * strategies, this class doesn not exist from the persistence point of view.
 *
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
public class AuditCollectionType extends AuditType
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    /**
     * Required by Hibernate.
     */
    public AuditCollectionType()
    {
    }

    // Public --------------------------------------------------------------------------------------

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
        // TODO: obviously, this can't stay like that
        return "COLLECTION - NOT YET IMPLEMENTED";
    }

    /**
     */
    @Override
    public Serializable stringToValue(String s)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
