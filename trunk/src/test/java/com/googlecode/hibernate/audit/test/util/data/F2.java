package com.googlecode.hibernate.audit.test.util.data;

import java.util.HashSet;
import java.util.Collections;
import java.util.Set;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class F2
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Set usstrings;

    // Constructors --------------------------------------------------------------------------------

    public F2()
    {
        this.usstrings = new HashSet();
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * @return an immutable collection.
     */
    public Set getUsstrings()
    {
        return Collections.unmodifiableSet(usstrings);
    }

    public void addUsstring(String s)
    {
        usstrings.add(s);
    }

    /**
     * WE cheat for the sake of the test.
     */
    public void genericAdd(Object o)
    {
        usstrings.add(o);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
