package com.googlecode.hibernate.audit.delta;

import java.io.Serializable;
import java.util.Set;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface EntityDelta
{
    /**
    * @return the id of the entity current current delta corresponds to.
     */
    Serializable getId();

    /**
     * The name of the entity this delta corresponds to. In most cases is the fully qualified class
     * name of the class implementing the entity, but it can also be an arbitrary string.
     */
    String getEntityName();

    Set<ScalarDelta> getScalarDeltas();
    Set<CollectionDelta> getCollectionDeltas();

    /**
     * @return null if no scalar delta exists for this specific member variable.
     */
    ScalarDelta getScalarDelta(String name);

    /**
     * @return null if no primitive delta exists for this specific member variable.
     */
    PrimitiveDelta getPrimitiveDelta(String name);

    /**
     * @return null if no entity feference delta exists for this specific member variable.
     */
    EntityReferenceDelta getEntityReferenceDelta(String name);
}
