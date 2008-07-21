package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.Transient;
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

    @Transient
    private AuditType memberType;

    // Constructors --------------------------------------------------------------------------------

    /**
     * Required by Hibernate.
     */
    public AuditCollectionType()
    {
    }

    /**
     * Required by Hibernate.
     */
    public AuditCollectionType(AuditType memberType)
    {
        this.memberType = memberType;
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
     * Returns the id (as Long converted to String) of the corresponding AuditType.
     * 
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    @Override
    public String valueToString(Object o)
    {
        // TODO: shaky, implemented in a hurry, review this
        // TODO: we're ignoring o and that's not alright, shows there's some problem with the logic
        return Long.toString(memberType.getId());
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
