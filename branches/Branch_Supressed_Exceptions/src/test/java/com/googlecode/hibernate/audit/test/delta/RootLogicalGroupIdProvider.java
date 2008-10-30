package com.googlecode.hibernate.audit.test.delta;

import com.googlecode.hibernate.audit.LogicalGroupIdProvider;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.hibernate.event.EventSource;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class RootLogicalGroupIdProvider implements LogicalGroupIdProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Object root;
    private Method getId;

    // Constructors --------------------------------------------------------------------------------

    public RootLogicalGroupIdProvider(Object root) throws Exception
    {
        this.root = root;
        getId = root.getClass().getMethod("getId");
    }

    // LogicalGroupIdProvider implementation -------------------------------------------------------

    public Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity)
    {
        try
        {
            return (Serializable)getId.invoke(root);
        }
        catch(Exception e)
        {
            throw new IllegalStateException("shouldn't have gotten here", e);
        }
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
