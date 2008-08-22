package com.googlecode.hibernate.audit.test.model;

import java.io.Serializable;

/**
 * Mimics an Integer.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 */
public class CustomType implements Serializable
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private int i;

    // Constructors --------------------------------------------------------------------------------

    public CustomType(int i)
    {
        this.i = i;
    }

    // Public --------------------------------------------------------------------------------------

    public static CustomType parseCustomType(String s)
    {
        int si = Integer.parseInt(s);
        return new CustomType(si);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof CustomType))
        {
            return false;
        }

        CustomType that = (CustomType)o;

        return i == that.i;
    }

    @Override
    public int hashCode()
    {
        return i;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
