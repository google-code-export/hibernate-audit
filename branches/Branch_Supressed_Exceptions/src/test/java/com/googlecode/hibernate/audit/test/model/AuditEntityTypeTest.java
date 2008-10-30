package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.model.base.AuditTypeTestBase;
import com.googlecode.hibernate.audit.test.model.data.A;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.HibernateAudit;

import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class AuditEntityTypeTest extends AuditTypeTestBase
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditEntityTypeTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testValueToString_InvalidState() throws Exception
    {
        AuditEntityType type = new AuditEntityType((Class)null, (Class)null);

        try
        {
            type.valueToString("doesn't matter");
            throw new Error("should've failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testValueToString_InvalidId() throws Exception
    {
        Method m = A.class.getMethod("getId");
        AuditEntityType type = new AuditEntityType(m.getReturnType(), A.class);

        try
        {
            type.valueToString(new RandomType());
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testValueToString_ValidId() throws Exception
    {
        Method m = A.class.getMethod("getId");
        AuditEntityType type = new AuditEntityType(m.getReturnType(), A.class);

        assert "7777".equals(type.valueToString(new Long(7777)));
    }

    @Test(enabled = true)
    public void testPersistence_NoActiveTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            HibernateAudit.getManager().getSessionFactory().openSession();

            // DO NOT begin transaction

            TypeCache tc = HibernateAudit.getManager().getTypeCache();
            AuditEntityType at = tc.getAuditEntityType(ExoticIdType.class, EntityType.class);
            assert at.getId() != null;
            assert EntityType.class.equals(at.getClassInstance());
            assert ExoticIdType.class.equals(at.getIdClassInstance());
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testPersistence_NoTypeInDatabase_Create_MessWithInternalSession() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Manager m = HibernateAudit.getManager();
            Session is = m.getSessionFactory().openSession();
            is.beginTransaction();

            AuditEntityType at =
                m.getTypeCache().getAuditEntityType(ExoticIdType.class, EntityType.class);

            assert !at.isPrimitiveType();
            assert at.isEntityType();
            assert !at.isCollectionType();
            assert at.getId() != null;
            assert EntityType.class.equals(at.getClassInstance());
            assert ExoticIdType.class.equals(at.getIdClassInstance());

            is.getTransaction().commit();
            is.close();

            // make sure it's in the database
            is = m.getSessionFactory().openSession();
            is.beginTransaction();
            AuditEntityType dt =
                (AuditEntityType)is.createQuery("from AuditEntityType").uniqueResult();
            is.getTransaction().commit();
            is.close();

            assert at.getId().equals(dt.getId());
            assert EntityType.class.equals(at.getClassInstance());
            assert ExoticIdType.class.equals(at.getIdClassInstance());
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testPersistence_TypeAlreadyInDatabase() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);
            Manager m = HibernateAudit.getManager();

            Session is = m.getSessionFactory().openSession();
            is.beginTransaction();
            AuditEntityType at = new AuditEntityType(ExoticIdType.class, EntityType.class);
            is.save(at);
            is.getTransaction().commit();
            is.close();

            Session s = sf.openSession();
            s.beginTransaction();

            AuditEntityType ct =
                m.getTypeCache().getAuditEntityType(ExoticIdType.class, EntityType.class);

            assert !ct.isPrimitiveType();
            assert ct.isEntityType();
            assert !ct.isCollectionType();
            assert at.getId().equals(ct.getId());
            assert EntityType.class.equals(ct.getClassInstance());
            assert ExoticIdType.class.equals(ct.getIdClassInstance());

            s.getTransaction().commit();
            s.close();
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected AuditEntityType getAuditTypeToTest()
    {
        return new AuditEntityType((Class)null, (Class)null);
    }

    protected AuditEntityType getAuditTypeToTest(Long id)
    {
        AuditEntityType at = new AuditEntityType((Class)null, (Class)null);
        at.setId(id);
        return at;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class EntityType
    {
    }

    private class RandomType
    {
    }

    private class ExoticIdType
    {
    }

}
