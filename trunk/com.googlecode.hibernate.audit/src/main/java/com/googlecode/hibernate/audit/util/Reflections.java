package com.googlecode.hibernate.audit.util;

import org.apache.log4j.Logger;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ArrayList;

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


        Method mutator = null;

        outer: for(Method m: o.getClass().getDeclaredMethods())
        {
            String n = m.getName();
            Class[] pts = m.getParameterTypes();

            if (!methodName.equals(n) || pts.length != 1)
            {
                continue;
            }

            Class argumentType = value.getClass();
            while(argumentType != Object.class)
            {
                if (argumentType.equals(pts[0]))
                {
                    mutator = m;
                    break outer;
                }

                argumentType = argumentType.getSuperclass();
            }
        }

        if (mutator == null)
        {
            // TODO last chance, to we use a tuplizer? https://jira.novaordis.org/browse/HBA-81

            throw new NoSuchMethodException(
                "cannot find mutator " + methodName + "(...) for " + value.getClass().getName() +
                " or any of its superclasses");
        }

        // override accessibility limitations if any
        if (!Modifier.isPublic(mutator.getModifiers()))
        {
            // TODO see HBA-84
            mutator.setAccessible(true);
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
     * Mutates the base, by applying all changes represented by delta.
     */
    public static void applyDelta(Object base, Object delta) throws Exception
    {
        List<Node> seenInstances = new ArrayList<Node>();
        seenInstances.add(new Node(delta, base));
        applyDelta(base, delta, seenInstances);
    }

    /**
     * @param seenInstances - a list of instances passed along stack frames for recursive calls,
     *        containing instances which are candidates for circular references, thus needing to be
     *        copied by reference, even if they're mutable, to maintain referential integrity. May
     *        be null.
     */
    public static void applyDelta(Object base, Object delta, List<Node> seenInstances)
        throws Exception
    {
        Class c = base.getClass();
        if (!c.isInstance(delta))
        {
            throw new IllegalArgumentException(base + " and " + delta + " have different types");
        }

        outer: for(Method getter: c.getDeclaredMethods())
        {
            if (Modifier.isStatic(getter.getModifiers()))
            {
                // ignoring static methods
                continue;
            }
            
            String name = getter.getName();

            int prefixLength;

            if (name.startsWith("get") && !"getClass".equals(name))
            {
                prefixLength = 3;
            }
            else if (name.startsWith("is"))
            {
                prefixLength = 2;
            }
            else
            {
                // not a getter, ignoring
                continue;
            }

            String attributeName = name.substring(prefixLength);
            Type attributeType = getter.getGenericReturnType();

            Method setter = null;
            String setterName = "set" + attributeName;
            for(Method sm: c.getDeclaredMethods())
            {
                if (Modifier.isStatic(sm.getModifiers()))
                {
                    continue;
                }

                Type[] params = sm.getGenericParameterTypes();

                if (sm.getName().equals(setterName) &&
                    params.length == 1 &&
                    attributeType.equals(params[0]))
                {
                    setter = sm;
                    break;
                }
            }

            if (setter != null)
            {
                if (!Modifier.isPublic(getter.getModifiers()))
                {
                    // TODO see HBA-84
                    getter.setAccessible(true);
                }
            
                Object deltaPiece = getter.invoke(delta);

                for(Node seen: seenInstances)
                {
                    if (seen.isSameInstance(deltaPiece))
                    {
                        // maintaining referential integrity

                        if (!Modifier.isPublic(setter.getModifiers()))
                        {
                            // TODO see HBA-84
                            setter.setAccessible(true);
                        }
                        setter.invoke(base, seen.getValidReference());
                        continue outer;
                    }
                }

                if (isMutable(deltaPiece))
                {
                    deltaPiece = deepCopy(deltaPiece, seenInstances);
                }

                if (!Modifier.isPublic(setter.getModifiers()))
                {
                    // TODO see HBA-84
                    setter.setAccessible(true);
                }

                setter.invoke(base, deltaPiece);
                continue;
            }

            Method adder = null;
            Class collectionClass = null;
            Class memberClass = null;

            if (attributeType instanceof ParameterizedType)
            {
                Type rawType = ((ParameterizedType)attributeType).getRawType();

                if (!(rawType instanceof Class))
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                Class rawClass = (Class)rawType;

                if (Collection.class.isAssignableFrom(rawClass))
                {
                    collectionClass = rawClass;
                    Type[] actualTypes = ((ParameterizedType)attributeType).getActualTypeArguments();

                    if (actualTypes.length == 0)
                    {
                        throw new IllegalStateException("no actual types");
                    }
                    else if (actualTypes.length > 1 || !(actualTypes[0] instanceof Class))
                    {
                        log.error("actualTypes: " + actualTypes);
                        throw new RuntimeException("NOT YET IMPLEMENTED");
                    }

                    memberClass = (Class)actualTypes[0];
                }
            }
            else if (attributeType instanceof Class &&
                     Collection.class.isAssignableFrom((Class)attributeType))
            {
                collectionClass = (Class)attributeType;
            }
            else
            {
                log.debug("base: " + base + ", delta: " + delta + ", " +
                          "attributeType: " + attributeType);
                throw new IllegalStateException("no " + setterName + "(" + attributeType +
                                                ") method found on " + base.getClass());
            }

            if (collectionClass != null && attributeName.endsWith("s"))
            {
                // special case for immutable collections that allow "add...(...)"

                Collection members = (Collection)getter.invoke(delta);;

                if (memberClass == null)
                {
                    // we need to figure out the member type by just looking at the collection

                    if (members.isEmpty())
                    {
                        // the collection is empty, so we need to empty out the corresponding
                        // collection on base
                        // TODO we can do that either using remove(), or getting a hold on the
                        //      modifiable collection, etc.
                        throw new RuntimeException("NOT YET IMPLEMENTED");
                    }

                    // we make sure the collection is homogeneous
                    for(Object o: members)
                    {
                        if (memberClass == null)
                        {
                            memberClass = o.getClass();
                            continue;
                        }

                        if (!memberClass.equals(o.getClass()))
                        {
                            throw new IllegalArgumentException(
                                "heterogeneous collection: " + memberClass.getName() + ", " +
                                o.getClass().getName());
                        }
                    }
                }

                String adderName = "add" + attributeName.substring(0, attributeName.length() - 1);

                try
                {
                    adder = c.getMethod(adderName, memberClass);
                }
                catch(Exception e)
                {
                    // ok
                }

                if (adder != null)
                {
                    // loop over the collection and add members one by one
                    for(Iterator i = members.iterator(); i.hasNext(); )
                    {
                        Object member = i.next();
                        adder.invoke(base, member);
                    }

                    continue;
                }
            }

            // if we reach this point, it means we didn't find an appropriate mutator
            throw new NoSuchMethodException(
                "cannot find set...() or add...() mutator corresponding to accessor " + name + "()");
        }
    }

    /**
     * Performs a deep copy of the object, making copies for mutable instances and just linking to
     * immutable instances. If in doubt, it makes a copy.
     */
    public static Object deepCopy(Object o) throws Exception
    {
        return deepCopy(o, new ArrayList<Node>());
    }

    /**
     * @param seenInstances - a list of instances passed along stack frames for recursive calls,
     *        containing instances which are candidates for circular references, thus needing to be
     *        copied by reference, even if they're mutable, to maintain referential integrity. Never
     *        null.
     */
    private static Object deepCopy(Object o, List<Node> seenInstances) throws Exception
    {
        if (o == null)
        {
            return null;
        }

        Object copy = instantiateOverridingAccessibility(o.getClass());

        if (o instanceof Collection)
        {
            Collection c = (Collection)copy;
            for(Object member: (Collection)o)
            {
                if(isMutable(member))
                {
                    boolean hasBeenSeen = false;
                    for(Node seen: seenInstances)
                    {
                        if (seen.isSameInstance(member))
                        {
                            // use the reference, don't make a deep copy of this guy
                            member = seen.getValidReference();
                            hasBeenSeen = true;
                            break;
                        }
                    }

                    if (!hasBeenSeen)
                    {
                        member = deepCopy(member, seenInstances);
                    }
                }

                c.add(member);
            }

            return c;
        }

        // TODO deepCopy() for arrays https://jira.novaordis.org/browse/HBA-67

        applyDelta(copy, o, seenInstances);

        return copy;
    }

    public static Object instantiateOverridingAccessibility(Class c) throws Exception
    {
        Constructor constructor = c.getDeclaredConstructor();

        if (!Modifier.isPublic(constructor.getModifiers()) ||
            !Modifier.isPublic(c.getModifiers()))
        {
            // TODO see HBA-84
            constructor.setAccessible(true);
        }

        return constructor.newInstance();
    }

    public static boolean isMutable(Object o)
    {
        if (o == null)
        {
            return false;
        }

        Class c = o.getClass();

        if (String.class.equals(c) ||
            Integer.class.equals(c) ||
            Long.class.equals(c) ||
            Boolean.class.equals(c))
        {
            return false;
        }

        return true;
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private static class Node
    {
        private Object active;
        private Object future;

        Node(Object active, Object future)
        {
            this.active = active;
            this.future = future;
        }

        Object getValidReference()
        {
            if (future != null)
            {
                return future;
            }

            return active;
        }

        boolean isSameInstance(Object instance)
        {
            return active == instance;
        }
    }
}
