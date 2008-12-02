package com.googlecode.hibernate.audit;

import org.hibernate.event.EventSource;

import java.io.Serializable;

/**
 * A logical group provider that extracts the logical group id from threadlocal.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class ThreadLocalLogicalGroupProvider implements LogicalGroupProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public ThreadLocalLogicalGroupProvider() throws Exception
    {
    }

    // LogicalGroupProvider implementation -------------------------------------------------------

    public LogicalGroup getLogicalGroup(EventSource es, Serializable id, Object entity)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
