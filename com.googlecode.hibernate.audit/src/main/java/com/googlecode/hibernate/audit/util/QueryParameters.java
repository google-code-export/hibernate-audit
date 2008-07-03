package com.googlecode.hibernate.audit.util;

import org.hibernate.Query;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.StringTokenizer;

/**
 * A simple static utility class that makes working with parameterized queries easier.
 *
 * TODO Only partially tested, not guaranteed to work with all possible set...() methods on Query.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class QueryParameters
{
    // Constants -----------------------------------------------------------------------------------

    // Map<argument type - method instance>
    private static final Map<Class, Method> namedParameterMutators;
    private static final Map<Class, Method> positionalParameterMutators;

    static
    {
        namedParameterMutators = new HashMap<Class, Method>();
        positionalParameterMutators = new HashMap<Class, Method>();

        for(Method m: Query.class.getMethods())
        {
            if (m.getName().startsWith("set") &&
                m.getParameterTypes().length == 2)
            {
                Class c = m.getParameterTypes()[1];

                if (m.getParameterTypes()[0].equals(Integer.TYPE))
                {
                    // a method of kind "setSomething(int, Something)", positional mutator
                    positionalParameterMutators.put(c, m);
                }
                else if (m.getParameterTypes()[0].equals(String.class))
                {
                    // a method of kind "setSomething(String, Something)", named parameter mutator
                    namedParameterMutators.put(c, m);
                }
            }
        }
    }

    // Static --------------------------------------------------------------------------------------

    /**
     * @exception IllegalStateException or other runtime exceptions on gross violations.
     */
    public static void fill(Query q, Object... args)
    {
        Iterator<QueryParameter> pari = extractParameters(q.getQueryString()).iterator();
        
        for(Object a: args)
        {
            if (!pari.hasNext())
            {
                throw new IllegalStateException(
                    "Argument " + a + " does not have a corresponding query parameter");
            }

            QueryParameter p = pari.next();
            Class c = a.getClass();
            Method mutator = null;

            if (p.isNamed())
            {
                mutator = namedParameterMutators.get(c);

                if (mutator == null)
                {
                    throw new IllegalStateException("found no named parameter mutator to set a " +
                                                    c.getName() + " instance on query");
                }
            }
            else
            {
                mutator = positionalParameterMutators.get(c);

                if (mutator == null)
                {
                    throw new IllegalStateException("found no positional parameter mutator to " +
                                                    "set a " + c.getName() + " instance on query");
                }
            }

            try
            {
                mutator.invoke(q, p.isNamed() ? p.getName() : p.getPosition(), a);
            }
            catch(IllegalAccessException e)
            {
                throw new IllegalStateException("underlying method inacessible", e);
            }
            catch(InvocationTargetException e)
            {
                throw new IllegalStateException("mutator invocation on query failed", e);
            }
        }
    }

    /**
     * Look for named parameters (:something) or JDBC-style (?) positional parameters.
     */
    public static List<QueryParameter> extractParameters(String hql)
    {
        List<QueryParameter> pars = new ArrayList<QueryParameter>();

        boolean namedExpected = false;
        int position = 0;
        for(StringTokenizer st = new StringTokenizer(hql, ":? \t", true); st.hasMoreTokens(); )
        {
            String tok = st.nextToken();

            if (namedExpected)
            {
                pars.add(new QueryParameter(position ++, tok));
                namedExpected = false;
            }

            if (":".equals(tok))
            {
                namedExpected = true;
            }
            else if ("?".equals(tok))
            {
                pars.add(new QueryParameter(position ++, null));
            }
        }

        return pars;
    }

//    Tests run: 1, Failures: 1, Errors: 0, Skipped: 0, Time elapsed: 131.375 sec <<< FAILURE!
//    testSingleInsert(com.googlecode.hibernate.audit.test.post_insert.PostInsertTest)  Time elapsed: 130.672 sec  <<< FAILURE!
//    java.lang.IllegalStateException: found no named parameter mutator to set a com.googlecode.hibernate.audit.model.AuditEvent instance on query
//        at com.googlecode.hibernate.audit.util.QueryParameters.fill(QueryParameters.java:88)
//        at com.googlecode.hibernate.audit.HibernateAudit.doQuery(HibernateAudit.java:361)
//        at com.googlecode.hibernate.audit.HibernateAudit.query(HibernateAudit.java:120)
//        at com.googlecode.hibernate.audit.test.post_insert.PostInsertTest.testSingleInsert(PostInsertTest.java:86)

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
    
}
