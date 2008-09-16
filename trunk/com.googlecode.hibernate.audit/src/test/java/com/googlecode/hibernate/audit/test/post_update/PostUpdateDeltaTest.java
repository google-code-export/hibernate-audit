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

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
