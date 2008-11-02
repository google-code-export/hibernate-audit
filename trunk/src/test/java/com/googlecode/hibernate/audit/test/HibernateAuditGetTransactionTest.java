package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.data.A;
import com.googlecode.hibernate.audit.test.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;

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
public class HibernateAuditGetTransactionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAuditGetTransactionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testLatestTransaction_HBANotStarted() throws Exception
    {
        try
        {
            HibernateAudit.getLatestTransaction("doesn't matter", "doesn't matter");
            throw new Error("should have failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(">>> " + e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testLatestTransaction_EmptyAuditTables() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());

            assert null == HibernateAudit.getLatestTransaction("doesn't matter", "doesn't matter");
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
    public void testLatestTransaction_NoSuchEntityName() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();
            A a = new A();
            s.save(a);
            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            assert null == HibernateAudit.getLatestTransaction("noSuchEntity", a.getId());
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
    public void testLatestTransaction_NoEntityWithSuchId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();
            A a = new A();
            s.save(a);
            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            assert null == HibernateAudit.getLatestTransaction(A.class.getName(), new Long(842424));
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
    public void testLatestTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();
            A a = new A();
            s.save(a);
            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            Long changeId = txs.get(0).getId();

            AuditTransaction atx = HibernateAudit.getLatestTransaction(A.class.getName(), a.getId());

            assert changeId.equals(atx.getId());
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
    public void testLatestTransaction_MultipleUpdates() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();

            s.beginTransaction();
            A a = new A();
            s.save(a);
            s.getTransaction().commit();

            s.beginTransaction();
            a.setI(1);
            s.update(a);
            s.getTransaction().commit();

            s.beginTransaction();
            a.setS("s");
            s.update(a);
            s.getTransaction().commit();

            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 3;
            Long latestChangeId = txs.get(2).getId();

            AuditTransaction atx = HibernateAudit.getLatestTransaction(A.class.getName(), a.getId());

            assert latestChangeId.equals(atx.getId());
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
    public void testLatestTransaction_MultipleEntities_MultipleUpdates() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();

            s.beginTransaction();
            A a = new A();
            s.save(a);
            s.getTransaction().commit();

            s.beginTransaction();
            a.getBs().add(new B());
            s.update(a);
            s.getTransaction().commit();

            s.beginTransaction();
            B b = a.getBs().get(0);
            b.setS("scramble");
            s.update(a);
            s.getTransaction().commit();

            s.beginTransaction();
            a = (A)s.get(A.class, a.getId());
            a.setS("unscrable");
            s.update(a);
            s.getTransaction().commit();

            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 4;
            Long latestChangeId = txs.get(2).getId();

            AuditTransaction atx = HibernateAudit.getLatestTransaction(B.class.getName(), b.getId());

            assert latestChangeId.equals(atx.getId());
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

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
