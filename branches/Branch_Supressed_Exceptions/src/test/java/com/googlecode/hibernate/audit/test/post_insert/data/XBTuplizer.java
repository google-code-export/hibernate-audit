package com.googlecode.hibernate.audit.test.post_insert.data;

import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.HibernateException;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.engine.SessionImplementor;

import java.io.Serializable;
import java.util.Map;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class XBTuplizer extends XB implements EntityTuplizer
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private EntityMetamodel emm;
    private PersistentClass pc;

    // Constructors --------------------------------------------------------------------------------

    public XBTuplizer(EntityMetamodel emm, PersistentClass pc)
    {
        this.emm = emm;
        this.pc = pc;
    }

    public XBTuplizer()
    {
        this(null, null);
    }

    // EntityTuplizer implementation ---------------------------------------------------------------

    public Object instantiate(Serializable id) throws HibernateException
    {
        XB xb = new XB();
        xb.id = (Long)id;
        return xb;
    }

    public Serializable getIdentifier(Object entity) throws HibernateException
    {
        XB xb = validateType(entity);
        return xb.id;
    }

    public void setIdentifier(Object entity, Serializable id) throws HibernateException
    {
        XB xb = validateType(entity);
        xb.id = (Long)id;
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
        XB xb = validateType(entity);

        if ("name".equals(propertyName))
        {
            xb.name = (String)value;
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
        XB xb = validateType(entity);
        return new Object[] { xb.name }; 
    }

    public Object getPropertyValue(Object entity, String propertyName) throws HibernateException
    {
        XB xb = validateType(entity);

        if ("name".equals(propertyName))
        {
            return xb.name;
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
        XB xb = validateType(entity);
        return new Object[] { xb.name };
    }

    public void setPropertyValues(Object entity, Object[] values) throws HibernateException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object getPropertyValue(Object entity, int i) throws HibernateException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Object instantiate() throws HibernateException
    {
        return new XB();
    }

    public boolean isInstance(Object object)
    {
        return object instanceof XB;
    }

    public Class getMappedClass()
    {
        return XB.class;
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

    private XB validateType(Object o) throws HibernateException
    {
        if (!(o instanceof XB))
        {
            throw new HibernateException(o + " not an XB");
        }

        return (XB)o;
    }

    // Inner classes -------------------------------------------------------------------------------

}
