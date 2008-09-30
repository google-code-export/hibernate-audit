package com.googlecode.hibernate.audit.test.model;

import java.io.Serializable;

/**
 * Mimics an Integer.
 *
 * This one has a "valueOf(String)" method.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 */
public class CustomType2 implements Serializable
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static CustomType2 valueOf(String s)
    {
        int si = Integer.parseInt(s);
        return new CustomType2(si);
    }

    /**
     * This one returns a wrong int value (value + 1) on purpose (for testing).
     */
    public static CustomType2 parseCustomType2(String s)
    {
        int si = Integer.parseInt(s);
        return new CustomType2(si + 1);
    }

    // Attributes ----------------------------------------------------------------------------------

    private int i;

    // Constructors --------------------------------------------------------------------------------

    public CustomType2(int i)
    {
        this.i = i;
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof CustomType2))
        {
            return false;
        }

        CustomType2 that = (CustomType2)o;

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