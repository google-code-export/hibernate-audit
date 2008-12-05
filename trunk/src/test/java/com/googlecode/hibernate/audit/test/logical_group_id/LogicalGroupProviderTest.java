package com.googlecode.hibernate.audit.test.logical_group_id;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.event.EventSource;
import org.hibernate.cfg.AnnotationConfiguration;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.logical_group_id.data.A;
import com.googlecode.hibernate.audit.test.logical_group_id.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.LogicalGroupProvider;
import com.googlecode.hibernate.audit.RootProvider;
import com.googlecode.hibernate.audit.LogicalGroup;
import com.googlecode.hibernate.audit.LogicalGroupImpl;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;

import java.util.List;
import java.util.Random;
import java.io.Serializable;

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
public class LogicalGroupProviderTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(LogicalGroupProviderTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testNullLogicalProviderId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();

            List<AuditTransaction> atxs = HibernateAudit.getTransactions(a.getId());

            assert atxs.size() == 1;

            AuditTransaction at = atxs.get(0);

            log.debug(at);

            List<AuditEvent> events = at.getEvents();

            for(AuditEvent e: events)
            {
                assert e.getLogicalGroup() == null;
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
    public void testConstantLogicalProviderId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            final long random = new Random().nextLong();

            LogicalGroupProvider lgip = new LogicalGroupProvider()
            {
                public LogicalGroup getLogicalGroup(EventSource es,
                                                    Serializable id,
                                                    Object entity)
                {
                    return new LogicalGroupImpl(new Long(random), A.class.getName());
                }
            };

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf, lgip);

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            a = new A();
            a.setName("anna");

            s.save(a);

            s.getTransaction().commit();

            List<AuditTransaction> atxs = HibernateAudit.getTransactions(null);

            assert atxs.size() == 2;

            for(AuditTransaction atx: atxs)
            {
                for(AuditEvent e: atx.getEvents())
                {
                    AuditLogicalGroup alg = e.getLogicalGroup();
                    assert new Long(random).equals(alg.getLogicalGroupId());
                    assert A.class.getName().equals(alg.getDefiningEntityName());
                }
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
    public void testLatestTransactionByLogicalGroup_HBANotStarted() throws Exception
    {
        try
        {
            HibernateAudit.getLatestTransactionForLogicalGroup(
                    new LogicalGroupImpl(new Long(0), "doesn't matter"));
            throw new Error("should have failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(">>> " + e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testLatestTransactionByLogicalGroup_EmptyAuditTables() throws Exception
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

            assert null == HibernateAudit.getLatestTransactionForLogicalGroup(
                    new LogicalGroupImpl(new Long(0), B.class.getName()));
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
    public void testLatestTransactionByLogicalGroup_NoSuchLogicalGroup() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf); // null logical group id

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            assert null == HibernateAudit.getLatestTransactionForLogicalGroup(
                    new LogicalGroupImpl(new Long(0), A.class.getName()));
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
    public void testLatestTransactionByLogicalGroup_OneRecord() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            RootProvider rip = new RootProvider(A.class);
            HibernateAudit.register(sf, rip);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            rip.setRoot(a);
            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            LogicalGroup lg = new LogicalGroupImpl(a.getId(), A.class.getName());
            AuditTransaction tx = HibernateAudit.getLatestTransactionForLogicalGroup(lg);

            for(AuditEvent e: tx.getEvents())
            {
                AuditLogicalGroup alg = e.getLogicalGroup();
                assert a.getId().equals(alg.getLogicalGroupId());
                assert A.class.getName().equals(alg.getDefiningEntityName());
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
    public void testLatestTransactionByLogicalGroup_TwoRecords() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            RootProvider rip = new RootProvider(A.class);
            HibernateAudit.register(sf, rip);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            rip.setRoot(a);
            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx1 = txs.get(0);

            LogicalGroup lg = new LogicalGroupImpl(a.getId(), A.class.getName());
            AuditTransaction tx = HibernateAudit.getLatestTransactionForLogicalGroup(lg);

            for(AuditEvent e: tx.getEvents())
            {
                AuditLogicalGroup alg = e.getLogicalGroup();
                assert a.getId().equals(alg.getLogicalGroupId());
                assert A.class.getName().equals(alg.getDefiningEntityName());
            }

            s = sf.openSession();
            s.beginTransaction();

            a = (A)s.get(A.class, a.getId());
            rip.setRoot(a);

            a.setName("blah");
            s.update(a);

            s.getTransaction().commit();
            s.close();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;
            assert tx1.getId().equals(txs.get(0).getId());
            AuditTransaction tx2 = txs.get(1);

            tx = HibernateAudit.getLatestTransactionForLogicalGroup(lg);
            assert tx2.getId().equals(tx.getId());

            for(AuditEvent e: tx2.getEvents())
            {
                AuditLogicalGroup alg = e.getLogicalGroup();
                assert a.getId().equals(alg.getLogicalGroupId());
                assert A.class.getName().equals(alg.getDefiningEntityName());
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

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
