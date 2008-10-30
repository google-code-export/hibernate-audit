package com.googlecode.hibernate.audit.test.post_update.data;

import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.HibernateException;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.engine.SessionImplementor;

import java.io.Serializable;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class CBiTuplizer extends CBi implements EntityTuplizer
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private EntityMetamodel emm;
    private PersistentClass pc;

    // Constructors --------------------------------------------------------------------------------

    public CBiTuplizer(EntityMetamodel emm, PersistentClass pc)
    {
        this.emm = emm;
        this.pc = pc;
    }

    public CBiTuplizer()
    {
        this(null, null);
    }

    // EntityTuplizer implementation ---------------------------------------------------------------

    public Object instantiate(Serializable id) throws HibernateException
    {
        CBi c = new CBi();
        c.setId((Long)id);
        return c;
    }

    public Serializable getIdentifier(Object entity) throws HibernateException
    {
        CBi c = validateType(entity);
        return c.getId();
    }

    public void setIdentifier(Object entity, Serializable id) throws HibernateException
    {
        CBi c = validateType(entity);
        c.setId((Long)id);
    }

    public void resetIdentifier(Object entity, Serializable currentId, Object currentVersion)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object getVersion(Object entity) throws HibernateException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void setPropertyValue(Object entity, int i, Object value) throws HibernateException
    {
       throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void setPropertyValue(Object entity, String propertyName, Object value)
        throws HibernateException
    {
        CBi c = validateType(entity);

        if ("s".equals(propertyName))
        {
            c.setS((String)value);
        }
        else if ("i".equals(propertyName))
        {
            c.setI((Integer)value);
        }
        else if ("ds".equals(propertyName))
        {
            c.setDs((Set<DBi>)value);
        }
        else
        {
            throw new HibernateException("unknown property " + propertyName);
        }
    }

    public Object[] getPropertyValuesToInsert(Object entity, Map mergeMap,
                                              SessionImplementor session)
        throws HibernateException
    {
        CBi c = validateType(entity);
        return new Object[] { c.getS(), c.getI(), c.getDs() };
    }

    public Object getPropertyValue(Object entity, String propertyName) throws HibernateException
    {
        CBi c = validateType(entity);

        if ("s".equals(propertyName))
        {
            return c.getS();
        }
        else if ("i".equals(propertyName))
        {
            return c.getI();
        }
        else if ("ds".equals(propertyName))
        {
            return c.getDs();
        }
        else
        {
            throw new HibernateException("unknown property " + propertyName);
        }
    }

    public void afterInitialize(Object entity, boolean lazyPropertiesAreUnfetched,
                                SessionImplementor session)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean hasProxy()
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object createProxy(Serializable id, SessionImplementor session)
        throws HibernateException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean isLifecycleImplementor()
    {
        return false;
    }

    public boolean isValidatableImplementor()
    {
        return false;
    }

    public Class getConcreteProxyClass()
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean hasUninitializedLazyProperties(Object entity)
    {
        return false;
    }

    public boolean isInstrumented()
    {
        return false;
    }

    public Object[] getPropertyValues(Object entity) throws HibernateException
    {
        CBi c = validateType(entity);
        return new Object[] { c.getS(), c.getI(), c.getDs() };
    }

    public void setPropertyValues(Object entity, Object[] values) throws HibernateException
    {
        CBi c = validateType(entity);

        if (values[0] != null)
        {
            c.setS((String)values[0]);
        }

        if (values[1] != null)
        {
            c.setI((Integer)values[1]);
        }

        if (values[2] != null)
        {
            c.setDs((Set)values[2]);
        }
    }

    public Object getPropertyValue(Object entity, int i) throws HibernateException
    {
        CBi c = validateType(entity);

        if (i == 0)
        {
            return c.getS();
        }
        else if (i == 1)
        {
            return c.getI();
        }
        else if (i == 2)
        {
            return c.getDs();
        }
        else
        {
            throw new HibernateException("invalid index " + i);
        }
    }

    public Object instantiate() throws HibernateException
    {
        return new CBi();
    }

    public boolean isInstance(Object object)
    {
        return object instanceof CBi;
    }

    public Class getMappedClass()
    {
        return CBi.class;
    }

    // Public --------------------------------------------------------------------------------------

    public EntityMetamodel getEntityMetamodel()
    {
        return emm;
    }

    public PersistentClass getPersistentClass()
    {
        return pc;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private CBi validateType(Object o) throws HibernateException
    {
        if (!(o instanceof CBi))
        {
            throw new HibernateException(o + " not an CBi");
        }

        return (CBi)o;
    }

    // Inner classes -------------------------------------------------------------------------------

}