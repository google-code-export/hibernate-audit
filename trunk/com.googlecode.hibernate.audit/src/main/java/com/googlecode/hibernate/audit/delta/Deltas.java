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
public class Deltas
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static <T> PrimitiveDeltaImpl<T> createPrimitiveDelta(String name, T value)
    {
        return new PrimitiveDeltaImpl<T>(name, value);
    }

    public static EntityReferenceDelta createEntityReferenceDelta(String name,
                                                                  Serializable id,
                                                                  String entityName)
    {
        return new EntityReferenceDeltaImpl(name, id, entityName);
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
