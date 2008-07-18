package com.googlecode.hibernate.audit;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;

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
class EntityExpectation
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Class c;
    private Serializable id;

    private Object detachedInstance;

    // TODO we're only handling pojos now
    private EntityMode mode = EntityMode.POJO;

    // Constructors --------------------------------------------------------------------------------

    EntityExpectation(Class c, Serializable id) throws Exception
    {
        this.c = c;
        this.id = id;
    }

    EntityExpectation(SessionFactoryImplementor sf, Class c, Serializable id) throws Exception
    {
        this(c, id);
        initializeDetachedInstance(sf);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (c == null)
        {
            return false;
        }

        if (id == null)
        {
            return false;
        }

        if (!(o instanceof EntityExpectation))
        {
            return false;
        }

        EntityExpectation that = (EntityExpectation)o;

        return c.equals(that.c) && id.equals(that.id);
    }

    @Override
    public int hashCode()
    {
        int result = 17;

        result = 37 * result + (c == null ? 0 : c.hashCode());
        result = 37 * result + (id == null ? 0 : id.hashCode());

        return result;
    }

    @Override
    public String toString()
    {
        return c.getName() + "[" + id + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    Serializable getId()
    {
        return id;
    }

    Class getClassInstance()
    {
        return c;
    }

    Object getDetachedInstance()
    {
        return detachedInstance;
    }

    void initializeDetachedInstance(SessionFactoryImplementor sf) throws Exception
    {
        String name = c.getName();
        EntityPersister p = sf.getEntityPersister(name);
        detachedInstance = p.instantiate(id, mode);
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
