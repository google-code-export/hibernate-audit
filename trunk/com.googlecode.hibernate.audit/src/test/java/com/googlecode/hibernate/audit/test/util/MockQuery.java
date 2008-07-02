package com.googlecode.hibernate.audit.test.util;

import org.hibernate.Query;

import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ArrayList;

/**
 * A mock query implementation that keeps a history of invocations into it.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
class MockQuery implements InvocationHandler
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    static Query newInstance()
    {
        return (Query)Proxy.newProxyInstance(MockQuery.class.getClassLoader(),
                                             new Class[] { Query.class,  InvocationHistory.class},
                                             new MockQuery());
    }

    // Attributes ----------------------------------------------------------------------------------

    private List<InvocationRecord> history;

    // Constructors --------------------------------------------------------------------------------

    MockQuery()
    {
        history = new ArrayList<InvocationRecord>();
    }

    // InvocationHandler implementation ------------------------------------------------------------

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        if ("getHistory".equals(method.getName()))
        {
            return history;
        }

        history.add(new InvocationRecord(method.getName(), args));
        return null;
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
