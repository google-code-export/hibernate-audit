package com.googlecode.hibernate.audit.test.mock.jndi;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.Name;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.Binding;
import javax.naming.NameParser;
import javax.naming.NameNotFoundException;
import java.util.Hashtable;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

/**
 * A flat MockContext that allows us to hijack JNDI for testing purposes.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockContext implements Context
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Map<String, Object> bindings;

    // Constructors --------------------------------------------------------------------------------

    public MockContext(Map<String, Object> initialJNDIContent)
    {
        bindings = Collections.synchronizedMap(new HashMap<String, Object>(initialJNDIContent));
    }

    // Context implementation ----------------------------------------------------------------------

    public Object lookup(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    /**
     * @throws NameNotFoundException is the corresponding object is not bound.
     */
    public Object lookup(String name) throws NamingException
    {
        Object result = bindings.get(name);

        if (result == null)
        {
            throw new NameNotFoundException(name + " not bound");
        }

        return result;

    }

    public void bind(Name name, Object obj) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void bind(String name, Object obj) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void rebind(Name name, Object obj) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void rebind(String name, Object obj) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void unbind(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void unbind(String name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void rename(Name oldName, Name newName) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void rename(String oldName, String newName) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NamingEnumeration<NameClassPair> list(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NamingEnumeration<NameClassPair> list(String name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NamingEnumeration<Binding> listBindings(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NamingEnumeration<Binding> listBindings(String name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void destroySubcontext(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void destroySubcontext(String name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Context createSubcontext(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Context createSubcontext(String name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object lookupLink(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object lookupLink(String name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NameParser getNameParser(Name name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NameParser getNameParser(String name) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Name composeName(Name name, Name prefix) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public String composeName(String name, String prefix) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object addToEnvironment(String propName, Object propVal) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object removeFromEnvironment(String propName) throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Hashtable<?, ?> getEnvironment() throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void close() throws NamingException
    {
        bindings.clear();
        bindings = null;
    }

    public String getNameInNamespace() throws NamingException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * The result map is thread safe.
     */
    Map<String, Object> getBindings()
    {
        return bindings;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
