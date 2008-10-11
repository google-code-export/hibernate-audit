package com.googlecode.hibernate.audit.util.wocache;

import org.hibernate.Criteria;
import org.hibernate.StatelessSession;
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

    // Constructors --------------------------------------------------------------------------------

    /**
     * @exception IllegalArgumentException for unconsistent name/value pairs
     */
    public CacheQuery(Class type, Object ... nameValuePairs)
    {
        this.type = type;
        this.key = new Key(nameValuePairs);

        if (key.isEmpty())
        {
            throw new IllegalArgumentException("no name/value pairs specified");
        }
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
        for(Iterator<String> i = key.names(); i.hasNext(); )
        {
            String name = i.next();
            c.add(Restrictions.eq(name, key.getValue(name)));
        }

        return c;
    }

    public P createInstanceMatchingQuery() throws Exception
    {
        Object o = type.newInstance();

        for(Iterator<String> i = key.names(); i.hasNext(); )
        {
            String name = i.next();
            Reflections.mutate(o, name, key.getValue(name));
        }
        
        return (P)o;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
