package com.googlecode.hibernate.audit.delta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
abstract class MemberVariableDeltaSupport implements MemberVariableDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String name;

    // Constructors --------------------------------------------------------------------------------

    // MemberVariableImplementation ----------------------------------------------------------------

    public String getName()
    {
        return name;
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

        if (!(o instanceof MemberVariableDelta))
        {
            return false;
        }

        MemberVariableDelta that = (MemberVariableDelta)o;

        return name.equals(that.getName());
    }

    @Override
    public int hashCode()
    {
        return name == null ? 0 : name.hashCode();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected void setName(String name)
    {
        this.name = name;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
