package com.googlecode.hibernate.audit.delta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
class PrimitiveDelta<T> extends MemberVariableDeltaSupport implements ScalarDelta<T>
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private T value;

    // Constructors --------------------------------------------------------------------------------

    PrimitiveDelta(String name, T value)
    {
        setName(name);
        this.value = value;
    }

    // ScalarDelta implementation ------------------------------------------------------------------

    public boolean isEntity()
    {
        return false;
    }

    public boolean isPrimitive()
    {
        return true;
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

        if (getName() == null)
        {
            return false;
        }

        if (!(o instanceof ScalarDelta))
        {
            return false;
        }

        ScalarDelta that = (ScalarDelta)o;

        return getName().equals(that.getName());
    }

    @Override
    public int hashCode()
    {
        return getName() == null ? 0 : getName().hashCode();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
