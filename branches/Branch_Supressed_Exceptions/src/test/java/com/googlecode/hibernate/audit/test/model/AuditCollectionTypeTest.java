package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.model.base.AuditTypeTestBase;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.HibernateAudit;

import java.util.Collection;
import java.util.Set;
import java.util.List;
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
            AuditCollectionType at = tc.getAuditCollectionType(Set.class, Integer.class);
            assert at.getId() != null;
            assert Integer.class.equals(at.getClassInstance());
            assert Set.class.equals(at.getCollectionClassInstance());
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

            AuditCollectionType at =
                m.getTypeCache().getAuditCollectionType(Set.class, Integer.class);

            assert !at.isPrimitiveType();
            assert !at.isEntityType();
            assert at.isCollectionType();
            assert at.getId() != null;
            assert Integer.class.equals(at.getClassInstance());
            assert Set.class.equals(at.getCollectionClassInstance());

            is.getTransaction().commit();
            is.close();

            // make sure it's in the database
            is = m.getSessionFactory().openSession();
            is.beginTransaction();
            AuditCollectionType dt =
                (AuditCollectionType)is.createQuery("from AuditCollectionType").uniqueResult();
            is.getTransaction().commit();
            is.close();

            assert at.getId().equals(dt.getId());
            assert Integer.class.equals(at.getClassInstance());
            assert Set.class.equals(at.getCollectionClassInstance());
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
            AuditCollectionType at = new AuditCollectionType(List.class, Date.class);
            is.save(at);
            is.getTransaction().commit();
            is.close();

            Session s = sf.openSession();
            s.beginTransaction();

            AuditCollectionType ct =
                m.getTypeCache().getAuditCollectionType(List.class, Date.class);

            assert !ct.isPrimitiveType();
            assert !ct.isEntityType();
            assert ct.isCollectionType();
            assert at.getId().equals(ct.getId());
            assert Date.class.equals(ct.getClassInstance());
            assert List.class.equals(ct.getCollectionClassInstance());

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
