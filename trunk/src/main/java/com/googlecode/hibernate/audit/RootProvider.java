package com.googlecode.hibernate.audit;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.hibernate.event.EventSource;

/**
 * A logical group provider that extracts the id from a "root" object and provides it to all
 * transactions associated with that root object.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class RootProvider implements LogicalGroupProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Object root;
    private Class rootClass;
    private Method getId;

    // Constructors --------------------------------------------------------------------------------

    public RootProvider(Object root) throws Exception
    {
        this(root.getClass());
        getId = root.getClass().getMethod("getId");
        this.root = root;
    }

    public RootProvider(Class rootClass) throws Exception
    {
        this.rootClass = rootClass;
        getId = rootClass.getMethod("getId");
    }

    // LogicalGroupProvider implementation -------------------------------------------------------

    public LogicalGroup getLogicalGroup(EventSource es, Serializable id, Object entity)
    {
        try
        {
            return new LogicalGroupImpl((Long)getId.invoke(root), rootClass.getName());
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
