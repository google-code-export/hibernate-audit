package com.googlecode.hibernate.audit.test.util.data;

import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.Method;
import java.io.Serializable;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$</tt>
 */
public class PImpl implements P
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Integer i;
    private Q q;

    // Constructors --------------------------------------------------------------------------------

    // P implementation ----------------------------------------------------------------------------

    public Q getQ()
    {
        return q;
    }

    public void setQ(Q q)
    {
        this.q = q;
    }

    public Integer getI()
    {
        return i;
    }

    public void setI(Integer i)
    {
        this.i = i;
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * Creates a mock HibernateProxy that allows us to test use cases encountering those.
     * @throws Exception
     */
    public P createHibernateProxyOfMyself() throws Exception
    {
        InvocationHandler h = new SyntheticHibernateProxyHandler(this);

        Class[] interfaces = new Class[]
            {
                P.class,
                HibernateProxy.class
            };

        return (P)Proxy.
            newProxyInstance(Thread.currentThread().getContextClassLoader(), interfaces, h);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class SyntheticHibernateProxyHandler implements InvocationHandler, LazyInitializer
    {
        private P delegate;

        public SyntheticHibernateProxyHandler(P delegate)
        {
            this.delegate = delegate;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable
        {
            String methodName = method.getName();

            if ("toString".equals(methodName))
            {
                return "SyntheticHibernateProxyHandler[" + delegate + "]";
            }
            else if ("getHibernateLazyInitializer".equals(methodName))
            {
                return this;
            }

            throw new RuntimeException(methodName + "(" + args + ") NOT YET IMPLEMENTED");
        }

        public void initialize() throws HibernateException
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public Serializable getIdentifier()
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public void setIdentifier(Serializable id)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public String getEntityName()
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public Class getPersistentClass()
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public boolean isUninitialized()
        {
            return false;
        }

        public void setImplementation(Object target)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public SessionImplementor getSession()
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public void setSession(SessionImplementor s) throws HibernateException
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public Object getImplementation()
        {
            return delegate;
        }

        public Object getImplementation(SessionImplementor s) throws HibernateException
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public void setUnwrap(boolean unwrap)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        public boolean isUnwrap()
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }
    }

}
