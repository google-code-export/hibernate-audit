package com.googlecode.hibernate.audit.delta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
class PrimitiveDeltaImpl<T> extends MemberVariableDeltaSupport implements PrimitiveDelta<T>
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String shortClassName; // used in toString()
    private T value;

    // Constructors --------------------------------------------------------------------------------

    PrimitiveDeltaImpl(String name, T value)
    {
        setName(name);
        this.value = value;

        if (value == null)
        {
            shortClassName = "null";
        }
        else
        {
            shortClassName = value.getClass().getName();
            int i = shortClassName.lastIndexOf('.');

            if (i != -1)
            {
                shortClassName = shortClassName.substring(i + 1);
            }
        }
    }

    // ScalarDelta implementation ------------------------------------------------------------------

    public boolean isEntityReference()
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
    public String toString()
    {
        return getName() + "[" + shortClassName + "]=" + value;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
