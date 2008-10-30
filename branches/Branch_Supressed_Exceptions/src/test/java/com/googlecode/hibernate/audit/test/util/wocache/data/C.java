package com.googlecode.hibernate.audit.test.util.wocache.data;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@DiscriminatorValue("C")
public class C extends B
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Integer ci;
    private String cs;

    // Constructors --------------------------------------------------------------------------------

    public C()
    {
    }

    // Public --------------------------------------------------------------------------------------

    public Integer getCi()
    {
        return ci;
    }

    public void setCi(Integer ci)
    {
        this.ci = ci;
    }

    public String getCs()
    {
        return cs;
    }

    public void setCs(String cs)
    {
        this.cs = cs;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
