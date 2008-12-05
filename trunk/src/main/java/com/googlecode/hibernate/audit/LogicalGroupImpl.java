package com.googlecode.hibernate.audit;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 */
public class LogicalGroupImpl implements LogicalGroup
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Long id;
    private String entityName;

    // Constructors --------------------------------------------------------------------------------

    public LogicalGroupImpl(Long id, String entityName)
    {
        this.id = id;
        this.entityName = entityName;
    }

    // LogicalGroup implementation -----------------------------------------------------------------

    public Long getLogicalGroupId()
    {
        return id;
    }

    public String getDefiningEntityName()
    {
        return entityName;
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof LogicalGroupImpl))
        {
            return false;
        }

        if (id == null)
        {
            return false;
        }

        if (entityName == null)
        {
            return false;
        }

        LogicalGroupImpl that = (LogicalGroupImpl)o;

        return id.equals(that.id) && entityName.equals(that.entityName);
    }

    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode()) +
                37 * (entityName == null ? 0 : entityName.hashCode());
    }

    @Override
    public String toString()
    {
        return "LogicalGroup[" + entityName + "[" + id + "]]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
