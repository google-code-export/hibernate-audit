package com.googlecode.hibernate.audit;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;

import com.googlecode.hibernate.audit.util.Reflections;

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

    private List<Target> targetEntities;

    // Constructors --------------------------------------------------------------------------------

    EntityExpectation(Class c, Serializable id) throws Exception
    {
        this.c = c;
        this.id = id;
    }

    EntityExpectation(SessionFactoryImplementor sf, Class c, Serializable id) throws Exception
    {
        this(c, id);
        initializeDetachedInstanceIfNecessary(sf);
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
        return c.getName() + "[" + id + "][" + (isFulfilled() ? "" : "NOT ") + "FULFILLED]";
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

    /**
     * Fulfills the expectation, by passing a fully functional entity instance, ready to be
     * forwarded to whoever is waiting for it.
     */
    void fulfill(Object o) throws Exception
    {
        if (!c.isInstance(o))
        {
            throw new IllegalArgumentException(o + "is not a " + c.getName() + " instance");
        }

        // TODO we could also check the id, implement this at some time

        detachedInstance = o;

        if (targetEntities == null)
        {
            // fine, noone wants it
            return;
        }

        // mutate all target entities
        for(Target t: targetEntities)
        {
            Reflections.mutate(t.targetEntity, t.targetMemberName, detachedInstance);
        }
    }

    void addTargetEntity(Object targetEntity, String targetMemberName)
    {
        if (targetEntities == null)
        {
            targetEntities = new ArrayList<Target>();
        }

        targetEntities.add(new Target(targetEntity, targetMemberName));
    }

    boolean isFulfilled()
    {
        return detachedInstance != null;
    }

    /**
     * It's a noop if the detached instance was already initialized.
     */
    void initializeDetachedInstanceIfNecessary(SessionFactoryImplementor sf) throws Exception
    {
        if (detachedInstance != null)
        {
            return;
        }

        String name = c.getName();
        EntityPersister p = sf.getEntityPersister(name);
        detachedInstance = p.instantiate(id, mode);
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class Target
    {
        Object targetEntity;
        String targetMemberName;

        Target(Object targetEntity, String targetMemberName)
        {
            this.targetEntity = targetEntity;
            this.targetMemberName = targetMemberName;
        }

    }
}
