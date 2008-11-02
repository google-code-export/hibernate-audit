package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.test.util.Formats;
import com.googlecode.hibernate.audit.test.model.base.AuditTypeTestBase;
import com.googlecode.hibernate.audit.HibernateAudit;

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

    @Test(enabled = false)
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

    @Test(enabled = false)
    public void testTypeConversion_String() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(String.class.getName());

        assert "abc".equals(type.valueToString("abc"));
        assert "abc".equals(type.stringToValue("abc"));
    }

    @Test(enabled = false)
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
        String timeZone = Formats.timeZone.format(d);

        log.debug(">>> Mon Jul 07 00:00:00 " + timeZone + " 2008 = " + type.valueToString(d));
        assert ("Mon Jul 07 00:00:00 " + timeZone + " 2008").equals(type.valueToString(d));
        assert d.equals(type.stringToValue("Mon Jul 07 00:00:00 " + timeZone + " 2008"));
    }

    @Test(enabled = false)
    public void testTypeConversion_Long() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Long.class.getName());

        assert "77".equals(type.valueToString(new Long(77)));
        assert new Long(77).equals(type.stringToValue("77"));
    }

    @Test(enabled = false)
    public void testTypeConversion_Boolean() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Boolean.class.getName());

        assert "true".equals(type.valueToString(Boolean.TRUE));
        assert "false".equals(type.valueToString(Boolean.FALSE));
        assert Boolean.TRUE.equals(type.stringToValue("true"));
        assert Boolean.FALSE.equals(type.stringToValue("false"));
    }

    @Test(enabled = false)
    public void testStringToValue_CustomType() throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType.class.getName());

        CustomType ct = (CustomType)t.stringToValue("123");

        assert new CustomType(123).equals(ct);
    }

    @Test(enabled = false)
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

    @Test(enabled = false)
    public void testStringToValue_CustomType_UseValueOfWithPriority() throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType2.class.getName());

        CustomType2 ct = (CustomType2)t.stringToValue("123");
        assert new CustomType2(123).equals(ct);
    }

    @Test(enabled = false)
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

    @Test(enabled = false)
    public void testPersistence_NoActiveTransaction() throws Exception
    {
        log.debug("testPersistence_NoActiveTransaction");
        
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
            AuditType at = tc.getAuditPrimitiveType(Integer.class);
            assert at.getId() != null;
            assert Integer.class.equals(at.getClassInstance());
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

    @Test(enabled = false)
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

            AuditType at = m.getTypeCache().getAuditPrimitiveType(Integer.class);

            assert at.isPrimitiveType();
            assert !at.isEntityType();
            assert !at.isCollectionType();
            assert at.getId() != null;
            assert Integer.class.equals(at.getClassInstance());
            assert "java.lang.Integer".equals(at.getClassName());

            is.getTransaction().commit();
            is.close();

            // make sure it's in the database
            is = m.getSessionFactory().openSession();
            is.beginTransaction();
            AuditType dt = (AuditType) is.createQuery("from AuditType").uniqueResult();
            is.getTransaction().commit();
            is.close();

            assert at.getId().equals(dt.getId());
            assert Integer.class.equals(at.getClassInstance());

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

    @Test(enabled = false)
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
            AuditType at = new AuditType();
            at.setClassName("java.lang.Long");
            is.save(at);
            is.getTransaction().commit();
            is.close();

            Session s = sf.openSession();
            s.beginTransaction();

            AuditType ct = m.getTypeCache().getAuditPrimitiveType(Long.class);

            assert ct.isPrimitiveType();
            assert !ct.isEntityType();
            assert !ct.isCollectionType();
            assert at.getId().equals(ct.getId());
            assert Long.class.equals(ct.getClassInstance());
            assert "java.lang.Long".equals(ct.getClassName());

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
