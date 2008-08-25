package com.googlecode.hibernate.audit.util;

import org.apache.log4j.Logger;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.proxy.LazyInitializer;

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
import java.util.HashSet;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.Collections;
import java.sql.Timestamp;
import java.io.Serializable;

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

    private static Set<Class> appDeclaredImmutableClasses;

    /**
     * Add the application-level class to the internal set of classes that are considered immutable.
     * No deep copy will be performed for an immutable class instance, it will be always be passed
     * by reference.
     *
     * @param ic - the class or interface to be handled as immutable.
     *
     * @return true if the set did not already contain the specified class.
     */
    public synchronized static boolean registerImmutableClass(Class ic)
    {
        if (appDeclaredImmutableClasses == null)
        {
            appDeclaredImmutableClasses = new HashSet<Class>();
        }

        return appDeclaredImmutableClasses.add(ic);
    }

    /**
     * Always returns a copy.
     */
    public synchronized static Set<Class> getImmutableClasses()
    {
        if (appDeclaredImmutableClasses == null)
        {
            return Collections.emptySet();
        }

        return new HashSet<Class>(appDeclaredImmutableClasses);
    }

    /**
     * @return true if the set contained the specified class.
     */
    public synchronized static boolean unregisterImmutableClass(Class ic)
    {
        return appDeclaredImmutableClasses != null && appDeclaredImmutableClasses.remove(ic);
    }

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

        Collection<Method> allMethods = new HashSet<Method>();

        // 'collect' all methods, including those of the superclasses
        // TODO very inefficient
        Class c = o.getClass();

        while(c != null)
        {
            allMethods.addAll(Arrays.asList(c.getDeclaredMethods()));
            c = c.getSuperclass();
        }

        outer: for(Method m: allMethods)
        {
            String n = m.getName();
            Class[] pts = m.getParameterTypes();

            if (!methodName.equals(n) || pts.length != 1)
            {
                continue;
            }

            Class parameterType = pts[0];
            Class argumentType = value.getClass();

            while(argumentType != Object.class)
            {
                if (argumentType.equals(parameterType) ||
                    parameterType.isAssignableFrom(argumentType))
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
                "cannot find mutator " + methodName + "(" + value.getClass().getName() +
                ") on " + o.getClass().getName() + " or any of its superclasses");
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

        String tempNameRoot = methodNameRoot;
        while(tempNameRoot.length() > 0)
        {
            Method addMethod = null;
            Iterator memberIterator = null;

            String addMethodNamePrefix = "add" + tempNameRoot;

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
                tempNameRoot = tempNameRoot.substring(0, tempNameRoot.length() - 1);
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

        // if not successful so far, try next trick wich is to try to mutate the collection
        // directly

        if (!successful)
        {
            // TODO duplicate code (see applyDelta())
            String getMethodName = "get" + methodNameRoot;

            for(Method m: methods)
            {
                if (!m.getName().equals(getMethodName))
                {
                    continue;
                }

                // try to invoke
                try
                {
                    Collection c = (Collection)m.invoke(o);
                    c.clear();
                    c.addAll(value);
                    successful = true;
                    break;
                }
                catch(Exception e)
                {
                    log.debug("failed to invoke", e);
                }
            }
        }

        if (!successful)
        {
            throw new NoSuchMethodException(
                "cannot find mutator " + setMethodName + "(" + value.getClass().getName() +
                ") or add..." + (memberType == null ? "(...)" : "(" + memberType.getName() + ")") +
                ", nor being able to mutate the collection directly");
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
                deltaPiece = cleanupHibernateProxy(deltaPiece);

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

                try
                {
                    setter.invoke(base, deltaPiece);
                }
                catch(Exception e)
                {
                    // we wrap the origial exception within a 'synthetic' one because the original
                    // exception can be pretty cryptic, and the synthetic one adds extra debugging
                    // elements
                    throw new Exception("failed to invoke " + setter.getName() + "(" +
                                        deltaPiece + ") on " + base, e);
                }
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

                if (!Modifier.isPublic(getter.getModifiers()))
                {
                    // TODO see HBA-84
                    getter.setAccessible(true);
                }

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
                    // ok, one more shot
                    try
                    {
                        adderName = memberClass.getName();
                        adderName = adderName.substring(adderName.lastIndexOf('.') + 1);
                        adderName = "add" + adderName;

                        adder = c.getMethod(adderName, memberClass);
                    }
                    catch(Exception e2)
                    {
                        // tough luck
                    }
                }

                if (adder != null)
                {
                    if (!Modifier.isPublic(adder.getModifiers()))
                    {
                        // TODO see HBA-84
                        adder.setAccessible(true);
                    }

                    // loop over the collection and add members one by one
                    for(Iterator i = members.iterator(); i.hasNext(); )
                    {
                        Object member = i.next();
                        adder.invoke(base, member);
                    }

                    continue;
                }
            }

            // if not successful so far, try next trick wich is to try to mutate the collection
            // directly

            // TODO duplicate code (see applyDelta())

            Object deltaPiece = getter.invoke(delta);

            // TODO we're not doing a deep copy, we're copying shallow, bad, to fix this

            if (collectionClass != null)
            {
                Collection baseCollection = (Collection)getter.invoke(base);
                baseCollection.clear();
                Collection deltaDeepCopy = (Collection)Reflections.
                    deepCopy(deltaPiece, seenInstances);

                baseCollection.addAll(deltaDeepCopy);
                continue;
            }

            // if we reach this point, it means we didn't find an appropriate mutator
            throw new NoSuchMethodException(
                "cannot find set...() or add...() mutator corresponding to accessor " + name + "()");
        }
    }

    /**
     * Performs a deep copy of the object, making copies for mutable instances and just linking to
     * immutable instances. If in doubt, it makes a copy.
     *
     * The method strips off HibernateProxy wrappers.
     */
    public static Object deepCopy(Object o) throws Exception
    {
        return deepCopy(o, new ArrayList<Node>());
    }

    /**
     * @param seenInstances - a list of instances passed along stack frames for recursive calls,
     *        containing instances which are candidates for circular references, thus needing to be
     *        copied by reference, even if they're mutable, to maintain referential integrity or
     *        avoid stack overflow. Never null.
     */
    private static Object deepCopy(Object o, List<Node> seenInstances) throws Exception
    {
        if (o == null)
        {
            return null;
        }

        o = cleanupHibernateProxy(o);

        if (!isMutable(o))
        {
            return o;
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

        // not a collection, the instance may be part of a circular reference
        seenInstances.add(new Node(o, copy));

        //TODO deepCopy() for arrays https://jira.novaordis.org/browse/HBA-67

        applyDelta(copy, o, seenInstances);

        return copy;
    }

    public static Object instantiateOverridingAccessibility(Class c) throws Exception
    {
        // java.sql.Timestamp, java.sql.Date, etc. doen't have a no-argument constructor, so we case
        // for them

        if (Timestamp.class.equals(c))
        {
            return new Timestamp(0);
        }

        // TODO commented out because get/set succession goes awry. Declaring as immutable,
        // see https://jira.novaordis.org/browse/HBA-98
//        else if (java.sql.Date.class.equals(c))
//        {
//            return new java.sql.Date(0);
//        }
        
        Constructor constructor = null;
        try
        {
            constructor = c.getDeclaredConstructor();
        }
        catch(NoSuchMethodException e)
        {
            // temporary kludge for "proprietary" collections with restricted constructors
            if (List.class.isAssignableFrom(c))
            {
                // TODO very simplistic, non-paramterized approach, need better implementation
                constructor = ArrayList.class.getDeclaredConstructor();
            }
            else
            {
                throw e;
            }
        }

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
            Boolean.class.equals(c) ||
            Date.class.equals(c) ||
            java.sql.Date.class.equals(c) || // TODO https://jira.novaordis.org/browse/HBA-98
            (appDeclaredImmutableClasses != null && appDeclaredImmutableClasses.contains(c)) ||
            isAssignableFromApplicationLevelImmutable(c)) // HBA-95: TODO not entirely correct, a subclass of a non-mutable can be mutable, review this
        {
            return false;
        }
        else if (Collection.class.isAssignableFrom(c))
        {
            return true;
        }

        // TODO very very very inneficient

        Collection<Method> allMethods = new HashSet<Method>();

        while(c != null)
        {
            allMethods.addAll(Arrays.asList(c.getDeclaredMethods()));
            c = c.getSuperclass();
        }

        for(Method m: allMethods)
        {
            String name = m.getName();
            if (name.startsWith("set"))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * Strips away the HibernateProxy wrapper, if any, returning the underlying entity. If there is
     * no proxy, the original object is returned.
     */
    public static Object cleanupHibernateProxy(Object o)
    {
        if (!(o instanceof HibernateProxy))
        {
            return o;
        }

        HibernateProxy hp = (HibernateProxy)o;
        LazyInitializer li = hp.getHibernateLazyInitializer();

        if (li.isUninitialized())
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        return li.getImplementation();
    }

    /**
     * Recursively walks o internal graph of objects and returns the instance corresponding to
     * targetClass and targetId.
     *
     * TODO incomplete and insuficiently tested needs refactoring
     *
     * @return null if nothing appropriate found.
     */
    public static Object find(Object o, Class targetClass, Serializable targetId)
    {
        if (o == null || targetClass == null || targetId == null)
        {
            return null;
        }

        if (targetClass.isInstance(o) && targetId.equals(getId(o)))
        {
            return o;
        }
        
        if (o instanceof Collection)
        {
            return find((Collection)o, targetClass, targetId);
        }

        Set<Collection> collections = null;

        for(Method m: o.getClass().getDeclaredMethods())
        {
            String name = m.getName();

            if (!name.startsWith("get") || m.getParameterTypes().length > 0)
            {
                continue;
            }

            try
            {
                Object candidate = m.invoke(o);

                if (candidate == null)
                {
                    continue;
                }
                else if (candidate instanceof Collection)
                {
                    if (collections == null)
                    {
                        collections = new HashSet<Collection>();
                    }
                    collections.add((Collection)candidate);
                    continue;
                }
                else if (!targetClass.equals(candidate.getClass()))
                {
                    continue;
                }
                else if (!targetId.equals(getId(candidate)))
                {
                    continue;
                }

                // this is what we were looking for
                return candidate;
            }
            catch(Exception e)
            {
                // too bad, ignore (TODO for the time being)
            }
        }

        // try the collections
        if (collections != null)
        {
            for(Collection c: collections)
            {
                Object result = find(c, targetClass, targetId);

                if (result != null)
                {
                    return result;
                }
            }
        }

        return null;
    }

    /**
     * TODO incomplete and insuficiently tested needs refactoring
     */
    public static Object find(Collection c, Class targetClass, Serializable targetId)
    {
        for(Iterator i = c.iterator(); i.hasNext(); )
        {
            Object o = i.next();
            Object result = find(o, targetClass, targetId);

            if (result != null)
            {
                return result;
            }
        }

        return null;
    }


    /**
     * TODO incomplete and insuficiently tested needs refactoring
     */
    public static Serializable getId(Object o)
    {
        if (o == null)
        {
            return null;
        }

        try
        {
            Method getId = o.getClass().getMethod("getId");
            return (Serializable)getId.invoke(o);
        }
        catch(Exception e)
        {
            return null;
        }
    }

    public static Object newInstance(Class c, Serializable id) throws Exception
    {
        Constructor ctor = c.getDeclaredConstructor();

        if (!Modifier.isPublic(ctor.getModifiers()))
        {
            // TODO see HBA-84
            ctor.setAccessible(true);
        }

        Object o = ctor.newInstance();

        for(Method m: c.getMethods())
        {
            if (m.getName().equals("setId"))
            {
                if (!Modifier.isPublic(m.getModifiers()))
                {
                    // TODO see HBA-84
                    ctor.setAccessible(true);
                }

                m.invoke(o, id);
            }
        }
        return o;
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private synchronized static boolean isAssignableFromApplicationLevelImmutable(Class c)
    {

        if (appDeclaredImmutableClasses == null)
        {
            return false;
        }

        for(Class ic: appDeclaredImmutableClasses)
        {
            if (ic.isAssignableFrom(c))
            {
                return true;
            }

        }

        return false;
    }

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
