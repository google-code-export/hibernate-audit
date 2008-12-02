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
    private String type;

    // Constructors --------------------------------------------------------------------------------

    public LogicalGroupImpl(Long id, String type)
    {
        this.id = id;
        this.type = type;
    }

    // LogicalGroup implementation -----------------------------------------------------------------

    public Long getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
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

        if (type == null)
        {
            return false;
        }

        LogicalGroupImpl that = (LogicalGroupImpl)o;

        return id.equals(that.id) && type.equals(that.type);
    }

    @Override
    public int hashCode()
    {
        return (id == null ? 0 : id.hashCode()) + 37 * (type == null ? 0 : type.hashCode());
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
