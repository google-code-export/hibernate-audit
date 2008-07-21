package com.googlecode.hibernate.audit.test.util;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class C
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String s;

    private List<A> as;

    // Constructors --------------------------------------------------------------------------------

    public C()
    {
        as = new ArrayList<A>();
    }

    C(String s)
    {
        this();
        this.s = s;
    }

    // Public --------------------------------------------------------------------------------------

    public String getS()
    {
        return s;
    }

    public void setS(String s)
    {
        this.s = s;
    }

    public List<A> getAs()
    {
        return as;
    }

    public void setAs(List<A> as)
    {
        this.as = as;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}