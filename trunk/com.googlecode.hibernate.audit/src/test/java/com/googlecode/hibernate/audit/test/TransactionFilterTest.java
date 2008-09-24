package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.event.EventSource;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.A;
import com.googlecode.hibernate.audit.test.post_insert.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.TransactionFilter;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.util.NotYetImplementedException;
import com.googlecode.hibernate.audit.model.LogicalGroupIdProvider;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.io.Serializable;

/**
 *@author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class TransactionFilterTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(TransactionFilterTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = false)
    public void testFilterDate() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        final Long currentLG = new Long(7);

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf, new LogicalGroupIdProvider()
            {
                public Serializable getLogicalGroupId(EventSource es,
                                                      Serializable id,
                                                      Object entity)
                {
                    // constant logical group
                    return currentLG;
                }
            });

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            a.setName("alice");

            s.save(a);
            s.getTransaction().commit();

            TransactionFilter f =
                new TransactionFilter(new Date(0), new Date(Long.MAX_VALUE));

            List<AuditTransaction> txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);
            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            Set<EntityDelta> eds = td.getEntityDeltas();
            assert eds.size() == 1;
            EntityDelta ed = eds.iterator().next();
            assert a.getId().equals(ed.getId());
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testFilterDate_Intervals() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        final Long currentLG = new Long(7);

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf, new LogicalGroupIdProvider()
            {
                public Serializable getLogicalGroupId(EventSource es,
                                                      Serializable id,
                                                      Object entity)
                {
                    // constant logical group
                    return currentLG;
                }
            });

            Date t1 = new Date();
            Thread.sleep(1000);

            Session s = sf.openSession();
            s.beginTransaction();

            // insert an entity (both name and age) as current user

            A a = new A();
            a.setName("alice");

            s.save(a);
            s.getTransaction().commit();

            Thread.sleep(1000);
            Date t2 = new Date();
            Thread.sleep(1000);

            s.beginTransaction();

            // insert another entity (both name and age) as current user

            A a2 = new A();
            a2.setName("anna");

            s.save(a2);
            s.getTransaction().commit();

            Thread.sleep(1000);
            Date t3 = new Date();
            Thread.sleep(1000);

            // test (infinty - t1]

            TransactionFilter f = new TransactionFilter(null, t1);

            List<AuditTransaction> txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.isEmpty();

            // test (infinty - t2]

            f = new TransactionFilter(null, t2);

            txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);
            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            Set<EntityDelta> eds = td.getEntityDeltas();
            assert eds.size() == 1;
            EntityDelta ed = eds.iterator().next();
            assert a.getId().equals(ed.getId());

            // test [t1 - t2]

            f = new TransactionFilter(t1, t2);

            txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.size() == 1;
            tx = txs.get(0);
            td = HibernateAudit.getDelta(tx.getId());
            eds = td.getEntityDeltas();
            assert eds.size() == 1;
            ed = eds.iterator().next();
            assert a.getId().equals(ed.getId());

            // test [t2 - t3]

            f = new TransactionFilter(t2, t3);

            txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.size() == 1;
            tx = txs.get(0);
            td = HibernateAudit.getDelta(tx.getId());
            eds = td.getEntityDeltas();
            assert eds.size() == 1;
            ed = eds.iterator().next();

            assert a2.getId().equals(ed.getId());

            // test [t1 - t3]

            f = new TransactionFilter(t1, t3);

            txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.size() == 2;

            tx = txs.get(0);
            td = HibernateAudit.getDelta(tx.getId());
            eds = td.getEntityDeltas();
            assert eds.size() == 1;
            ed = eds.iterator().next();
            assert a.getId().equals(ed.getId());

            tx = txs.get(1);
            td = HibernateAudit.getDelta(tx.getId());
            eds = td.getEntityDeltas();
            assert eds.size() == 1;
            ed = eds.iterator().next();
            assert a2.getId().equals(ed.getId());

            // test [t1 - infinity)

            f = new TransactionFilter(t1, null);

            txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.size() == 2;

            tx = txs.get(0);
            td = HibernateAudit.getDelta(tx.getId());
            eds = td.getEntityDeltas();
            assert eds.size() == 1;
            ed = eds.iterator().next();
            assert a.getId().equals(ed.getId());

            tx = txs.get(1);
            td = HibernateAudit.getDelta(tx.getId());
            eds = td.getEntityDeltas();
            assert eds.size() == 1;
            ed = eds.iterator().next();
            assert a2.getId().equals(ed.getId());

            // test [t2 - infinity)

            f = new TransactionFilter(t2, null);

            txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.size() == 1;

            tx = txs.get(0);
            td = HibernateAudit.getDelta(tx.getId());
            eds = td.getEntityDeltas();
            assert eds.size() == 1;
            ed = eds.iterator().next();
            assert a2.getId().equals(ed.getId());

            // test [t3 - infinity)

            f = new TransactionFilter(t3, null);

            txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.isEmpty();
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testFilterUser() throws Exception
    {
        throw new NotYetImplementedException();
    }

    @Test(enabled = true)
    public void testFilterAuditEntityTypeId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        final Long currentLG = new Long(7);

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf, new LogicalGroupIdProvider()
            {
                public Serializable getLogicalGroupId(EventSource es,
                                                      Serializable id,
                                                      Object entity)
                {
                    // constant logical group
                    return currentLG;
                }
            });

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            a.setName("alice");
            s.save(a);
            s.getTransaction().commit();

            s.beginTransaction();
            B b = new B();
            b.setName("bob");
            s.save(b);
            s.getTransaction().commit();

            s.beginTransaction();
            A a2 = new A();
            a2.setName("anna");
            B b2 = new B();
            b2.setName("ben");
            s.save(a2);
            s.save(b2);
            s.getTransaction().commit();

            TransactionFilter f =
                new TransactionFilter(null, null, null, new Long(3857398753l), null);

            List<AuditTransaction> txs = HibernateAudit.getTransactionsByLogicalGroup(currentLG, f);
            assert txs.isEmpty();
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
