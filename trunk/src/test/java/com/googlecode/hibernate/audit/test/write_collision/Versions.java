package com.googlecode.hibernate.audit.test.write_collision;

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
public class Versions
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public void put(Class c, Long entityId, Long version)
    {
        
    }

    public VersionedEntity getVersionedEntity(Class c, Serializable id)
    {
        return null;
    }

    public Object getEntity(Class c, Serializable id)
    {
        VersionedEntity ve = getVersionedEntity(c, id);

        if (ve == null)
        {
            return null;
        }

        return ve.getEntity();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
