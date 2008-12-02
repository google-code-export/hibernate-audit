package com.googlecode.hibernate.audit;

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
public interface LogicalGroupProvider
{
    /**
     * @return nul if it cannot figure it out.
     */
    LogicalGroup getLogicalGroup(EventSource es, Serializable id, Object entity);
}
