package com.googlecode.hibernate.audit.delta;

import java.io.Serializable;
import java.util.Date;
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
public interface TransactionDelta
{
    /**
    * @return the id of the transaction current delta corresponds to.
     */
    Serializable getId();

    Serializable getLogicalGroupId();
    Date getTimestamp();
    String getUser();

    Set<EntityDelta> getEntityDeltas();

    /**
     * @param entityName - the entity name. In most cases is the fully qualified class name of the
     *        class that implements the entity, but it can also be an arbitrary string.
     *
     * @return null if no entity delta corresponding to an entity with the given id is found. 
     */
    EntityDelta getEntityDelta(Serializable entityId, String entityName);

}
