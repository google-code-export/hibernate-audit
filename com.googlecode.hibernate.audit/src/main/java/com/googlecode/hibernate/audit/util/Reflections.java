package com.googlecode.hibernate.audit.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Reflections
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    /**
     * TODO: inefficient, can be optimized.
     */
    public static void mutate(Object o, String memberName, Object value)
        throws NoSuchMethodException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException
    {
        String methodName = Character.toUpperCase(memberName.charAt(0)) + memberName.substring(1);
        methodName = "set" + methodName;

        Method mutator = o.getClass().getMethod(methodName, value.getClass());
        mutator.invoke(o, value);
    }

    /**
     * TODO: inefficient, can be optimized
     */
    public static void applyDelta(Object base, Object delta) throws Exception
    {
        Class c = base.getClass();
        if (!c.isInstance(delta))
        {
            throw new IllegalArgumentException(base + " and " + delta + " have different types");
        }

        for(Method m: c.getMethods())
        {
            String name = m.getName();

            if (!name.startsWith("set"))
            {
                continue;
            }

            String getterName = "get" + name.substring(3);
            Method getter = c.getMethod(getterName);

            Object partialDelta = getter.invoke(delta);
            m.invoke(base, partialDelta);
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
