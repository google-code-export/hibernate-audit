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
public class Change
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private ChangeType type;
    private Object target;
    private String name;
    private Object value;

    // Constructors --------------------------------------------------------------------------------

    public Change(ChangeType type, Object target, String name, Object value)
    {
        this.type = type;
        this.target = target;
        this.name = name;
        this.value = value;
    }

    // Public --------------------------------------------------------------------------------------

    public ChangeType getType()
    {
        return type;
    }

    public String getName()
    {
        return name;
    }

    public Object getValue()
    {
        return value;
    }

    public Object getTarget()
    {
        return target;
    }

    @Override
    public String toString()
    {
        return type.toString() + " " + name + " to " + value;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
