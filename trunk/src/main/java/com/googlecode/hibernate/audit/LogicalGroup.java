package com.googlecode.hibernate.audit;

import java.io.Serializable;

/**
 * Based on our concrete use cases, all logical groups encountered so far were in a way or another
 * related to a domain model entity, so we decided to infer the interface based on this conclusion.
 * Hence, a logical group is currently defined by a Serializable id and the entity name of the
 * entity that defines the group.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface LogicalGroup
{
    /**
     * We went for the long name (getExternalId() instead of getId()) to make easier for
     * implementing classes to avoid breaking the usual persistent class idiom: getId() returns
     * the database id, which, in this case, is not the logical group id. 
     */
    Serializable getExternalId();
    String getDefiningEntityName();

}