package com.googlecode.hibernate.audit.test.post_insert.data;

import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class XA2
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Long id;
    private Set<XB> xbs;

    // Constructors --------------------------------------------------------------------------------

    public XA2()
    {
        xbs = new HashSet<XB>();
    }

    // Public --------------------------------------------------------------------------------------

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Set<XB> getXbs()
    {
        return xbs;
    }

    public void setXbs(Set<XB> xbs)
    {
        this.xbs = xbs;
    }

    @Override
    public String toString()
    {
        return "XA[" + (id == null ? "TRANSIENT" : id.toString()) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
