package com.googlecode.hibernate.audit.test.util.data;

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

    // offering access to members whose accessors have been made private on purpose

    public static Long getIdFrom(K k)
    {
        return k.getId();
    }

    public static String getNameFrom(K k)
    {
        return k.getName();
    }

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    // Constructors --------------------------------------------------------------------------------

    public K()
    {
    }

    public K(String name)
    {
        setId(null); // just to shut off warning of unused setId()
        setName(name);
    }

    // Public --------------------------------------------------------------------------------------

    private Long getId()
    {
        return id;
    }

    private void setId(Long id)
    {
        this.id = id;
    }

    private String getName()
    {
        return name;
    }

    private void setName(String name)
    {
        this.name = name;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
