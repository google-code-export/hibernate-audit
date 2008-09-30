package com.googlecode.hibernate.audit.delta_deprecated;

import com.googlecode.hibernate.audit.util.Entity;
import com.googlecode.hibernate.audit.delta.ChangeType;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 *
 *
 */
@Deprecated
public class ChangeDeprecated 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private ChangeType type;

    private Entity entity;

    private String propertyName;
    private Class propertyType;
    private Object propertyValue;

    // Constructors --------------------------------------------------------------------------------

    public ChangeDeprecated(ChangeType type, Entity entity,
                  String propertyName, Class propertyType, Object propertyValue)
    {
        this.type = type;
        this.entity = entity;
        this.propertyName = propertyName;
        this.propertyType = propertyType;
        this.propertyValue = propertyValue;
    }

    // Public --------------------------------------------------------------------------------------

    public ChangeType getType()
    {
        return type;
    }

    public String getPropertyName()
    {
        return propertyName;
    }

    public Class getPropertyType()
    {
        return propertyType;
    }

    public Object getPropertyValue()
    {
        return propertyValue;
    }

    public Entity getEntity()
    {
        return entity;
    }

    @Override
    public String toString()
    {
        return (ChangeType.INSERT.equals(type) ? "NEW " : "UPDATE ") +
        entity + " " +
        (propertyType == null ? "null" : propertyType.getName()) +
        "." + propertyName + " = " + propertyValue;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
