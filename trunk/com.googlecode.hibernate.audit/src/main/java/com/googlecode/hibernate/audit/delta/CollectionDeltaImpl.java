package com.googlecode.hibernate.audit.delta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public class CollectionDeltaImpl
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String name;

    // Constructors --------------------------------------------------------------------------------

    public CollectionDeltaImpl(String name)
    {
        this.name = name;
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * @return the name of the collection variable this delta was recorded for.
     */
    public String getName()
    {
        return name;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
