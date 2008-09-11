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
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;

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

    // Primitive Updates ---------------------------------------------------------------------------

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
    public void testPostUpdate_ChangeAPrimitive() throws Exception
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

            a.setS("sabar");
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
            AuditTypeField f = p.getField();
            AuditType ft = f.getType();
            assert "s".equals(f.getName());
            assert ft.isPrimitiveType();
            assert String.class.equals(ft.getClassInstance());
            assert "sabar".equals(p.getValue());
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

    // Collection Updates --------------------------------------------------------------------------

    @Test(enabled = true)
    public void testPostUpdate_EmptyCollection_AddOne() throws Exception
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

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
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

            assert pairs.size() == 0; // no fields in for the new D instance
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
    public void testPostUpdate_EmptyCollection_AddTwo() throws Exception
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

            List<DUni> ds = new ArrayList<DUni>();
            DUni done = new DUni();
            DUni dtwo = new DUni();
            dtwo.setI(7);

            ds.add(done);
            ds.add(dtwo);

            c.setDs(ds);

            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }

            }

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            // two INSERTs, no UPDATE
            assert events.size() == 2;

            AuditEvent ae = (AuditEvent)events.get(0);
            assert ChangeType.INSERT.equals(ae.getType());
            assert tx.equals(ae.getTransaction());
            assert done.getId().equals(ae.getTargetId());

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 0; // no fields in for the first D instance

            ae = (AuditEvent)events.get(1);
            assert ChangeType.INSERT.equals(ae.getType());
            assert tx.equals(ae.getTransaction());
            assert dtwo.getId().equals(ae.getTargetId());

            pairs = HibernateAudit.query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 1;

            AuditEventPair p = (AuditEventPair)pairs.get(0);
            assert ae.equals(p.getEvent());
            AuditTypeField f = p.getField();
            assert "i".equals(f.getName());
            assert Integer.class.equals(f.getType().getClassInstance());
            assert new Integer(7).equals(p.getValue());
            assert "7".equals(p.getStringValue());
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
    public void testPostUpdate_CollectionOfOne_AddAnotherOne() throws Exception
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
            List<DUni> ds = new ArrayList<DUni>();
            DUni done = new DUni();
            ds.add(done);
            c.setDs(ds);

            s.save(c);

            s.getTransaction().commit();

            s.beginTransaction();

            DUni dtwo = new DUni();
            dtwo.setI(7);
            c.getDs().add(dtwo);
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }
            }

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 2;

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;
                ChangeType ct = ae.getType();
                assert tx.equals(ae.getTransaction());
                List pairs = HibernateAudit.
                    query("from AuditEventPair as p where p.event = :event", ae);

                if (ChangeType.INSERT.equals(ct))
                {
                    assert dtwo.getId().equals(ae.getTargetId());
                    assert pairs.size() == 1;
                    AuditEventPair p = (AuditEventPair)pairs.get(0);
                    assert ae.equals(p.getEvent());
                    AuditTypeField f = p.getField();
                    assert "i".equals(f.getName());
                    assert Integer.class.equals(f.getType().getClassInstance());
                    assert new Integer(7).equals(p.getValue());
                    assert "7".equals(p.getStringValue());
                }
                else if (ChangeType.UPDATE.equals(ct))
                {
                    assert c.getId().equals(ae.getTargetId());
                    assert pairs.size() == 1;
                    AuditEventPair p = (AuditEventPair)pairs.get(0);
                    assert ae.equals(p.getEvent());
                    AuditTypeField f = p.getField();
                    assert "ds".equals(f.getName());
                    List newids = (List)p.getValue();
                    assert newids.size() == 2;
                    assert newids.contains(done.getId());
                    assert newids.contains(dtwo.getId());
                }
                else
                {
                    throw new Error("invalid state");
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
    public void testPostUpdate_CollectionOfOne_AddTwo() throws Exception
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
            List<DUni> ds = new ArrayList<DUni>();
            DUni done = new DUni();
            ds.add(done);
            c.setDs(ds);

            s.save(c);

            s.getTransaction().commit();

            s.beginTransaction();

            DUni dtwo = new DUni();
            dtwo.setI(8);
            DUni dthree = new DUni();
            dthree.setS("sonoma");

            c.getDs().add(dtwo);
            c.getDs().add(dthree);
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }

            }

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 3;

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;
                assert tx.equals(ae.getTransaction());
                ChangeType ct = ae.getType();
                Object id = ae.getTargetId();
                List pairs = HibernateAudit.
                    query("from AuditEventPair as p where p.event = :event", ae);

                if (c.getId().equals(id))
                {
                    assert ChangeType.UPDATE.equals(ct);
                    assert pairs.size() == 1;
                    AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);
                    assert ae.equals(p.getEvent());
                    AuditTypeField f = p.getField();
                    assert "ds".equals(f.getName());
                    assert DUni.class.equals(f.getType().getClassInstance());
                    assert List.class.equals(((AuditCollectionType)f.getType()).
                        getCollectionClassInstance());

                    List ids = (List)p.getValue();
                    assert ids.size() == 3;
                    assert ids.contains(done.getId());
                    assert ids.contains(dtwo.getId());
                    assert ids.contains(dthree.getId());
                }
                else if (dtwo.getId().equals(id))
                {
                    assert ChangeType.INSERT.equals(ct);
                    assert pairs.size() == 1;
                    AuditEventPair p = (AuditEventPair)pairs.get(0);
                    assert ae.equals(p.getEvent());
                    AuditTypeField f = p.getField();
                    assert "i".equals(f.getName());
                    assert Integer.class.equals(f.getType().getClassInstance());
                    assert new Integer(8).equals(p.getValue());
                    assert "8".equals(p.getStringValue());
                }
                else if (dthree.getId().equals(id))
                {
                    assert ChangeType.INSERT.equals(ct);
                    assert pairs.size() == 1;

                    AuditEventPair p = (AuditEventPair)pairs.get(0);
                    assert ae.equals(p.getEvent());
                    AuditTypeField f = p.getField();
                    assert "s".equals(f.getName());
                    assert String.class.equals(f.getType().getClassInstance());
                    assert "sonoma".equals(p.getValue());
                    assert "sonoma".equals(p.getStringValue());
                }
                else
                {
                    throw new Error("invalid state, id = " + id);
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
    public void testPostUpdate_CollectionOfOne_UpdateExistingsContent() throws Exception
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
            List<DUni> ds = new ArrayList<DUni>();
            DUni done = new DUni();
            ds.add(done);
            c.setDs(ds);

            s.save(c);

            s.getTransaction().commit();

            s.beginTransaction();

            done.setI(9);
            done.setS("sith");

            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }

            }

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            // one UPDATE
            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);
            assert ChangeType.UPDATE.equals(ae.getType());
            assert tx.equals(ae.getTransaction());
            assert done.getId().equals(ae.getTargetId());

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 2;

            AuditEventPair p = (AuditEventPair)pairs.get(0);

            assert ae.equals(p.getEvent());
            AuditTypeField f = p.getField();
            assert "i".equals(f.getName());
            assert Integer.class.equals(f.getType().getClassInstance());
            assert new Integer(9).equals(p.getValue());
            assert "9".equals(p.getStringValue());

            p = (AuditEventPair)pairs.get(1);
            assert ae.equals(p.getEvent());
            f = p.getField();
            assert "s".equals(f.getName());
            assert String.class.equals(f.getType().getClassInstance());
            assert "sith".equals(p.getValue());
            assert "sith".equals(p.getStringValue());
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
    public void testPostUpdate_CollectionOfOne_RemoveExisting() throws Exception
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
            List<DUni> ds = new ArrayList<DUni>();
            DUni done = new DUni();
            ds.add(done);
            c.setDs(ds);

            s.save(c);

            s.getTransaction().commit();

            s.beginTransaction();

            c.getDs().clear();
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            // check entity types

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }

            }

            // check collection types

            types = HibernateAudit.query("from AuditCollectionType");
            assert types.size() == 1;

            AuditCollectionType act = (AuditCollectionType)types.get(0);
            assert List.class.equals(act.getCollectionClassInstance());
            assert DUni.class.equals(act.getClassInstance());

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            // one collection UPDATE
            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);
            assert tx.equals(ae.getTransaction());
            assert c.getId().equals(ae.getTargetId());
            assert ChangeType.UPDATE.equals(ae.getType());

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 1;

            AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);

            assert ae.equals(p.getEvent());
            AuditTypeField f = p.getField();
            assert "ds".equals(f.getName());
            AuditCollectionType ct = (AuditCollectionType)f.getType();
            assert List.class.equals(ct.getCollectionClassInstance());
            assert DUni.class.equals(ct.getClassInstance());

            List<Long> value = p.getIds();
            Object o = p.getValue();
            assert o == value;

            assert value.isEmpty();
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
    public void testPostUpdate_CollectionOfOne_ReplaceExistingWithAnotherOne() throws Exception
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
            List<DUni> ds = new ArrayList<DUni>();
            DUni done = new DUni();
            ds.add(done);
            c.setDs(ds);

            s.save(c);

            s.getTransaction().commit();

            s.beginTransaction();

            DUni dtwo = new DUni();
            c.getDs().clear();
            c.getDs().add(dtwo);
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            // check entity types

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }

            }

            // check collection types

            types = HibernateAudit.query("from AuditCollectionType");
            assert types.size() == 1;

            AuditCollectionType act = (AuditCollectionType)types.get(0);
            assert List.class.equals(act.getCollectionClassInstance());
            assert DUni.class.equals(act.getClassInstance());

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            // one insert and one collection update
            assert events.size() == 2;

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;
                ChangeType ct = ae.getType();
                assert tx.equals(ae.getTransaction());

                if (ChangeType.INSERT.equals(ct))
                {
                    // entity insert
                    assert dtwo.getId().equals(ae.getTargetId());

                    List pairs = HibernateAudit.
                        query("from AuditEventPair as p where p.event = :event", ae);

                    assert pairs.size() == 0;
                }
                else if (ChangeType.UPDATE.equals(ct))
                {
                    // collection update
                    assert c.getId().equals(ae.getTargetId());

                    List pairs = HibernateAudit.
                        query("from AuditEventPair as p where p.event = :event", ae);

                    assert pairs.size() == 1;

                    AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);

                    assert ae.equals(p.getEvent());
                    AuditTypeField f = p.getField();
                    assert "ds".equals(f.getName());
                    AuditCollectionType acolt = (AuditCollectionType)f.getType();
                    assert List.class.equals(acolt.getCollectionClassInstance());
                    assert DUni.class.equals(acolt.getClassInstance());

                    List<Long> value = p.getIds();
                    Object v = p.getValue();
                    assert v == value;

                    assert value.size() == 1;
                    assert dtwo.getId().equals(value.get(0));
                }
                else
                {
                    throw new Error("invalid state");
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
    public void testPostUpdate_CollectionOfTwo_RemoveOne() throws Exception
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
            List<DUni> ds = new ArrayList<DUni>();
            DUni done = new DUni();
            DUni dtwo = new DUni();
            ds.add(done);
            ds.add(dtwo);
            c.setDs(ds);

            s.save(c);

            s.getTransaction().commit();

            s.beginTransaction();

            c.getDs().remove(0);
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;

            // check entity types

            List types = HibernateAudit.query("from AuditEntityType");
            assert types.size() == 2;

            for(Object o: types)
            {
                AuditEntityType aet = (AuditEntityType)o;

                if (CUni.class.equals(aet.getClassInstance()) ||
                    DUni.class.equals(aet.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type: "  + aet);
                }

            }

            // check collection types

            types = HibernateAudit.query("from AuditCollectionType");
            assert types.size() == 1;

            AuditCollectionType act = (AuditCollectionType)types.get(0);
            assert List.class.equals(act.getCollectionClassInstance());
            assert DUni.class.equals(act.getClassInstance());

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            // one collection UPDATE
            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);
            assert tx.equals(ae.getTransaction());
            assert c.getId().equals(ae.getTargetId());
            assert ChangeType.UPDATE.equals(ae.getType());

            List pairs = HibernateAudit.
                query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.size() == 1;

            AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);

            assert ae.equals(p.getEvent());
            AuditTypeField f = p.getField();
            assert "ds".equals(f.getName());
            AuditCollectionType ct = (AuditCollectionType)f.getType();
            assert List.class.equals(ct.getCollectionClassInstance());
            assert DUni.class.equals(ct.getClassInstance());

            List<Long> value = p.getIds();
            Object o = p.getValue();
            assert o == value;

            // updates with the remaining collection
            assert value.size() == 1;
            assert dtwo.getId().equals(value.get(0));
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
