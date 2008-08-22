package com.googlecode.hibernate.audit.test.util.data;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$</tt>
 */
public class CustomImmutableInteger
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private int value;

    // Constructors --------------------------------------------------------------------------------

    public CustomImmutableInteger(int i)
    {
        this.value = i;
    }

    // Public --------------------------------------------------------------------------------------

    public boolean isNegative()
    {
        return value < 0;
    }

    public int getValue()
    {
        return value;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
