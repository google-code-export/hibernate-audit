package com.googlecode.hibernate.audit.test.post_update;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_update.data.A;
import com.googlecode.hibernate.audit.test.post_update.data.CUni;
import com.googlecode.hibernate.audit.test.post_update.data.DUni;
import com.googlecode.hibernate.audit.test.post_update.data.FMan;
import com.googlecode.hibernate.audit.test.post_update.data.E;
import com.googlecode.hibernate.audit.test.post_update.data.GMan;
import com.googlecode.hibernate.audit.test.post_update.data.H;
import com.googlecode.hibernate.audit.test.post_update.data.CBi;
import com.googlecode.hibernate.audit.test.post_update.data.DBi;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.CollectionDelta;
import com.googlecode.hibernate.audit.delta.EntityReferenceDelta;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.io.Serializable;
import java.io.ByteArrayInputStream;

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
    public void testPostUpdate_EmptyCollection_Recreate_AddOne() throws Exception
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

    @Test(enabled = true)
    public void testPostUpdate_EmptyCollection_Update_AddOne() throws Exception
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
            c.setDs(new ArrayList<DUni>()); // empty collection
            s.save(c);
            s.getTransaction().commit();

            s.beginTransaction();

            DUni d = new DUni();
            c.getDs().add(d); // using the internal collection, to avoid RECREATE
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 2;
            AuditTransaction tx = null;

            tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = null;

            ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 0;

            tx = txs.get(1);

            td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

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

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 3;

            EntityDelta ed = null;

            ed = td.getEntityDelta(done.getId(), DUni.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();

            ed = td.getEntityDelta(dtwo.getId(), DUni.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert new Integer(7).equals(ed.getPrimitiveDelta("i").getValue());

            ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("ds");

            assert DUni.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 2;
            
            assert ids.contains(done.getId());
            assert ids.contains(dtwo.getId());
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
            dtwo.setI(8);
            c.getDs().add(dtwo);
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = null;

            ed = td.getEntityDelta(dtwo.getId(), DUni.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert new Integer(8).equals(ed.getPrimitiveDelta("i").getValue());

            ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("ds");

            assert DUni.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 2;

            assert ids.contains(done.getId());
            assert ids.contains(dtwo.getId());
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
            dtwo.setI(9);
            DUni dthree = new DUni();
            dthree.setS("sonoma");

            c.getDs().add(dtwo);
            c.getDs().add(dthree);
            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 3;

            EntityDelta ed = null;

            ed = td.getEntityDelta(dtwo.getId(), DUni.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert new Integer(9).equals(ed.getPrimitiveDelta("i").getValue());

            ed = td.getEntityDelta(dthree.getId(), DUni.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert "sonoma".equals(ed.getPrimitiveDelta("s").getValue());

            ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("ds");

            assert DUni.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 3;

            assert ids.contains(done.getId());
            assert ids.contains(dtwo.getId());
            assert ids.contains(dthree.getId());
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

            done.setI(10);
            done.setS("sith");

            s.update(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = null;

            ed = td.getEntityDelta(done.getId(), DUni.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;
            assert new Integer(10).equals(ed.getPrimitiveDelta("i").getValue());
            assert "sith".equals(ed.getPrimitiveDelta("s").getValue());
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

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = null;

            ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ed.isUpdate();
            assert ed.getCollectionDeltas().size() == 1;
            assert ed.getScalarDeltas().isEmpty();
            CollectionDelta cd = ed.getCollectionDelta("ds");
            assert DUni.class.getName().equals(cd.getMemberEntityName());
            assert cd.getIds().size() == 0;
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

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = null;

            ed = td.getEntityDelta(dtwo.getId(), DUni.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();

            ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ed.isUpdate();
            assert ed.getCollectionDeltas().size() == 1;
            assert ed.getScalarDeltas().isEmpty();
            CollectionDelta cd = ed.getCollectionDelta("ds");
            assert DUni.class.getName().equals(cd.getMemberEntityName());
            assert cd.getIds().size() == 1;
            assert cd.getIds().contains(dtwo.getId());
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

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ed.isUpdate();
            assert ed.getCollectionDeltas().size() == 1;
            assert ed.getScalarDeltas().isEmpty();
            CollectionDelta cd = ed.getCollectionDelta("ds");
            assert DUni.class.getName().equals(cd.getMemberEntityName());
            assert cd.getIds().size() == 1;
            assert cd.getIds().contains(dtwo.getId());
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

//    TODO https://jira.novaordis.org/browse/HBA-145
//    TODO this fails because no UPDATE event gets generated for entities (unlike collections)
//    @Test(enabled = true)
//    public void testPostUpdate_OneToOne_NonManagerUpdated() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(E.class);
//        config.addAnnotatedClass(FMan.class);
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
//            E e = new E();
//            FMan f = new FMan();
//
//            e.setF(f);
//            f.setE(e);
//
//            s.save(e);
//
//            s.getTransaction().commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions();
//            assert txs.size() == 1;
//
//            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());
//
//            assert td.getEntityDeltas().size() == 2;
//
//            EntityDelta ed = td.getEntityDelta(e.getId(), E.class.getName());
//            assert ed.isInsert();
//            assert ed.getCollectionDeltas().size() == 0;
//            assert ed.getScalarDeltas().size() == 1;
//            EntityReferenceDelta erd = ed.getEntityReferenceDelta("f");
//            assert FMan.class.getName().equals(erd.getEntityName());
//            assert f.getId().equals(erd.getId());
//
//            ed = td.getEntityDelta(f.getId(), FMan.class.getName());
//            assert ed.isInsert();
//            assert ed.getCollectionDeltas().size() == 0;
//            assert ed.getScalarDeltas().size() == 1;
//            erd = ed.getEntityReferenceDelta("e");
//            assert E.class.getName().equals(erd.getEntityName());
//            assert e.getId().equals(erd.getId());
//
//            s.beginTransaction();
//
//            FMan f2 = new FMan();
//            e.setF(f2);
//
//            s.update(e);
//
//            s.getTransaction().commit();
//
//            txs = HibernateAudit.getTransactions();
//            assert txs.size() == 2;
//
//            td = HibernateAudit.getDelta(txs.get(1).getId());
//
//            assert td.getEntityDeltas().size() == 2; // I expect an INSERT and an UPDATE
//
//            ed = td.getEntityDelta(e.getId(), E.class.getName());
//            assert ed.isUpdate();
//            assert ed.getCollectionDeltas().size() == 0;
//            assert ed.getScalarDeltas().size() == 1;
//            erd = ed.getEntityReferenceDelta("f");
//            assert FMan.class.getName().equals(erd.getEntityName());
//            assert f2.getId().equals(erd.getId());
//
//            ed = td.getEntityDelta(f2.getId(), FMan.class.getName());
//            assert ed.isInsert();
//            assert ed.getCollectionDeltas().isEmpty();
//            assert ed.getScalarDeltas().isEmpty();
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

    @Test(enabled = true)
    public void testPostUpdate_OneToOne_ManagerUpdated() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(GMan.class);
        config.addAnnotatedClass(H.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            GMan g = new GMan();
            H h = new H();

            g.setH(h);
            h.setG(g);

            s.save(g);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(g.getId(), GMan.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().size() == 0;
            assert ed.getScalarDeltas().size() == 1;
            EntityReferenceDelta erd = ed.getEntityReferenceDelta("h");
            assert H.class.getName().equals(erd.getEntityName());
            assert h.getId().equals(erd.getId());

            ed = td.getEntityDelta(h.getId(), H.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().size() == 0;
            assert ed.getScalarDeltas().size() == 1;
            erd = ed.getEntityReferenceDelta("g");
            assert GMan.class.getName().equals(erd.getEntityName());
            assert g.getId().equals(erd.getId());

            s.beginTransaction();

            H h2 = new H();
            g.setH(h2);

            s.update(g);

            s.getTransaction().commit();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 2; // I expect an INSERT and an UPDATE

            ed = td.getEntityDelta(g.getId(), GMan.class.getName());
            assert ed.isUpdate();
            assert ed.getCollectionDeltas().size() == 0;
            assert ed.getScalarDeltas().size() == 1;
            erd = ed.getEntityReferenceDelta("h");
            assert H.class.getName().equals(erd.getEntityName());
            assert h2.getId().equals(erd.getId());

            ed = td.getEntityDelta(h2.getId(), H.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();
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
    public void testPostUpdate_ManyToOne_Bidirectional() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(CBi.class);
        config.addAnnotatedClass(DBi.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            CBi c = new CBi();
            DBi d = new DBi();

            c.getDs().add(d);
            d.setC(c);

            s.save(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(c.getId(), CBi.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().size() == 1;
            assert ed.getScalarDeltas().isEmpty();

            CollectionDelta cd = ed.getCollectionDelta("ds");
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 1;
            assert ids.contains(d.getId());

            ed = td.getEntityDelta(d.getId(), DBi.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().size() == 0;
            assert ed.getScalarDeltas().size() == 1;
            EntityReferenceDelta erd = ed.getEntityReferenceDelta("c");
            assert CBi.class.getName().equals(erd.getEntityName());
            assert c.getId().equals(erd.getId());

            s.beginTransaction();

            // UPDATE the 'one' reference

            CBi c2 = new CBi();
            d.setC(c2);

            s.update(d);

            s.getTransaction().commit();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 2; // I expect an INSERT and an UPDATE

            ed = td.getEntityDelta(d.getId(), DBi.class.getName());
            assert ed.isUpdate();
            assert ed.getCollectionDeltas().size() == 0;
            assert ed.getScalarDeltas().size() == 1;
            erd = ed.getEntityReferenceDelta("c");
            assert CBi.class.getName().equals(erd.getEntityName());
            assert c2.getId().equals(erd.getId());

            ed = td.getEntityDelta(c2.getId(), CBi.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();
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
    public void testPostUpdate_ManyToOne_Bidirectional_Tuplizer() throws Exception
    {
        Configuration config = new Configuration();
        config.configure(getHibernateConfigurationFileName());

        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
        // enough)

        String cMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class entity-name='C' name='com.googlecode.hibernate.audit.test.post_update.data.CBi' table='C'>\n" +
            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_update.data.CBiTuplizer'/>\n" +
            "        <id name='id' type='long'>\n" +
            "            <generator class='native'/>\n" +
            "        </id>\n" +
            "      <property name='s' type='string'/>\n" +
            "      <property name='i' type='integer'/>\n" +
            "      <set name='ds' cascade='all'>\n" +
            "            <key column='c_id'/>\n" +
            "            <one-to-many entity-name='com.googlecode.hibernate.audit.test.post_update.data.DBi'/>\n" +
            "      </set>\n" +
            "    </class>\n" +
            "</hibernate-mapping>";

        String dMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class name='com.googlecode.hibernate.audit.test.post_update.data.DBi' table='D'>\n" +
            "      <id name='id' type='long'>\n" +
            "         <generator class='native'/>\n" +
            "      </id>\n" +
            "      <property name='s' type='string'/>\n" +
            "      <property name='i' type='integer'/>\n" +
            "      <many-to-one name='c' entity-name='C' column='c_id' cascade='all'/>\n" +
            "   </class>\n" +
            "</hibernate-mapping>";

        config.addInputStream(new ByteArrayInputStream(cMapping.getBytes()));
        config.addInputStream(new ByteArrayInputStream(dMapping.getBytes()));

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            CBi c = new CBi();
            DBi d = new DBi();
            c.getDs().add(d);
            d.setC(c);

            s.save("C", c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(c.getId(), CBi.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().size() == 1;
            assert ed.getScalarDeltas().isEmpty();

            CollectionDelta cd = ed.getCollectionDelta("ds");
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 1;
            assert ids.contains(d.getId());

            ed = td.getEntityDelta(d.getId(), DBi.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().size() == 0;
            assert ed.getScalarDeltas().size() == 1;
            EntityReferenceDelta erd = ed.getEntityReferenceDelta("c");
            assert CBi.class.getName().equals(erd.getEntityName());
            assert c.getId().equals(erd.getId());

            s.beginTransaction();

            // UPDATE the 'one' reference

            CBi c2 = new CBi();
            d.setC(c2);

            s.update(d);

            s.getTransaction().commit();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 2; // I expect an INSERT and an UPDATE

            ed = td.getEntityDelta(d.getId(), DBi.class.getName());
            assert ed.isUpdate();
            assert ed.getCollectionDeltas().size() == 0;
            assert ed.getScalarDeltas().size() == 1;
            erd = ed.getEntityReferenceDelta("c");
            assert CBi.class.getName().equals(erd.getEntityName());
            assert c2.getId().equals(erd.getId());

            ed = td.getEntityDelta(c2.getId(), CBi.class.getName());
            assert ed.isInsert();
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();
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
