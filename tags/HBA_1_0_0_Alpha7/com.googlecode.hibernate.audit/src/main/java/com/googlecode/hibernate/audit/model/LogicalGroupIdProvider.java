package com.googlecode.hibernate.audit.model;

import org.hibernate.event.EventSource;

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
public interface LogicalGroupIdProvider 
{
    /**
     * @return nul if it cannot figure it out.
     */
    Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity);
}
