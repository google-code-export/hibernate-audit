package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
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
        assert "Mon Jul 07 00:00:00 PDT 2008".equals(type.valueToString(d));
        assert d.equals(type.stringToValue("Mon Jul 07 00:00:00 PDT 2008"));
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
//                AuditType.getInstanceFromDatabase(Integer.class, false, s);
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
//            AuditType at = AuditType.getInstanceFromDatabase(Integer.class, false, s);
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
//            AuditType at = AuditType.getInstanceFromDatabase(Integer.class, true, s);
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
//            AuditType at = AuditType.getInstanceFromDatabase(String.class, true, s);
//
//            assert at != null;
//
//            s.getTransaction().commit();
//            s.beginTransaction();
//
//            at = AuditType.getInstanceFromDatabase(String.class, false, s);
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
