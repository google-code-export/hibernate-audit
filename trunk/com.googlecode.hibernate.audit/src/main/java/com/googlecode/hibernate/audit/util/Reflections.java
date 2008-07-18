package com.googlecode.hibernate.audit.util;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

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
     * TODO: inefficient and incomplete, needs tests
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
     * TODO: inefficient and incomplete, needs tests
     */
    public static Object applyDelta(Object base, Object delta) throws Exception
    {
        Class c = base.getClass();
        if (!c.isInstance(delta))
        {
            throw new IllegalArgumentException(base + " and " + delta + " have different types");
        }

        Object baseCopy = deepCopy(base);

        for(Method m: c.getMethods())
        {
            String name = m.getName();

            if (!name.startsWith("set"))
            {
                continue;
            }

            String getterName = "get" + name.substring(3);
            Method getter = c.getMethod(getterName);

            Object d = getter.invoke(delta);
            m.invoke(baseCopy, d);
        }

        return baseCopy;
    }

    /**
     * TODO totally incomplete, needs proper implementation and tests
     */
    public static Object deepCopy(Object o) throws Exception
    {
        Class c = o.getClass();

        return instantiateOverridingAccessibility(c);
    }

    public static Object instantiateOverridingAccessibility(Class c) throws Exception
    {
        Constructor constructor = c.getDeclaredConstructor();

        if (!Modifier.isPublic(constructor.getModifiers()) ||
            !Modifier.isPublic(c.getModifiers()))
        {
            constructor.setAccessible(true);
        }

        return constructor.newInstance();
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
