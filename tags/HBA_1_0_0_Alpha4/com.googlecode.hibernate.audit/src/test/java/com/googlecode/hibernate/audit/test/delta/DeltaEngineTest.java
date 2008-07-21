package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.RandomType;
import com.googlecode.hibernate.audit.DeltaEngine;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;

/**
 * Tests the runtime API
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class DeltaEngineTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DeltaEngineTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = false)
    public void testNoSuchEntity() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            RandomType o = new RandomType();

            try
            {
                DeltaEngine.delta((SessionFactoryImplementor)sf, o, null, null);
                throw new Error("should've failed");
            }
            catch(MappingException e)
            {
                log.info(e.getMessage());
            }
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = false)
    public void testInvalidId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            A a = new A();

            try
            {
                DeltaEngine.delta((SessionFactoryImplementor)sf, a, null, null);
                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
            }
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = false)
    public void testNoSuchAuditTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            A a = new A();
            a.setId(new Long(0));

            try
            {
                DeltaEngine.delta((SessionFactoryImplementor)sf, a, null, new Long(23843431223l));
                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
            }
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testNoSuchTypeAudited() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();
            s.close();

            // entity is in the database and it's 'audited' as well

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            B toFillUp = new B();
            toFillUp.setId(new Long(0));

            try
            {
                DeltaEngine.delta((SessionFactoryImplementor)sf, toFillUp, null, at.getId());
                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
            }

            HibernateAudit.disable();
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = false)
    public void testNoSuchEntityIdInDatabase() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();
            s.close();

            // entity is in the database and it's 'audited' as well

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            A toFillUp = new A();
            toFillUp.setId(a.getId().longValue() + 157);

            try
            {
                DeltaEngine.delta((SessionFactoryImplementor)sf, toFillUp, at.getId());
                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
            }

            HibernateAudit.disable();
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = false)
    public void testForwardDelta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            A a = new A();
            a.setName("alice");
            a.setAge(33);

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();
            s.close();

            Long id = a.getId();

            // entity is in the database and it's 'audited' as well

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            A preTransaction = new A();

            A postTransaction = (A)DeltaEngine.
                delta((SessionFactoryImplementor)sf, preTransaction, id, at.getId());

            assert preTransaction != postTransaction;

            assert preTransaction.getId() == null;
            assert id.equals(postTransaction.getId());

            assert preTransaction.getName() == null;
            assert "alice".equals(postTransaction.getName());

            assert preTransaction.getAge() == null;
            assert 33 == postTransaction.getAge();

            HibernateAudit.disable();
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = false)
    public void testDelta_ProtectedConstructor() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(ProtectedConstructorC.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            ProtectedConstructorC c = ProtectedConstructorC.getInstance();
            c.setName("cami");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(c);

            s.getTransaction().commit();
            s.close();

            Long id = c.getId();

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            ProtectedConstructorC preTransaction = ProtectedConstructorC.getInstance();

            ProtectedConstructorC postTransaction = (ProtectedConstructorC)DeltaEngine.
                delta((SessionFactoryImplementor)sf, preTransaction, id, at.getId());

            assert preTransaction != postTransaction;

            assert preTransaction.getId() == null;
            assert id.equals(postTransaction.getId());

            assert preTransaction.getName() == null;
            assert "cami".equals(postTransaction.getName());

            HibernateAudit.disable();
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
