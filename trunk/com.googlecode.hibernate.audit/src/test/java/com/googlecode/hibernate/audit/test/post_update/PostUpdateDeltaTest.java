package com.googlecode.hibernate.audit.test.post_update;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_update.data.A;
import com.googlecode.hibernate.audit.test.post_update.data.CUni;
import com.googlecode.hibernate.audit.test.post_update.data.DUni;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.CollectionDelta;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.Serializable;

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
public class PostUpdateDeltaTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostUpdateDeltaTest.class);

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

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;
            EntityDelta ed = td.getEntityDeltas().iterator().next();
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert a.getId().equals(ed.getId());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().isEmpty();
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

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDeltas().iterator().next();
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;

            PrimitiveDelta pd = ed.getPrimitiveDelta("s");
            assert "shinshila".equals(pd.getValue());
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

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDeltas().iterator().next();
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;

            PrimitiveDelta pd = ed.getPrimitiveDelta("s");
            assert "shalimar".equals(pd.getValue());
            pd = ed.getPrimitiveDelta("i");
            assert new Integer(789).equals(pd.getValue());
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

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDeltas().iterator().next();
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;

            PrimitiveDelta pd = null;
            pd = ed.getPrimitiveDelta("s");
            assert null == pd.getValue();
            pd = ed.getPrimitiveDelta("i");
            assert null == pd.getValue();
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

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDeltas().iterator().next();
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;

            PrimitiveDelta pd = ed.getPrimitiveDelta("s");
            assert "sabar".equals(pd.getValue());
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

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            log.debug(td.getEntityDeltas().size());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = null;

            ed = td.getEntityDelta(d.getId(), DUni.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();

            ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("ds");

            assert DUni.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 1;
            assert d.getId().equals(ids.iterator().next());
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

//    @Test(enabled = true)
//    public void testPostUpdate_EmptyCollection_AddTwo() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(CUni.class);
//        config.addAnnotatedClass(DUni.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            CUni c = new CUni();
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            s.beginTransaction();
//
//            List<DUni> ds = new ArrayList<DUni>();
//            DUni done = new DUni();
//            DUni dtwo = new DUni();
//            dtwo.setI(7);
//
//            ds.add(done);
//            ds.add(dtwo);
//
//            c.setDs(ds);
//
//            s.update(c);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
//            assert txs.size() == 2;
//
//            List types = HibernateAudit.query("from AuditEntityType");
//            assert types.size() == 2;
//
//            for(Object o: types)
//            {
//                AuditEntityType aet = (AuditEntityType)o;
//
//                if (CUni.class.equals(aet.getClassInstance()) ||
//                    DUni.class.equals(aet.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type: "  + aet);
//                }
//
//            }
//
//            AuditTransaction tx = txs.get(1);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            // two INSERTs, no UPDATE
//            assert events.size() == 2;
//
//            AuditEvent ae = (AuditEvent)events.get(0);
//            assert ChangeType.INSERT.equals(ae.getType());
//            assert tx.equals(ae.getTransaction());
//            assert done.getId().equals(ae.getTargetId());
//
//            List pairs = HibernateAudit.
//                query("from AuditEventPair as p where p.event = :event", ae);
//
//            assert pairs.size() == 0; // no fields in for the first D instance
//
//            ae = (AuditEvent)events.get(1);
//            assert ChangeType.INSERT.equals(ae.getType());
//            assert tx.equals(ae.getTransaction());
//            assert dtwo.getId().equals(ae.getTargetId());
//
//            pairs = HibernateAudit.query("from AuditEventPair as p where p.event = :event", ae);
//
//            assert pairs.size() == 1;
//
//            AuditEventPair p = (AuditEventPair)pairs.get(0);
//            assert ae.equals(p.getEvent());
//            AuditTypeField f = p.getField();
//            assert "i".equals(f.getName());
//            assert Integer.class.equals(f.getType().getClassInstance());
//            assert new Integer(7).equals(p.getValue());
//            assert "7".equals(p.getStringValue());
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testPostUpdate_CollectionOfOne_AddAnotherOne() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(CUni.class);
//        config.addAnnotatedClass(DUni.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            CUni c = new CUni();
//            List<DUni> ds = new ArrayList<DUni>();
//            DUni done = new DUni();
//            ds.add(done);
//            c.setDs(ds);
//
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            s.beginTransaction();
//
//            DUni dtwo = new DUni();
//            dtwo.setI(7);
//            c.getDs().add(dtwo);
//            s.update(c);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
//            assert txs.size() == 2;
//
//            List types = HibernateAudit.query("from AuditEntityType");
//            assert types.size() == 2;
//
//            for(Object o: types)
//            {
//                AuditEntityType aet = (AuditEntityType)o;
//
//                if (CUni.class.equals(aet.getClassInstance()) ||
//                    DUni.class.equals(aet.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type: "  + aet);
//                }
//            }
//
//            AuditTransaction tx = txs.get(1);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            assert events.size() == 2;
//
//            for(Object o: events)
//            {
//                AuditEvent ae = (AuditEvent)o;
//                ChangeType ct = ae.getType();
//                assert tx.equals(ae.getTransaction());
//                List pairs = HibernateAudit.
//                    query("from AuditEventPair as p where p.event = :event", ae);
//
//                if (ChangeType.INSERT.equals(ct))
//                {
//                    assert dtwo.getId().equals(ae.getTargetId());
//                    assert pairs.size() == 1;
//                    AuditEventPair p = (AuditEventPair)pairs.get(0);
//                    assert ae.equals(p.getEvent());
//                    AuditTypeField f = p.getField();
//                    assert "i".equals(f.getName());
//                    assert Integer.class.equals(f.getType().getClassInstance());
//                    assert new Integer(7).equals(p.getValue());
//                    assert "7".equals(p.getStringValue());
//                }
//                else if (ChangeType.UPDATE.equals(ct))
//                {
//                    assert c.getId().equals(ae.getTargetId());
//                    assert pairs.size() == 1;
//                    AuditEventPair p = (AuditEventPair)pairs.get(0);
//                    assert ae.equals(p.getEvent());
//                    AuditTypeField f = p.getField();
//                    assert "ds".equals(f.getName());
//                    List newids = (List)p.getValue();
//                    assert newids.size() == 2;
//                    assert newids.contains(done.getId());
//                    assert newids.contains(dtwo.getId());
//                }
//                else
//                {
//                    throw new Error("invalid state");
//                }
//            }
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testPostUpdate_CollectionOfOne_AddTwo() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(CUni.class);
//        config.addAnnotatedClass(DUni.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            CUni c = new CUni();
//            List<DUni> ds = new ArrayList<DUni>();
//            DUni done = new DUni();
//            ds.add(done);
//            c.setDs(ds);
//
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            s.beginTransaction();
//
//            DUni dtwo = new DUni();
//            dtwo.setI(8);
//            DUni dthree = new DUni();
//            dthree.setS("sonoma");
//
//            c.getDs().add(dtwo);
//            c.getDs().add(dthree);
//            s.update(c);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
//            assert txs.size() == 2;
//
//            List types = HibernateAudit.query("from AuditEntityType");
//            assert types.size() == 2;
//
//            for(Object o: types)
//            {
//                AuditEntityType aet = (AuditEntityType)o;
//
//                if (CUni.class.equals(aet.getClassInstance()) ||
//                    DUni.class.equals(aet.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type: "  + aet);
//                }
//
//            }
//
//            AuditTransaction tx = txs.get(1);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            assert events.size() == 3;
//
//            for(Object o: events)
//            {
//                AuditEvent ae = (AuditEvent)o;
//                assert tx.equals(ae.getTransaction());
//                ChangeType ct = ae.getType();
//                Object id = ae.getTargetId();
//                List pairs = HibernateAudit.
//                    query("from AuditEventPair as p where p.event = :event", ae);
//
//                if (c.getId().equals(id))
//                {
//                    assert ChangeType.UPDATE.equals(ct);
//                    assert pairs.size() == 1;
//                    AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);
//                    assert ae.equals(p.getEvent());
//                    AuditTypeField f = p.getField();
//                    assert "ds".equals(f.getName());
//                    assert DUni.class.equals(f.getType().getClassInstance());
//                    assert List.class.equals(((AuditCollectionType)f.getType()).
//                        getCollectionClassInstance());
//
//                    List ids = (List)p.getValue();
//                    assert ids.size() == 3;
//                    assert ids.contains(done.getId());
//                    assert ids.contains(dtwo.getId());
//                    assert ids.contains(dthree.getId());
//                }
//                else if (dtwo.getId().equals(id))
//                {
//                    assert ChangeType.INSERT.equals(ct);
//                    assert pairs.size() == 1;
//                    AuditEventPair p = (AuditEventPair)pairs.get(0);
//                    assert ae.equals(p.getEvent());
//                    AuditTypeField f = p.getField();
//                    assert "i".equals(f.getName());
//                    assert Integer.class.equals(f.getType().getClassInstance());
//                    assert new Integer(8).equals(p.getValue());
//                    assert "8".equals(p.getStringValue());
//                }
//                else if (dthree.getId().equals(id))
//                {
//                    assert ChangeType.INSERT.equals(ct);
//                    assert pairs.size() == 1;
//
//                    AuditEventPair p = (AuditEventPair)pairs.get(0);
//                    assert ae.equals(p.getEvent());
//                    AuditTypeField f = p.getField();
//                    assert "s".equals(f.getName());
//                    assert String.class.equals(f.getType().getClassInstance());
//                    assert "sonoma".equals(p.getValue());
//                    assert "sonoma".equals(p.getStringValue());
//                }
//                else
//                {
//                    throw new Error("invalid state, id = " + id);
//                }
//            }
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testPostUpdate_CollectionOfOne_UpdateExistingsContent() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(CUni.class);
//        config.addAnnotatedClass(DUni.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            CUni c = new CUni();
//            List<DUni> ds = new ArrayList<DUni>();
//            DUni done = new DUni();
//            ds.add(done);
//            c.setDs(ds);
//
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            s.beginTransaction();
//
//            done.setI(9);
//            done.setS("sith");
//
//            s.update(c);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
//            assert txs.size() == 2;
//
//            List types = HibernateAudit.query("from AuditEntityType");
//            assert types.size() == 2;
//
//            for(Object o: types)
//            {
//                AuditEntityType aet = (AuditEntityType)o;
//
//                if (CUni.class.equals(aet.getClassInstance()) ||
//                    DUni.class.equals(aet.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type: "  + aet);
//                }
//
//            }
//
//            AuditTransaction tx = txs.get(1);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            // one UPDATE
//            assert events.size() == 1;
//
//            AuditEvent ae = (AuditEvent)events.get(0);
//            assert ChangeType.UPDATE.equals(ae.getType());
//            assert tx.equals(ae.getTransaction());
//            assert done.getId().equals(ae.getTargetId());
//
//            List pairs = HibernateAudit.
//                query("from AuditEventPair as p where p.event = :event", ae);
//
//            assert pairs.size() == 2;
//
//            AuditEventPair p = (AuditEventPair)pairs.get(0);
//
//            assert ae.equals(p.getEvent());
//            AuditTypeField f = p.getField();
//            assert "i".equals(f.getName());
//            assert Integer.class.equals(f.getType().getClassInstance());
//            assert new Integer(9).equals(p.getValue());
//            assert "9".equals(p.getStringValue());
//
//            p = (AuditEventPair)pairs.get(1);
//            assert ae.equals(p.getEvent());
//            f = p.getField();
//            assert "s".equals(f.getName());
//            assert String.class.equals(f.getType().getClassInstance());
//            assert "sith".equals(p.getValue());
//            assert "sith".equals(p.getStringValue());
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testPostUpdate_CollectionOfOne_RemoveExisting() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(CUni.class);
//        config.addAnnotatedClass(DUni.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            CUni c = new CUni();
//            List<DUni> ds = new ArrayList<DUni>();
//            DUni done = new DUni();
//            ds.add(done);
//            c.setDs(ds);
//
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            s.beginTransaction();
//
//            c.getDs().clear();
//            s.update(c);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
//            assert txs.size() == 2;
//
//            // check entity types
//
//            List types = HibernateAudit.query("from AuditEntityType");
//            assert types.size() == 2;
//
//            for(Object o: types)
//            {
//                AuditEntityType aet = (AuditEntityType)o;
//
//                if (CUni.class.equals(aet.getClassInstance()) ||
//                    DUni.class.equals(aet.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type: "  + aet);
//                }
//
//            }
//
//            // check collection types
//
//            types = HibernateAudit.query("from AuditCollectionType");
//            assert types.size() == 1;
//
//            AuditCollectionType act = (AuditCollectionType)types.get(0);
//            assert List.class.equals(act.getCollectionClassInstance());
//            assert DUni.class.equals(act.getClassInstance());
//
//            AuditTransaction tx = txs.get(1);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            // one collection UPDATE
//            assert events.size() == 1;
//
//            AuditEvent ae = (AuditEvent)events.get(0);
//            assert tx.equals(ae.getTransaction());
//            assert c.getId().equals(ae.getTargetId());
//            assert ChangeType.UPDATE.equals(ae.getType());
//
//            List pairs = HibernateAudit.
//                query("from AuditEventPair as p where p.event = :event", ae);
//
//            assert pairs.size() == 1;
//
//            AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);
//
//            assert ae.equals(p.getEvent());
//            AuditTypeField f = p.getField();
//            assert "ds".equals(f.getName());
//            AuditCollectionType ct = (AuditCollectionType)f.getType();
//            assert List.class.equals(ct.getCollectionClassInstance());
//            assert DUni.class.equals(ct.getClassInstance());
//
//            List<Long> value = p.getIds();
//            Object o = p.getValue();
//            assert o == value;
//
//            assert value.isEmpty();
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testPostUpdate_CollectionOfOne_ReplaceExistingWithAnotherOne() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(CUni.class);
//        config.addAnnotatedClass(DUni.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            CUni c = new CUni();
//            List<DUni> ds = new ArrayList<DUni>();
//            DUni done = new DUni();
//            ds.add(done);
//            c.setDs(ds);
//
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            s.beginTransaction();
//
//            DUni dtwo = new DUni();
//            c.getDs().clear();
//            c.getDs().add(dtwo);
//            s.update(c);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
//            assert txs.size() == 2;
//
//            // check entity types
//
//            List types = HibernateAudit.query("from AuditEntityType");
//            assert types.size() == 2;
//
//            for(Object o: types)
//            {
//                AuditEntityType aet = (AuditEntityType)o;
//
//                if (CUni.class.equals(aet.getClassInstance()) ||
//                    DUni.class.equals(aet.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type: "  + aet);
//                }
//
//            }
//
//            // check collection types
//
//            types = HibernateAudit.query("from AuditCollectionType");
//            assert types.size() == 1;
//
//            AuditCollectionType act = (AuditCollectionType)types.get(0);
//            assert List.class.equals(act.getCollectionClassInstance());
//            assert DUni.class.equals(act.getClassInstance());
//
//            AuditTransaction tx = txs.get(1);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            // one insert and one collection update
//            assert events.size() == 2;
//
//            for(Object o: events)
//            {
//                AuditEvent ae = (AuditEvent)o;
//                ChangeType ct = ae.getType();
//                assert tx.equals(ae.getTransaction());
//
//                if (ChangeType.INSERT.equals(ct))
//                {
//                    // entity insert
//                    assert dtwo.getId().equals(ae.getTargetId());
//
//                    List pairs = HibernateAudit.
//                        query("from AuditEventPair as p where p.event = :event", ae);
//
//                    assert pairs.size() == 0;
//                }
//                else if (ChangeType.UPDATE.equals(ct))
//                {
//                    // collection update
//                    assert c.getId().equals(ae.getTargetId());
//
//                    List pairs = HibernateAudit.
//                        query("from AuditEventPair as p where p.event = :event", ae);
//
//                    assert pairs.size() == 1;
//
//                    AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);
//
//                    assert ae.equals(p.getEvent());
//                    AuditTypeField f = p.getField();
//                    assert "ds".equals(f.getName());
//                    AuditCollectionType acolt = (AuditCollectionType)f.getType();
//                    assert List.class.equals(acolt.getCollectionClassInstance());
//                    assert DUni.class.equals(acolt.getClassInstance());
//
//                    List<Long> value = p.getIds();
//                    Object v = p.getValue();
//                    assert v == value;
//
//                    assert value.size() == 1;
//                    assert dtwo.getId().equals(value.get(0));
//                }
//                else
//                {
//                    throw new Error("invalid state");
//                }
//            }
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testPostUpdate_CollectionOfTwo_RemoveOne() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(CUni.class);
//        config.addAnnotatedClass(DUni.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            CUni c = new CUni();
//            List<DUni> ds = new ArrayList<DUni>();
//            DUni done = new DUni();
//            DUni dtwo = new DUni();
//            ds.add(done);
//            ds.add(dtwo);
//            c.setDs(ds);
//
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            s.beginTransaction();
//
//            c.getDs().remove(0);
//            s.update(c);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
//            assert txs.size() == 2;
//
//            // check entity types
//
//            List types = HibernateAudit.query("from AuditEntityType");
//            assert types.size() == 2;
//
//            for(Object o: types)
//            {
//                AuditEntityType aet = (AuditEntityType)o;
//
//                if (CUni.class.equals(aet.getClassInstance()) ||
//                    DUni.class.equals(aet.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type: "  + aet);
//                }
//
//            }
//
//            // check collection types
//
//            types = HibernateAudit.query("from AuditCollectionType");
//            assert types.size() == 1;
//
//            AuditCollectionType act = (AuditCollectionType)types.get(0);
//            assert List.class.equals(act.getCollectionClassInstance());
//            assert DUni.class.equals(act.getClassInstance());
//
//            AuditTransaction tx = txs.get(1);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            // one collection UPDATE
//            assert events.size() == 1;
//
//            AuditEvent ae = (AuditEvent)events.get(0);
//            assert tx.equals(ae.getTransaction());
//            assert c.getId().equals(ae.getTargetId());
//            assert ChangeType.UPDATE.equals(ae.getType());
//
//            List pairs = HibernateAudit.
//                query("from AuditEventPair as p where p.event = :event", ae);
//
//            assert pairs.size() == 1;
//
//            AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);
//
//            assert ae.equals(p.getEvent());
//            AuditTypeField f = p.getField();
//            assert "ds".equals(f.getName());
//            AuditCollectionType ct = (AuditCollectionType)f.getType();
//            assert List.class.equals(ct.getCollectionClassInstance());
//            assert DUni.class.equals(ct.getClassInstance());
//
//            List<Long> value = p.getIds();
//            Object o = p.getValue();
//            assert o == value;
//
//            // updates with the remaining collection
//            assert value.size() == 1;
//            assert dtwo.getId().equals(value.get(0));
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
