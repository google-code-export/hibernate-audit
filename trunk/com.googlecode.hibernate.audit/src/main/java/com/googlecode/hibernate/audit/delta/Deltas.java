package com.googlecode.hibernate.audit.delta;

import java.io.Serializable;
import java.util.Collection;

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

    public static CollectionDelta createCollectionDelta(String name,
                                                        String memberEntityName,
                                                        Collection<Serializable> ids)
    {
        return new CollectionDeltaImpl(name, memberEntityName, ids);
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
