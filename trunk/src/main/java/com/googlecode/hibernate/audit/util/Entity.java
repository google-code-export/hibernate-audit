package com.googlecode.hibernate.audit.util;

import java.io.Serializable;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class Entity
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Serializable id;
    private Class type;

    // Constructors --------------------------------------------------------------------------------

    public Entity(Serializable id, Class type)
    {
        this.id = id;
        this.type = type;
    }

    // Public --------------------------------------------------------------------------------------

    public Serializable getId()
    {
        return id;
    }

    public Class getType()
    {
        return type;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (id == null || type == null)
        {
            return false;
        }

        if (!(o instanceof Entity))
        {
            return false;
        }

        Entity that = (Entity)o;

        return id.equals(that.id) && type.equals(that.type);
    }

    @Override
    public int hashCode()
    {
        int result = 17;

        result = 37 * result + (id == null ? 0 : id.hashCode());
        result = 37 * result + (type == null ? 0 : type.hashCode());

        return result;
    }

    @Override
    public String toString()
    {
        return (type == null ? "null" : type.getName()) + "[" + id + "]";
    }
    
    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
