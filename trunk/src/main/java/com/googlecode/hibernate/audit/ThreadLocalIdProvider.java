package com.googlecode.hibernate.audit;

import org.hibernate.event.EventSource;

import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * A logical group id provider that extracts the logical group id from threadlocal.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class ThreadLocalIdProvider implements LogicalGroupIdProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public ThreadLocalIdProvider() throws Exception
    {
    }

    // LogicalGroupIdProvider implementation -------------------------------------------------------

    public Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
