package com.googlecode.hibernate.audit.delta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
class PrimitiveDeltaImpl<T> implements PrimitiveDelta<T>
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String name;
    private T value;

    // Constructors --------------------------------------------------------------------------------

    PrimitiveDeltaImpl(String name, T value)
    {
        this.name = name;
        this.value = value;
    }

    // PrimitiveDelta implementation ---------------------------------------------------------------

    public String getName()
    {
        return name;
    }

    public T getValue()
    {
        return value;
    }

    public Class getType()
    {
        if (value == null)
        {
            return null;
        }

        return value.getClass();
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (name == null)
        {
            return false;
        }

        if (!(o instanceof PrimitiveDelta))
        {
            return false;
        }

        PrimitiveDelta that = (PrimitiveDelta)o;

        return name.equals(that.getName());
    }

    @Override
    public int hashCode()
    {
        return name == null ? 0 : name.hashCode();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
