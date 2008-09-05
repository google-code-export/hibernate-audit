package com.googlecode.hibernate.audit.test.post_update;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_update.data.A;
import com.googlecode.hibernate.audit.test.post_update.data.CUni;
import com.googlecode.hibernate.audit.test.post_update.data.DUni;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEntityType;

import java.util.List;
import java.util.ArrayList;

/**
 * A set of post-update tests that look directly into the database and check raw deltas.
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
public class PostUpdateRawTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostUpdateRawTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testPostUpdate_Noop() throws Exception
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

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            log.debug("noop");
            s.update(a);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(a.getId());
            assert txs.size() == 1;

            AuditTransaction tx = txs.get(0);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;

                // making sure there are no UPDATEs
                assert ChangeType.INSERT.equals(ae.getType());
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
    public void testPostUpdate_OneNewPrimitive() throws Exception
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

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            a.setS("shinshila");

            s.update(a);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(a.getId());
            assert txs.size() == 2;

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);
            assert ChangeType.UPDATE.equals(ae.getType());
            assert tx.equals(ae.getTransaction());
            assert a.getId().equals(ae.getTargetId());

            List types = HibernateAudit.query("from AuditEntityType as t where t.className = :cn",
                                              A.class.getName());
            assert types.size() == 1;
            assert types.get(0).equals(ae.getTargetType());

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 1;

            AuditEventPair p = (AuditEventPair)pairs.get(0);
            assert ae.equals(p.getEvent());
            assert p.getField() != null;
            assert "shinshila".equals(p.getValue());
            assert "shinshila".equals(p.getStringValue());
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
    public void testPostUpdate_TwoNewPrimitives() throws Exception
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

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            a.setS("shalimar");
            a.setI(789);

            s.update(a);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(a.getId());
            assert txs.size() == 2;

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);
            assert ChangeType.UPDATE.equals(ae.getType());
            assert tx.equals(ae.getTransaction());
            assert a.getId().equals(ae.getTargetId());

            List types = HibernateAudit.query("from AuditEntityType as t where t.className = :cn",
                                              A.class.getName());
            assert types.size() == 1;
            assert types.get(0).equals(ae.getTargetType());

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 2;

            for(Object o: pairs)
            {
                AuditEventPair p = (AuditEventPair)o;
                assert ae.equals(p.getEvent());
                AuditTypeField f = p.getField();
                String name = f.getName();

                if ("s".equals(name))
                {
                    assert "shalimar".equals(p.getValue());
                    assert "shalimar".equals(p.getStringValue());
                }
                else if ("i".equals(name))
                {
                    assert new Integer(789).equals(p.getValue());
                    assert "789".equals(p.getStringValue());
                }
                else
                {
                    new Error("unexpected field " + name);
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
    public void testPostUpdate_TwoRemovedPrimitives() throws Exception
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

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            a.setS("shazam");
            a.setI(234);
            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            a.setS(null);
            a.setI(null);

            s.update(a);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(a.getId());
            assert txs.size() == 2;

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);
            assert ChangeType.UPDATE.equals(ae.getType());
            assert tx.equals(ae.getTransaction());
            assert a.getId().equals(ae.getTargetId());

            List types = HibernateAudit.query("from AuditEntityType as t where t.className = :cn",
                                              A.class.getName());
            assert types.size() == 1;
            assert types.get(0).equals(ae.getTargetType());

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 2;

            for(Object o: pairs)
            {
                AuditEventPair p = (AuditEventPair)o;
                assert ae.equals(p.getEvent());
                AuditTypeField f = p.getField();
                String name = f.getName();

                if ("s".equals(name))
                {
                    assert p.getValue() == null;
                    assert p.getStringValue() == null;
                }
                else if ("i".equals(name))
                {
                    assert p.getValue() == null;
                    assert p.getStringValue() == null;
                }
                else
                {
                    new Error("unexpected field " + name);
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
    public void testPostUpdate_Collection_AddOne() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(CUni.class);
        config.addAnnotatedClass(DUni.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            CUni c = new CUni();
            s.save(c);

            s.getTransaction().commit();

            s.beginTransaction();

            DUni d = new DUni();
            List<DUni> ds = new ArrayList<DUni>();
            ds.add(d);
            c.setDs(ds);

            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 1;

            // Just one INSERT, no UPDATE

            AuditEvent ae = (AuditEvent)events.get(0);
            assert ChangeType.INSERT.equals(ae.getType());
            assert tx.equals(ae.getTransaction());
            assert d.getId().equals(ae.getTargetId());

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else if (DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }

            }

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 0;
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
