package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.test.util.Formats;
import com.googlecode.hibernate.audit.test.model.base.AuditTypeTestBase;

import java.util.Date;

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
public class AuditTypeTest extends AuditTypeTestBase
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditTypeTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testTypeConversion_UnsupportedType() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(String.class.getName());

        try
        {
            type.valueToString(new Object());
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testTypeConversion_String() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(String.class.getName());

        assert "abc".equals(type.valueToString("abc"));
        assert "abc".equals(type.stringToValue("abc"));
    }

    @Test(enabled = true)
    public void testTypeConversion_Integer() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Integer.class.getName());

        assert "77".equals(type.valueToString(new Integer(77)));
        assert new Integer(77).equals(type.stringToValue("77"));
    }

    @Test(enabled = true)
    public void testTypeConversion_Date() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Date.class.getName());

        // it only supports "oracle" date format so far, this will be a problem in the future
        Date d = Formats.testDateFormat.parse("07/07/2008");
        String timeZone = Formats.timeZone.format(new Date());

        assert ("Mon Jul 07 00:00:00 " + timeZone + " 2008").equals(type.valueToString(d));
        assert d.equals(type.stringToValue("Mon Jul 07 00:00:00 " + timeZone + " 2008"));
    }

    @Test(enabled = true)
    public void testTypeConversion_Long() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Long.class.getName());

        assert "77".equals(type.valueToString(new Long(77)));
        assert new Long(77).equals(type.stringToValue("77"));
    }

    @Test(enabled = true)
    public void testTypeConversion_Boolean() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Boolean.class.getName());

        assert "true".equals(type.valueToString(Boolean.TRUE));
        assert "false".equals(type.valueToString(Boolean.FALSE));
        assert Boolean.TRUE.equals(type.stringToValue("true"));
        assert Boolean.FALSE.equals(type.stringToValue("false"));
    }

    @Test(enabled = true)
    public void testStringToValue_CustomType() throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType.class.getName());

        CustomType ct = (CustomType)t.stringToValue("123");

        assert new CustomType(123).equals(ct);
    }

    @Test(enabled = true)
    public void testStringToValue_NonConvertibleCustomType() throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType.class.getName());

        try
        {
            t.stringToValue("this won't convert into an int");
            throw new Error("should've failed");
        }
        catch(RuntimeException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testStringToValue_CustomType_UseValueOfWithPriority() throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType2.class.getName());

        CustomType2 ct = (CustomType2)t.stringToValue("123");
        assert new CustomType2(123).equals(ct);
    }

    @Test(enabled = true)
    public void testStringToValue_NonConvertibleCustomType_UseValueOfWithPriority()
        throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType2.class.getName());

        try
        {
            t.stringToValue("this won't convert into an int");
            throw new Error("should've failed");
        }
        catch(RuntimeException e)
        {
            Throwable thro = e.getCause();
            assert thro instanceof NumberFormatException;
            log.debug(">>>>>> " + e.getMessage());
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
//                TestAccessHelper.AuditType_getInstanceFromDatabase(Integer.class, false, s);
//            }
//            catch(IllegalStateException e)
//            {
//                log.debug(">>>> " + e.getMessage());
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
//            AuditType at = TestAccessHelper.
//                AuditType_getInstanceFromDatabase(Integer.class, false, s);
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
//            AuditType at = TestAccessHelper.
//                AuditType_getInstanceFromDatabase(Integer.class, true, s);
//
//            assert at.isPrimitiveType();
//            assert !at.isEntityType();
//            assert !at.isCollectionType();
//
//            assert at.getId() != null;
//            assert Integer.class.equals(at.getClassInstance());
//            assert "java.lang.Integer".equals(at.getClassName());
//
//            log.debug(at);
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
//            AuditType at =
//                TestAccessHelper.AuditType_getInstanceFromDatabase(String.class, true, s);
//
//            assert at != null;
//
//            s.getTransaction().commit();
//            s.beginTransaction();
//
//            at = TestAccessHelper.AuditType_getInstanceFromDatabase(String.class, false, s);
//
//            assert at.isPrimitiveType();
//            assert !at.isEntityType();
//            assert !at.isCollectionType();
//
//            assert at.getId() != null;
//            assert String.class.equals(at.getClassInstance());
//            assert "java.lang.String".equals(at.getClassName());
//
//            log.debug(at);
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

    protected AuditType getAuditTypeToTest()
    {
        return new AuditType();
    }

    protected AuditType getAuditTypeToTest(Long id)
    {
        AuditType at = new AuditType();
        at.setId(id);
        return at;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
