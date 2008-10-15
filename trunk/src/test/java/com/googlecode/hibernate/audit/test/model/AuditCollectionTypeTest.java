package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.model.base.AuditTypeTestBase;
import com.googlecode.hibernate.audit.model.AuditCollectionType;

import java.util.Collection;

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
public class AuditCollectionTypeTest extends AuditTypeTestBase
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditCollectionTypeTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testIllegalCollectionType() throws Exception
    {
        try
        {
            new AuditCollectionType(Integer.class, Integer.class);
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testPersistence_NoActiveTransaction() throws Exception
    {
        throw new Exception("DEPRECATED, CONVERT");
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = HibernateAudit.getManager().getSessionFactory().openSession();
//
//            // DO NOT begin transaction
//
//            try
//            {
//                TestAccessHelper.AuditCollectionType_getInstanceFromDatabase(null, null, false, s);
//            }
//            catch(IllegalStateException e)
//            {
//                log.debug(">>>>> " + e.getMessage());
//            }
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    @Test(enabled = true)
    public void testPersistence_NoTypeInDatabase_DontCreate() throws Exception
    {
        throw new Exception("DEPRECATED, CONVERT");
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = HibernateAudit.getManager().getSessionFactory().openSession();
//            s.beginTransaction();
//
//            AuditCollectionType ct = TestAccessHelper.
//                AuditCollectionType_getInstanceFromDatabase(Collection.class,
//                                                            Integer.class, false, s);
//
//            assert ct == null;
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
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    @Test(enabled = true)
    public void testPersistence_NoTypeInDatabase_Create() throws Exception
    {
        throw new Exception("DEPRECATED, CONVERT");
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = HibernateAudit.getManager().getSessionFactory().openSession();
//            s.beginTransaction();
//
//
//            AuditCollectionType ct = TestAccessHelper.
//                AuditCollectionType_getInstanceFromDatabase(List.class, Long.class, true, s);
//
//            assert !ct.isPrimitiveType();
//            assert !ct.isEntityType();
//            assert ct.isCollectionType();
//
//            assert ct.getId() != null;
//            assert Long.class.equals(ct.getClassInstance());
//            assert List.class.equals(ct.getCollectionClassInstance());
//
//            log.debug(ct);
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
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    @Test(enabled = true)
    public void testPersistence_TypeAlreadyInDatabase() throws Exception
    {
        throw new Exception("DEPRECATED, CONVERT");
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = HibernateAudit.getManager().getSessionFactory().openSession();
//            s.beginTransaction();
//
//            AuditCollectionType ct =
//                TestAccessHelper.AuditCollectionType_getInstanceFromDatabase(Set.class,
//                                                                             String.class, true, s);
//
//            assert ct != null;
//
//            s.getTransaction().commit();
//            s.beginTransaction();
//
//            ct = TestAccessHelper.
//                AuditCollectionType_getInstanceFromDatabase(Set.class, String.class, false, s);
//
//            assert !ct.isPrimitiveType();
//            assert !ct.isEntityType();
//            assert ct.isCollectionType();
//
//            assert ct.getId() != null;
//            assert String.class.equals(ct.getClassInstance());
//            assert Set.class.equals(ct.getCollectionClassInstance());
//
//            log.debug(ct);
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
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected AuditCollectionType getAuditTypeToTest()
    {
        return new AuditCollectionType(Collection.class, (Class)null);
    }

    protected AuditCollectionType getAuditTypeToTest(Long id)
    {
        AuditCollectionType at = new AuditCollectionType(Collection.class, (Class)null);
        at.setId(id);
        return at;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
