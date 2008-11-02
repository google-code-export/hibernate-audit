package com.googlecode.hibernate.audit.test.data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "B")
public class B
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    private String s;
    private Integer i;

    // Constructors --------------------------------------------------------------------------------

    public B()
    {
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
