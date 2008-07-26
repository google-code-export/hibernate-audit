package com.googlecode.hibernate.audit.test.util.data;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class F3
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Set<String> strings;

    // Constructors --------------------------------------------------------------------------------

    public F3()
    {
        this.strings = new HashSet<String>();
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * @return an immutable collection.
     */
    public Set<String> getStrings()
    {
        return Collections.unmodifiableSet(strings);
    }

    public void addString(String s)
    {
        strings.add(s);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
