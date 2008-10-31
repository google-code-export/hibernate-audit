package com.googlecode.hibernate.audit.test.logical_group_id;

import com.googlecode.hibernate.audit.LogicalGroupIdProvider;

import java.io.Serializable;

import org.hibernate.event.EventSource;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
class ThreadBoundLogicalGroupIdProvider implements LogicalGroupIdProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private static ThreadLocal<Long> logicalGroupId;

    // Constructors --------------------------------------------------------------------------------

    ThreadBoundLogicalGroupIdProvider()
    {
       logicalGroupId = new ThreadLocal<Long>();
    }

    // LogicalGroupIdProvider implementation -------------------------------------------------------

    public Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity)
    {
        return logicalGroupId.get();
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    void bindLogicalGroupId(Long lgid)
    {
        logicalGroupId.set(lgid);
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
