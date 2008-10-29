package com.googlecode.hibernate.audit.test.post_insert.data;

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
@Table(name = "K")
public class K
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private String id;

    private String s;
    private Integer i;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public String getId()
    {
        return id;
    }

    public void setId(String id)
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