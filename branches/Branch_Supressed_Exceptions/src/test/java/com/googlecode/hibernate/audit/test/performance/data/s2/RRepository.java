package com.googlecode.hibernate.audit.test.performance.data.s2;

import org.hibernate.SessionFactory;
import org.hibernate.Session;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import com.googlecode.hibernate.audit.test.performance.util.Util;
import com.googlecode.hibernate.audit.util.packinsp.PackageInspector;
import com.googlecode.hibernate.audit.util.packinsp.Filter;

/**
 * A simulator of reference data repository.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class RRepository
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Map<Class, List<R>> cache;
    private int size;
    private String packageName;
    private Set<Class> rClasses;


    // Constructors --------------------------------------------------------------------------------

    public RRepository(int size) throws Exception
    {
        this.size = size;
        cache = new HashMap<Class, List<R>>();

        packageName = getClass().getPackage().getName();
        
        rClasses = new PackageInspector(getClass()).inspect(new Filter()
        {
            public boolean accept(Class c)
            {
                return R.class.isAssignableFrom(c) && !Modifier.isAbstract(c.getModifiers());
            }
        });
    }

    // Public --------------------------------------------------------------------------------------

    public void populate(SessionFactory sf, boolean writeToDatabase) throws Exception
    {
        Session s = writeToDatabase ? sf.openSession() : null;

        try
        {
            if (writeToDatabase)
            {
                s.beginTransaction();
            }

            for(int i = 0; i < size; i ++)
            {
                for(Class c: rClasses)
                {
                    Constructor ctor = c.getConstructor(Integer.TYPE);
                    R r = (R)ctor.newInstance(i);

                    if (writeToDatabase)
                    {
                        s.save(r);
                    }

                    List<R> instances = cache.get(c);

                    if (instances == null)
                    {
                        instances = new ArrayList<R>();
                        cache.put(c, instances);
                    }

                    instances.add(r);
                }
            }

            if (writeToDatabase)
            {
                s.getTransaction().commit();
            }
        }
        finally
        {
            if (s != null)
            {
                s.close();
            }
        }
    }

    /**
     * @param i must be between 0 and repository's size (exclusive).
     *
     * @return null if no corresponding instance found.
     */
    public R getInstance(Class type, int i)
    {
        List<R> instances = cache.get(type);

        if (instances == null)
        {
            return null;
        }

        if (i > instances.size() - 1)
        {
            return null;
        }

        R result = instances.get(i);

        // little gratuitous consistency check
        if (i != result.getI())
        {
            throw new IllegalStateException(
                result + " was suppose to be " + i + " element, but it isn't");
        }

        return result;
    }

    public int getSize()
    {
        return size;
    }

    public void fillReferences(Object o) throws Exception
    {
        Class c = o.getClass();

        for(Method m: c.getMethods())
        {
            String name = m.getName();

            if (!name.startsWith("set"))
            {
                continue;
            }

            name = name.substring(3).toUpperCase();

            Class rc = null;

            try
            {
                rc = Class.forName(packageName + "." + name);
            }
            catch(Exception e)
            {
                continue;
            }

            if (!R.class.isAssignableFrom(rc))
            {
                continue;
            }

            int i = Util.randomInteger(0, getSize() - 1);
            R r = getInstance(rc, i);

            if (r == null)
            {
                throw new IllegalStateException("did not find " + rc + "[" + i + "] in repository");
            }

            m.invoke(o, r);
        }
    }

    public Set<Class> getRClasses()
    {
        return rClasses;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
