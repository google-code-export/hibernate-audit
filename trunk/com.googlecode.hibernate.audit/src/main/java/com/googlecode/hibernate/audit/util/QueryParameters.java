package com.googlecode.hibernate.audit.util;

import org.hibernate.Query;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.HashMap;

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
    private static final Map<Class, Method> mutators;

    static
    {
        mutators = new HashMap<Class, Method>();

        for(Method m: Query.class.getMethods())
        {
            if (m.getName().startsWith("set") &&
                m.getParameterTypes().length == 2 &&
                m.getParameterTypes()[0].equals(Integer.TYPE))
            {
                // a method of kind "setSomething(int, Something)"
                Class c = m.getParameterTypes()[1];
                mutators.put(c, m);
            }
        }
    }

    // Static --------------------------------------------------------------------------------------

    /**
     * @exception IllegalStateException or other runtime exceptions on gross violations.
     */
    public static void fill(Query q, Object... params)
    {
        int position = 0;
        for(Object p: params)
        {
            Class c = p.getClass();
            Method mutator = mutators.get(c);

            if (mutator == null)
            {
                throw new IllegalStateException(
                    "found no mutator to set a " + c.getName() + " instance on query");
            }

            try
            {
                mutator.invoke(q, position, p);
            }
            catch(IllegalAccessException e)
            {
                throw new IllegalStateException("underlying method inacessible", e);
            }
            catch(InvocationTargetException e)
            {
                throw new IllegalStateException("mutator invocation on query failed", e);
            }

            position ++;
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
