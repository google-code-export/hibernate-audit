package com.googlecode.hibernate.audit.util;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class QueryParameter
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String name;
    private int position;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param position - 0-based.
     * @param name - null is acceptable for a positional parameter.
     */
    QueryParameter(int position, String name)
    {
        this.position = position;
        this.name = name;
    }

    // Public --------------------------------------------------------------------------------------

    public boolean isNamed()
    {
        return name != null;
    }

    public String getName()
    {
        return name;
    }

    public int getPosition()
    {
        return position;
    }

    @Override
    public String toString()
    {
        return name != null ? name : "[" + position + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
    
}
