package com.googlecode.hibernate.audit.test.post_insert.data;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class XC 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    protected Long id;
    protected String name;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public Long getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    @Override
    public String toString()
    {
        return "XB[" + (id == null ? "TRANSIENT" : id.toString()) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
