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

    /**
     * @param value can be null, in which case the delta is equivalent with "remove previous value".
     *        If value is null, returned PrimitiveDelta instance returns a null type.
     */
    public static <T> PrimitiveDelta<T> createPrimitiveDelta(String name, T value)
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
