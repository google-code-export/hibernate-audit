package com.googlecode.hibernate.audit.test.logical_group_id;

import com.googlecode.hibernate.audit.LogicalGroupProvider;
import com.googlecode.hibernate.audit.LogicalGroup;

import java.io.Serializable;

import org.hibernate.event.EventSource;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
class ThreadBoundLogicalGroupProvider implements LogicalGroupProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private static ThreadLocal<LogicalGroup> logicalGroup;

    // Constructors --------------------------------------------------------------------------------

    ThreadBoundLogicalGroupProvider()
    {
       logicalGroup = new ThreadLocal<LogicalGroup>();
    }

    // LogicalGroupProvider implementation -------------------------------------------------------

    public LogicalGroup getLogicalGroup(EventSource es, Serializable id, Object entity)
    {
        return logicalGroup.get();
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    void bindLogicalGroup(LogicalGroup lg)
    {
        logicalGroup.set(lg);
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
