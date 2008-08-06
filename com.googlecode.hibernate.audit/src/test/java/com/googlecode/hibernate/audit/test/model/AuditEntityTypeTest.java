package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import com.googlecode.hibernate.audit.test.model.base.AuditTypeTestBase;
import com.googlecode.hibernate.audit.test.model.data.A;
import com.googlecode.hibernate.audit.model.AuditEntityType;

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
        throw new Exception("NOT YET IMPLEMENTED");

//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactory sf = null;
//
//        try
//        {
//            sf = config.buildSessionFactory();
//            StatelessSession s = sf.openStatelessSession();
//
//            // DO NOT begin transaction
//
//            try
//            {
//                AuditEntityType.getInstanceFromDatabase(Integer.class, false, s);
//            }
//            catch(IllegalStateException e)
//            {
//                log.debug(e.getMessage());
//            }
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    @Test(enabled = true)
    public void testPersistence_NoTypeInDatabase_DontCreate() throws Exception
    {
        throw new Exception("NOT YET IMPLEMENTED");

//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactory sf = null;
//
//        try
//        {
//            sf = config.buildSessionFactory();
//            StatelessSession s = sf.openStatelessSession();
//            s.beginTransaction();
//
//            AuditEntityType at = AuditEntityType.
//                getInstanceFromDatabase(EntityType.class, null, false, s);
//
//            assert at == null;
//
//            s.getTransaction().commit();
//            s.close();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    @Test(enabled = true)
    public void testPersistence_NoTypeInDatabase_Create() throws Exception
    {
        throw new Exception("NOT YET IMPLEMENTED");

//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactory sf = null;
//
//        try
//        {
//            sf = config.buildSessionFactory();
//            StatelessSession s = sf.openStatelessSession();
//            s.beginTransaction();
//
//            AuditEntityType et = AuditEntityType.
//                getInstanceFromDatabase(EntityType.class, Long.class, true, s);
//
//            assert !et.isPrimitiveType();
//            assert et.isEntityType();
//            assert !et.isCollectionType();
//
//            assert et.getId() != null;
//            assert EntityType.class.equals(et.getClassInstance());
//            assert Long.class.equals(et.getIdClassInstance());
//
//            log.debug(et);
//
//            s.getTransaction().commit();
//            s.close();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    @Test(enabled = true)
    public void testPersistence_TypeAlreadyInDatabase() throws Exception
    {
        throw new Exception("NOT YET IMPLEMENTED");

//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactory sf = null;
//
//        try
//        {
//            sf = config.buildSessionFactory();
//            StatelessSession s = sf.openStatelessSession();
//            s.beginTransaction();
//
//            AuditEntityType et = AuditEntityType.
//                getInstanceFromDatabase(EntityType.class, Long.class, true, s);
//
//            assert et != null;
//
//            s.getTransaction().commit();
//            s.beginTransaction();
//
//            et = AuditEntityType.getInstanceFromDatabase(EntityType.class, null, false, s);
//
//            assert !et.isPrimitiveType();
//            assert et.isEntityType();
//            assert !et.isCollectionType();
//
//            assert et.getId() != null;
//            assert EntityType.class.equals(et.getClassInstance());
//            assert Long.class.equals(et.getIdClassInstance());
//
//            log.debug(et);
//
//            s.getTransaction().commit();
//            s.close();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
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

}
