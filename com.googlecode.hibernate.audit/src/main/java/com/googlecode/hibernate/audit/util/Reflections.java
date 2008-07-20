package com.googlecode.hibernate.audit.util;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;

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

    private static final Logger log = Logger.getLogger(Reflections.class);

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
        String methodName =
            "set" + Character.toUpperCase(memberName.charAt(0)) + memberName.substring(1);

        Class argumentType = value.getClass();

        Method mutator = null;

        while(argumentType != Object.class)
        {
            try
            {
                mutator = o.getClass().getMethod(methodName, argumentType);

                if (mutator != null)
                {
                    break;
                }
            }
            catch(Exception e)
            {
                // ok, it's part of the logic
            }

            argumentType = argumentType.getSuperclass();
        }

        if (mutator == null)
        {
            throw new NoSuchMethodException(
                "cannot find mutator " + methodName + "(...) for " + value.getClass().getName() +
                " or any of its superclasses");
        }

        mutator.invoke(o, value);
    }

    /**
     * TODO: inefficient and incomplete, needs tests
     */
    public static void mutateCollection(Object o, String memberName, Collection value)
        throws NoSuchMethodException,
               IllegalAccessException,
               IllegalArgumentException,
               InvocationTargetException
    {
        String methodName =
            "set" + Character.toUpperCase(memberName.charAt(0)) + memberName.substring(1);

        boolean sucessful = false;

        Method[] methods = o.getClass().getMethods();

        for(Method m: methods)
        {
            if (m.getName().equals(methodName))
            {
                // try to invoke

                try
                {
                    m.invoke(o, value);
                    sucessful = true;
                    break;
                }
                catch(Exception e)
                {
                    log.debug("failed to invoke", e);
                }
            }
        }

        if (!sucessful)
        {
            throw new NoSuchMethodException(
                "cannot find mutator " + methodName + "(...) for " + value.getClass().getName());
        }
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

            name = name.substring(3);

            String getterName = "get" + name;

            Method getter = null;

            try
            {
                getter = c.getMethod(getterName);
            }
            catch(Exception e)
            {
                // ok, keep trying
            }

            if (getter == null)
            {
                getterName = "is" + name;

                try
                {
                    getter = c.getMethod(getterName);
                }
                catch(Exception e)
                {
                    // ok, keep trying
                }
            }

            if (getter == null)
            {
                throw new NoSuchMethodException(
                    "cannot find set...() or is...() accessor for " + name);
            }

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
