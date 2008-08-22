package com.googlecode.hibernate.audit.test.post_insert.data;

import java.util.Set;
import java.util.HashSet;

/**
 * The tuplizer-using, "one"-end entity in a many-to-one relationship.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class XA3
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    protected Long id;
    protected Set<XB> xbs;

    // Constructors --------------------------------------------------------------------------------

    public XA3()
    {
        id = null;
        xbs = new HashSet<XB>();
    }

    // Public --------------------------------------------------------------------------------------

    public Long getId()
    {
        return id;
    }

    public Set<XB> getXbs()
    {
        return xbs;
    }

    @Override
    public String toString()
    {
        return "XA3[" + (id == null ? "TRANSIENT" : id.toString()) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
