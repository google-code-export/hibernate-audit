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

    /**
     * A string that mocks the presence of named or positional parameters.
     */
    static Query newInstance(String hql)
    {
        return (Query)Proxy.newProxyInstance(MockQuery.class.getClassLoader(),
                                             new Class[] { Query.class,  InvocationHistory.class},
                                             new MockQuery(hql));
    }

    // Attributes ----------------------------------------------------------------------------------

    private String hql;

    private List<InvocationRecord> history;

    // Constructors --------------------------------------------------------------------------------

    MockQuery(String hql)
    {
        this.hql = hql;
        history = new ArrayList<InvocationRecord>();
    }

    // InvocationHandler implementation ------------------------------------------------------------

    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
    {
        String methodName = method.getName();

        if ("getHistory".equals(methodName))
        {
            return history;
        }
        else if ("getQueryString".equals(methodName))
        {
            return hql;
        }
        else if ("toString".equals(methodName))
        {
            return "MockQuery[" + hql + "]";
        }

        // record in history everyting else
        history.add(new InvocationRecord(method.getName(), args));
        return null;
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
