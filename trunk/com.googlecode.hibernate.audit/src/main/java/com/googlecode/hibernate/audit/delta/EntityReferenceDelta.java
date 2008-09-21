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
public interface EntityReferenceDelta extends ScalarDelta
{
    /**
     * @return the id of referred entity.
     */
    Serializable getId();


    /**
     * @return the name of referred entity. In most cases, is the fully qualified class name of the
     *         entity class, but can also be an arbitrary string that uniquely identifies the entity
     *         type.
     */
    String getEntityName();

    Class getEntityClass();
}
