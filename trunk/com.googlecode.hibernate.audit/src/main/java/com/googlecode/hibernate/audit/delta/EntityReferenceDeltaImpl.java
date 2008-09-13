package com.googlecode.hibernate.audit.delta;

import java.io.Serializable;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
class EntityReferenceDeltaImpl extends MemberVariableDeltaSupport implements EntityReferenceDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Serializable id;
    private String entityName;

    // Constructors --------------------------------------------------------------------------------

    EntityReferenceDeltaImpl(String name, Serializable id, String entityName)
    {
        setName(name);
        this.id = id;
        this.entityName = entityName;
    }

    // ScalarDelta implementation ------------------------------------------------------------------

    public boolean isEntityReference()
    {
        return true;
    }

    public boolean isPrimitive()
    {
        return false;
    }

    public Serializable getId()
    {
        return id;
    }

    public String getEntityName()
    {
        return entityName;
    }

    // Public --------------------------------------------------------------------------------------

    // MemberVariableDelta implementation ----------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
