package com.googlecode.hibernate.audit;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.hibernate.event.EventSource;

/**
 * A logical group id provider that extracts the id from a "root" object and provides it to all
 * transactions associated with that root object.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class RootIdProvider implements LogicalGroupIdProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Object root;
    private Class rootClass;
    private Method getId;

    // Constructors --------------------------------------------------------------------------------

    public RootIdProvider(Object root) throws Exception
    {
        this(root.getClass());
        getId = root.getClass().getMethod("getId");
    }

    public RootIdProvider(Class rootClass) throws Exception
    {
        this.rootClass = rootClass;
        getId = rootClass.getMethod("getId");
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
            throw new IllegalStateException("cannot obtain root id from " + root, e);
        }
    }

    // Public --------------------------------------------------------------------------------------

    public Object getRoot()
    {
        return root;
    }

    public void setRoot(Object root)
    {
        if (!rootClass.isInstance(root))
        {
            throw new IllegalArgumentException(
                root + " is not a " + rootClass.getName() + " instance");
        }

        this.root = root;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
