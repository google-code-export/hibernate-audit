package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.HibernateException;
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
public class HibernateAuditHistoryAccessorsTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAuditHistoryAccessorsTest.class);

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

    @Test(enabled = true)
    public void testGetValue_HBANotStarted() throws Exception
    {
        try
        {
            HibernateAudit.getValue(null, "doesn't matter", "doesn't matter", "dsnmtr", 10l);
            throw new Error("should have failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(">>> " + e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testGetValue_EmptyAuditTables() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());

            try
            {
                HibernateAudit.getValue(sf, "com.googlecode.hibernate.audit.test.data.A",
                                        new Long(1), "dsntmttr", new Long(10));
                throw new Exception("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.debug(">>> " + e.getMessage());
            }
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
    public void testGetValue_EntityNotSeenYet() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();
            s.beginTransaction();
            B b = new B();
            s.save(b);
            s.getTransaction().commit();
            s.close();

            try
            {
                HibernateAudit.getValue(sf, "com.googlecode.hibernate.audit.test.data.A",
                                        new Long(1), "dsntmttr", new Long(10));
                throw new Exception("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.debug(">>> " + e.getMessage());
            }
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
    public void testGetValue_WrongEntityIdType() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();
            s.beginTransaction();
            B b = new B();
            s.save(b);
            s.getTransaction().commit();
            s.close();

            try
            {
                HibernateAudit.getValue(sf, "com.googlecode.hibernate.audit.test.data.A",
                                        "wrongid", "dsntmttr", new Long(10));
                throw new Exception("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.debug(">>> " + e.getMessage());
            }
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
    public void testGetValue_UnknownField() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();
            s.beginTransaction();
            B b = new B();
            s.save(b);
            s.getTransaction().commit();
            s.close();

            try
            {
                HibernateAudit.getValue(sf, "com.googlecode.hibernate.audit.test.data.B",
                                        new Long(777), "noSuchField", new Long(10));
                throw new Exception("should've failed");
            }
            catch(HibernateException e)
            {
                log.debug(">>> " + e.getMessage());
            }
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
    public void testGetValue_UnknownEntityId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();
            s.beginTransaction();
            B b = new B();
            b.setS("s");
            s.save(b);
            s.getTransaction().commit();
            s.close();

            try
            {
                HibernateAudit.getValue(sf, "com.googlecode.hibernate.audit.test.data.B",
                                        new Long(32847239), "s", new Long(10));
                throw new Exception("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.debug(">>> " + e.getMessage());
            }
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
    public void testGetValue_InvalidVersion() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();
            s.beginTransaction();
            B b = new B();
            b.setS("s");
            s.save(b);
            s.getTransaction().commit();
            s.close();

            try
            {
                HibernateAudit.getValue(sf, "com.googlecode.hibernate.audit.test.data.B",
                                        b.getId(), "s", new Long(-1));
                throw new Exception("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.debug(">>> " + e.getMessage());
            }
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
    public void testGetValue() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();
            s.beginTransaction();
            B b = new B();
            b.setS("s");
            s.save(b);
            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            Long currentVersion = txs.get(0).getId();

            String v = (String)HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "s", currentVersion);

            assert "s".equals(v);
            
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
    public void testGetValue_InsertAndUpdate() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();

            s.beginTransaction();
            B b = new B();
            b.setS("s");
            s.save(b);
            s.getTransaction().commit();

            s.beginTransaction();
            b.setS("s2");
            s.update(b);
            s.getTransaction().commit();

            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            Long currentVersion = txs.get(1).getId();

            String v = (String)HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "s", currentVersion);

            assert "s2".equals(v);

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
    public void testGetValue_InsertAndTwoUpdates() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();

            s.beginTransaction();
            B b = new B();
            b.setS("s");
            s.save(b);
            s.getTransaction().commit();

            s.beginTransaction();
            b.setS("s2");
            s.update(b);
            s.getTransaction().commit();

            s.beginTransaction();
            b.setS("s3");
            s.update(b);
            s.getTransaction().commit();

            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 3;

            Long currentVersion = txs.get(2).getId();

            String v = (String)HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "s", currentVersion);

            assert "s3".equals(v);

            v = (String)HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "s", new Long(384328427432l));

            assert "s3".equals(v);
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
    public void testGetValue_NullAndOldValue() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();

            s.beginTransaction();
            B b = new B();
            b.setS("s");
            s.save(b);

            // TODO
            // 'decoy', get rid of this when https://jira.novaordis.org/browse/HBA-172 is fixed
            B b2 = new B();
            b2.setI(1);
            s.save(b2);
            // end of 'decoy'

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            Long currentVersion = txs.get(0).getId();

            assert null == HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "i", currentVersion);

            s.beginTransaction();
            b.setS("s2");
            b.setI(10);
            s.update(b);
            s.getTransaction().commit();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            currentVersion = txs.get(1).getId();

            Integer i = (Integer)HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "i", currentVersion);

            assert new Integer(10).equals(i);

            s.beginTransaction();
            b.setS("s3");
            s.update(b);
            s.getTransaction().commit();

            s.close();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 3;

            currentVersion = txs.get(2).getId();

            i = (Integer)HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "i", currentVersion);

            assert new Integer(10).equals(i);
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

    /**
     * https://jira.novaordis.org/browse/HBA-172
     */
    @Test(enabled = true)
    public void testGetValue_FieldNotSeenEvenIfExists() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // enter B in the audit tables
            Session s = sf.openSession();

            s.beginTransaction();
            B b = new B();
            b.setS("s");
            s.save(b);
            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            Long currentVersion = txs.get(0).getId();

            assert null == HibernateAudit.
                getValue(sf, B.class.getName(), b.getId(), "i", currentVersion);
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
