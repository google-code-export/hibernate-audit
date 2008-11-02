package com.googlecode.hibernate.audit.test.write_collision;

import java.io.Serializable;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Entity
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String name;
    private Serializable id;

    // Constructors --------------------------------------------------------------------------------

    public Entity(String name, Serializable id)
    {
        this.name = name;
        this.id = id;
    }

    // Public --------------------------------------------------------------------------------------

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Serializable getId()
    {
        return id;
    }

    public void setId(Serializable id)
    {
        this.id = id;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof Entity))
        {
            return false;
        }

        if (name == null || id == null)
        {
            return false;
        }

        Entity that = (Entity)o;

        return name.equals(that.name) && id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode()) * 37 + (name == null ? 0 : name.hashCode());
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
