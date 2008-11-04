package com.googlecode.hibernate.audit.util.wocache;

import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
import org.hibernate.Session;
import org.hibernate.criterion.Restrictions;

import java.util.Iterator;

import com.googlecode.hibernate.audit.util.Reflections;

/**
 * TODO This class will probably go away: https://jira.novaordis.org/browse/HBA-125
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class CacheQuery<P>
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Class type;
    private Key key;
    private InstanceFactory<P> factory;

    private boolean insert;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @exception IllegalArgumentException for unconsistent name/value pairs
     */
    public CacheQuery(Class type, Object ... nameValuePairs)
    {
        this(type, true, null, nameValuePairs);
    }

    /**
     * @exception IllegalArgumentException for unconsistent name/value pairs
     */
    public CacheQuery(Class type, boolean insert, Object ... nameValuePairs)
    {
        this(type, insert, null, nameValuePairs);
    }

    /**
     * @exception IllegalArgumentException for unconsistent name/value pairs
     */
    public CacheQuery(Class type, InstanceFactory<P> factory, Object ... nameValuePairs)
    {
        this(type, true, factory, nameValuePairs);
    }

    /**
     * @param insert - specifies behavior on cache miss - if true, transactionally insert in the
     *        database, return null otherwise.
     *
     * @exception IllegalArgumentException for unconsistent name/value pairs
     */
    public CacheQuery(Class type, boolean insert, InstanceFactory<P> factory,
                      Object ... nameValuePairs)
    {
        this.key = new Key(nameValuePairs);

        if (key.isEmpty())
        {
            throw new IllegalArgumentException("no name/value pairs specified");
        }

        this.type = type;
        this.factory = factory;
        this.insert = insert;
    }

    // Public --------------------------------------------------------------------------------------

    public Class getType()
    {
        return type;
    }

    public Key getKey()
    {
        return key;
    }

    public Criteria generateCriteria(StatelessSession s)
    {
        Criteria c = s.createCriteria(type);
        fillCriteria(c);
        return c;
    }

    public Criteria generateCriteria(Session s)
    {
        Criteria c = s.createCriteria(type);
        fillCriteria(c);
        return c;
    }

    public P createMatchingInstance() throws Exception
    {
        if (factory != null)
        {
            return factory.createInstance(key);
        }

        Object o = type.newInstance();

        for(Iterator<String> i = key.names(); i.hasNext(); )
        {
            String name = i.next();
            Reflections.mutate(o, name, key.getValue(name));
        }
        
        return (P)o;
    }

    public boolean isInsert()
    {
        return insert;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private void fillCriteria(Criteria c)
    {
        for(Iterator<String> i = key.names(); i.hasNext(); )
        {
            String name = i.next();
            c.add(Restrictions.eq(name, key.getValue(name)));
        }
    }

    // Inner classes -------------------------------------------------------------------------------
}
