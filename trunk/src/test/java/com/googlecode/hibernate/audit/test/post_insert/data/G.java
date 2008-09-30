package com.googlecode.hibernate.audit.test.post_insert.data;

import javax.persistence.Embeddable;
import javax.persistence.Column;

/**
 * Data for component testing (the embeddable).
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Embeddable
public class G
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Column(name = "EMBEDDED_S")
    private String s;

    @Column(name = "EMBEDDED_I")
    private Integer i;

    // Constructors --------------------------------------------------------------------------------

    public G()
    {
    }

    // Public --------------------------------------------------------------------------------------

    public String getS()
    {
        return s;
    }

    public void setS(String s)
    {
        this.s = s;
    }

    public Integer getI()
    {
        return i;
    }

    public void setI(Integer i)
    {
        this.i = i;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
