package com.googlecode.hibernate.audit.delta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public class PrimitiveDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String name;
    private Object value;

    // Constructors --------------------------------------------------------------------------------

    public PrimitiveDelta(String name, Object value)
    {
        this.name = name;
        this.value = value;
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * @return the name of the primitive (variable) this delta was recorded for.
     */
    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
