package com.googlecode.hibernate.audit.delta;

import java.util.Map;
import java.io.Serializable;

/**
 * Encapsulates the set of changes applied to entity state during a transaction.
 *
 * An EntityDelta instance contains complete information allowing to recreate the state of the
 * entity at the end of the transaction, given that we have access to the state of the entity as
 * persisted just before the transaction started.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public class EntityDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // the entity id
    private Serializable id;

    private Map<String, PrimitiveDelta> primitiveDeltas;
    private Map<String, CollectionDelta> collectionDeltas;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    /**
     * The corresponding entity's id.
     */
    public Serializable getId()
    {
        return id;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
