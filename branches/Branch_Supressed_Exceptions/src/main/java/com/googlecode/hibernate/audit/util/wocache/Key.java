package com.googlecode.hibernate.audit.util.wocache;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Immutable.
 *
 * WARNING, for speed optimization reasons, Key("a", 1, "b", 2) is *NOT* considered equal to
 * Key("b", 2, "a", 1).
 *
 * TODO This class will probably go away: https://jira.novaordis.org/browse/HBA-125
 *
 * TODO speed optimization, calculate a unique hash based on names/values?
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Key
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private List<String> names;
    private List<Object> values;

    private Integer hashCode;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @exception IllegalArgumentException for unconsistent name/value pairs, duplicate names,
     *            null values, etc.
     */
    public Key(Object... nameValuePairs)
    {
        names = new ArrayList<String>();
        values = new ArrayList<Object>();

        boolean nameExpected = true;
        for(Object o: nameValuePairs)
        {
            if (nameExpected)
            {
                if (!(o instanceof String))
                {
                    throw new IllegalArgumentException(o + " must be a name (String)");
                }

                String s = (String)o;
                if (names.contains(s))
                {
                    throw new IllegalArgumentException("duplicate name " + s);
                }

                names.add(s);
                nameExpected = false;
            }
            else
            {
                if (o == null)
                {
                    throw new IllegalArgumentException("null value correspoding to " +
                                                       names.get(names.size() - 1));
                }

                values.add(o);
                nameExpected = true;
            }
        }

        if (names.size() != values.size())
        {
            throw new IllegalArgumentException("value missing for " + names.get(names.size() - 1));
        }
    }

    // Public --------------------------------------------------------------------------------------

    public boolean isEmpty()
    {
        return names.isEmpty();
    }

    /**
     * TODO inconsistent with the promise of immutability, may be used to modify the internal
     *      name list if remove() is called.
     */
    public Iterator<String> names()
    {
        return names.iterator();
    }

    /**
     * @return null of an unknown name.
     */
    public Object getValue(String name)
    {
        int i = names.indexOf(name);

        if (i == -1)
        {
            return null;
        }

        return values.get(i);
    }

    /**
     * WARNING!
     *
     * Dor speed optimization reasons, Key("a", 1, "b", 2) is *NOT* considered to be equal to
     * Key("b", 2, "a", 1).
     *
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Key))
        {
            return false;
        }

        Key that = (Key)o;

        int size = names.size();

        if (size != that.names.size())
        {
            return false;
        }

        for(int i = 0; i < size; i++)
        {
            // we're sure names aren't null

            if (!names.get(i).equals(that.names.get(i)))
            {
                return false;
            }

            // we're sure values arent' null

            if (!values.get(i).equals(that.values.get(i)))
            {
                return false;
            }
        }

        return true;
    }

    /**
     * Cache the hashCode as instances are immutable
     */
    @Override
    public int hashCode()
    {
        if (hashCode == null)
        {
            int result = 0;
            for(String name: names)
            {
                result += 11 * name.hashCode() + 17;
            }

            for(Object o: values)
            {
                result += 13 * o.hashCode() + 37;
            }

            hashCode = new Integer(result);
        }

        return hashCode.intValue();
    }
    
    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
