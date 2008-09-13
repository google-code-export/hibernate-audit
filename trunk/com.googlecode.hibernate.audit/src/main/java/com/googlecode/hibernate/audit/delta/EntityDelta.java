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

    Set<PrimitiveDelta> getPrimitiveDeltas();
    Set<CollectionDelta> getCollectionDeltas();

    /**
     * @return null if no primitive delta exists for this specific member variable.
     */
    PrimitiveDelta getPrimitiveDelta(String name);

}
