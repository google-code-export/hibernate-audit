package com.googlecode.hibernate.audit.test.util.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Collections;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class F1
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Collection ucstrings;

    // Constructors --------------------------------------------------------------------------------

    public F1()
    {
        this.ucstrings = new HashSet();
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * @return an immutable collection.
     */
    public Collection getUcstrings()
    {
        return Collections.unmodifiableCollection(ucstrings);
    }

    public void addUcstring(String s)
    {
        ucstrings.add(s);
    }

    /**
     * WE cheat for the sake of the test.
     */
    public void genericAdd(Object o)
    {
        ucstrings.add(o);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
