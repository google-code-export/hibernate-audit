package com.googlecode.hibernate.audit.util;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Iterator;

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
        String methodNameRoot =
            Character.toUpperCase(memberName.charAt(0)) + memberName.substring(1);
        String setMethodName = "set" + methodNameRoot;

        if (value.isEmpty())
        {
            // see why this is happening
            // TODO https://jira.novaordis.org/browse/HBA-57
            return;
        }

        boolean successful = false;

        Method[] methods = o.getClass().getMethods();

        for(Method m: methods)
        {
            if (!m.getName().equals(setMethodName))
            {
                continue;
            }

            // try to invoke
            try
            {
                m.invoke(o, value);
                successful = true;
                break;
            }
            catch(Exception e)
            {
                log.debug("failed to invoke", e);
            }
        }

        // try with "add" + progressive reverse scan of methodNameRoot

        Class memberType = null;

        while(methodNameRoot.length() > 0)
        {
            Method addMethod = null;
            Iterator memberIterator = null;

            String addMethodNamePrefix = "add" + methodNameRoot;

            outer: for(Method m: methods)
            {
                if (!m.getName().startsWith(addMethodNamePrefix))
                {
                    continue;
                }

                for(memberIterator = value.iterator(); memberIterator.hasNext(); )
                {
                    Object member = memberIterator.next();

                    if (memberType != null && !member.getClass().equals(memberType))
                    {
                        throw new IllegalStateException(
                            "non-homogenous collection, contains " + memberType + " and " +
                            member.getClass());
                    }

                    if (memberType == null)
                    {
                        memberType = member.getClass();
                    }

                    try
                    {
                        m.invoke(o, member);
                        addMethod = m;
                        break outer;
                    }
                    catch(Exception e)
                    {
                        continue outer;
                    }
                }
            }

            if (addMethod == null)
            {
                // no method with such prefix, continue with the next choice
                methodNameRoot = methodNameRoot.substring(0, methodNameRoot.length() - 1);
            }
            else
            {
                // we succeeded at least one invocation, invoke on the rest
                for(; memberIterator.hasNext(); )
                {
                    Object member = memberIterator.next();

                    if (memberType != null && !member.getClass().equals(memberType))
                    {
                        throw new IllegalStateException(
                            "non-homogenous collection, contains " + memberType + " and " +
                            member.getClass());
                    }

                    if (memberType == null)
                    {
                        memberType = member.getClass();
                    }

                    addMethod.invoke(o, member);
                }

                successful = true;
                break;
            }
        }

        if (!successful)
        {
            throw new NoSuchMethodException(
                "cannot find mutator " + setMethodName + "(" + value.getClass().getName() +
                ") or add..." + (memberType == null ? "(...)" : "(" + memberType.getName() + ")"));
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

        // TODO HBA-58
        // copying as is implemented here doesn't work for immutable collections that expose
        // access as add...(...) methods, so commenting out temporarily, but in need to return
        // here
        
        baseCopy = delta;

//        for(Method m: c.getMethods())
//        {
//            String name = m.getName();
//
//            if (!name.startsWith("set"))
//            {
//                continue;
//            }
//
//            name = name.substring(3);
//
//            String getterName = "get" + name;
//
//            Method getter = null;
//
//            try
//            {
//                getter = c.getMethod(getterName);
//            }
//            catch(Exception e)
//            {
//                // ok, keep trying
//            }
//
//            if (getter == null)
//            {
//                getterName = "is" + name;
//
//                try
//                {
//                    getter = c.getMethod(getterName);
//                }
//                catch(Exception e)
//                {
//                    // ok, keep trying
//                }
//            }
//
//            if (getter == null)
//            {
//                throw new NoSuchMethodException(
//                    "cannot find set...() or is...() accessor for " + name);
//            }
//
//            Object d = getter.invoke(delta);
//            m.invoke(baseCopy, d);
//        }

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
