package com.googlecode.hibernate.audit.test.post_insert.data;

import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.tuple.entity.EntityMetamodel;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.HibernateException;
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
public class XA3Tuplizer extends XA3 implements EntityTuplizer 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private EntityMetamodel emm;
    private PersistentClass pc;

    // Constructors --------------------------------------------------------------------------------

    public XA3Tuplizer(EntityMetamodel emm, PersistentClass pc)
    {
        this.emm = emm;
        this.pc = pc;
    }

    public XA3Tuplizer()
    {
        this(null, null);
    }

    // EntityTuplizer implementation ---------------------------------------------------------------

    public Object instantiate(Serializable id) throws HibernateException
    {
        XA3 xa = new XA3();
        xa.id = (Long)id;
        return xa;
    }

    public Serializable getIdentifier(Object entity) throws HibernateException
    {
        XA3 xa = validateType(entity);
        return xa.id;
    }

    public void setIdentifier(Object entity, Serializable id) throws HibernateException
    {
        XA3 xa = validateType(entity);
        xa.id = (Long)id;
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
        XA3 xa = validateType(entity);

        if ("xbs".equals(propertyName))
        {
            xa.xbs = (Set<XB>)value;
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
        XA3 xa = validateType(entity);
        return new Object[] { xa.xbs };
    }

    public Object getPropertyValue(Object entity, String propertyName) throws HibernateException
    {
        XA3 xa = validateType(entity);

        if ("xbs".equals(propertyName))
        {
            return xa.xbs;
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
        XA3 xa = validateType(entity);
        return new Object[] { xa.xbs };
    }

    public void setPropertyValues(Object entity, Object[] values) throws HibernateException
    {
        XA3 xa = validateType(entity);

        if (values.length != 1)
        {
            throw new HibernateException("invalid value array length " + values.length);
        }
        
        xa.xbs = (Set<XB>)values[0];
    }

    public Object getPropertyValue(Object entity, int i) throws HibernateException
    {
        XA3 xa = validateType(entity);

        if (i == 0)
        {
            return xa.xbs;
        }
        else
        {
            throw new HibernateException("invalid property index " + i);
        }
    }

    public Object instantiate() throws HibernateException
    {
        return new XA3();
    }

    public boolean isInstance(Object object)
    {
        return object instanceof XA3;
    }

    public Class getMappedClass()
    {
        return XA3.class;
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

    private XA3 validateType(Object o) throws HibernateException
    {
        if (!(o instanceof XA3))
        {
            throw new HibernateException(o + " not an XA3");
        }

        return (XA3)o;
    }

    // Inner classes -------------------------------------------------------------------------------

}
