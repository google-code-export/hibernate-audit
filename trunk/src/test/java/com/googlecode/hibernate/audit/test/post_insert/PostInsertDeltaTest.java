package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.A;
import com.googlecode.hibernate.audit.test.post_insert.data.B;
import com.googlecode.hibernate.audit.test.post_insert.data.C;
import com.googlecode.hibernate.audit.test.post_insert.data.D;
import com.googlecode.hibernate.audit.test.post_insert.data.WB;
import com.googlecode.hibernate.audit.test.post_insert.data.WA;
import com.googlecode.hibernate.audit.test.post_insert.data.XA;
import com.googlecode.hibernate.audit.test.post_insert.data.XB;
import com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer;
import com.googlecode.hibernate.audit.test.post_insert.data.XA2;
import com.googlecode.hibernate.audit.test.post_insert.data.H;
import com.googlecode.hibernate.audit.test.util.Formats;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.ScalarDelta;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.EntityReferenceDelta;
import com.googlecode.hibernate.audit.delta.CollectionDelta;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Collection;
import java.io.Serializable;
import java.io.ByteArrayInputStream;

/**
 * This is a collection of various "post-insert" use cases of leaving an audit trail and then
 * extracting the deltas from that audit trail via the API.
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
public class PostInsertDeltaTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertDeltaTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Tests initially developed in PostInsertTest -------------------------------------------------

    @Test(enabled = false)
    public void testInsert_NullProperty() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Session s = sf.openSession();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);
            s.beginTransaction();

            A a = new A();
            a.setName("alice");
            // 'age' is null

            s.save(a);
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(a.getId());

            assert txs.size() == 1;

            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(a.getId(), A.class.getName());

            assert a.getId().equals(ed.getId());
            assert ed.getCollectionDeltas().isEmpty();
            assert ChangeType.INSERT.equals(ed.getChangeType());

            Set<ScalarDelta> pds = ed.getScalarDeltas();
            assert pds.size() == 1;

            ScalarDelta sd = ed.getScalarDelta("name");
            PrimitiveDelta pd = (PrimitiveDelta)sd;
            assert String.class.equals(pd.getType());
            assert "alice".equals(pd.getValue());
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
    public void testSuccesiveInserts() throws Exception
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

            A a2 = new A();
            a2.setName("alex");

            s = sf.openSession();
            s.beginTransaction();
            s.save(a2);
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);

            assert txs.size() == 2;

            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(a.getId(), A.class.getName());

            assert a.getId().equals(ed.getId());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            Set<ScalarDelta> pds = ed.getScalarDeltas();
            assert pds.size() == 1;
            ScalarDelta sd = ed.getScalarDelta("name");
            PrimitiveDelta pd = (PrimitiveDelta)sd;
            assert String.class.equals(pd.getType());
            assert "alice".equals(pd.getValue());

            tx = txs.get(1);
            td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            ed = td.getEntityDelta(a2.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert a2.getId().equals(ed.getId());
            assert ed.getCollectionDeltas().isEmpty();
            pds = ed.getScalarDeltas();
            assert pds.size() == 1;
            pd = (PrimitiveDelta)ed.getScalarDelta("name");
            assert String.class.equals(pd.getType());
            assert "alex".equals(pd.getValue());
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
    public void testAuditType_TwoInsertsSameEntity_OneTransaction() throws Exception
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
            a.setName("alice");
            s.save(a);

            A a2 = new A();
            a2.setName("alex");
            s.save(a2);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(a.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            ScalarDelta sd = ed.getScalarDelta("name");
            PrimitiveDelta pd = (PrimitiveDelta)sd;
            assert String.class.equals(pd.getType());
            assert "alice".equals(pd.getValue());

            ed = td.getEntityDelta(a2.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            pd = (PrimitiveDelta)ed.getScalarDelta("name");
            assert String.class.equals(pd.getType());
            assert "alex".equals(pd.getValue());
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
    public void testAuditType_TwoInsertsSameEntity_TwoTransactions() throws Exception
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
            a.setName("alice");
            s.save(a);

            s.getTransaction().commit();
            s.close();

            s = sf.openSession();
            s.beginTransaction();

            A a2 = new A();
            a2.setName("alex");
            s.save(a2);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            AuditTransaction tx = txs.get(0);
            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(a.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            ScalarDelta sd = ed.getScalarDelta("name");
            PrimitiveDelta pd = (PrimitiveDelta)sd;
            assert String.class.equals(pd.getType());
            assert "alice".equals(pd.getValue());

            tx = txs.get(1);
            td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            ed = td.getEntityDelta(a2.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            pd = (PrimitiveDelta)ed.getScalarDelta("name");
            assert String.class.equals(pd.getType());
            assert "alex".equals(pd.getValue());
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
    public void testAuditType_TwoEntities() throws Exception
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
            a.setName("alice");
            s.save(a);

            B b = new B();
            b.setName("bob");
            s.save(b);

            B b2 = new B();
            b2.setName("ben");
            s.save(b2);

            A a2 = new A();
            a2.setName("alex");
            s.save(a2);

            s.getTransaction().commit();
            s.close();


            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction at = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(at.getId());
            assert td.getEntityDeltas().size() == 4;

            EntityDelta d = null;

            d = td.getEntityDelta(a.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert d.getScalarDeltas().size() == 1;
            assert "alice".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert A.class.getName().equals(d.getEntityName());

            d = td.getEntityDelta(b.getId(), B.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert d.getScalarDeltas().size() == 1;
            assert "bob".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert B.class.getName().equals(d.getEntityName());

            d = td.getEntityDelta(b2.getId(), B.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert d.getScalarDeltas().size() == 1;
            assert "ben".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert B.class.getName().equals(d.getEntityName());

            d = td.getEntityDelta(a2.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert d.getScalarDeltas().size() == 1;
            assert "alex".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert A.class.getName().equals(d.getEntityName());
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
    public void testAuditField_TwoEntities_TwoTransactions() throws Exception
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
            a.setName("alice");
            a.setAge(30);

            s.save(a);

            B b = new B();
            b.setName("bob");
            b.setBirthDate((Date)Formats.testDateFormat.parseObject("01/01/1971"));

            s.save(b);

            s.getTransaction().commit();
            s.close();

            s = sf.openSession();
            s.beginTransaction();

            A a2 = new A();
            a2.setName("anna");

            s.save(a2);

            B b2 = new B();
            b2.setName("ben");
            b2.setBirthDate((Date)Formats.testDateFormat.parseObject("02/02/1972"));

            s.save(b2);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;
            TransactionDelta td = null;

            td = HibernateAudit.getDelta(txs.get(0).getId());

            EntityDelta d = null;

            assert td.getEntityDeltas().size() == 2;
            d = td.getEntityDelta(a.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert A.class.getName().equals(d.getEntityName());
            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().size() == 2;
            assert "alice".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert new Integer(30).equals(((PrimitiveDelta)d.getScalarDelta("age")).getValue());

            d = td.getEntityDelta(b.getId(), B.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert B.class.getName().equals(d.getEntityName());
            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().size() == 2;
            assert "bob".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert ((Date)Formats.testDateFormat.parseObject("01/01/1971")).
                equals(((PrimitiveDelta)d.getScalarDelta("birthDate")).getValue());

            td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 2;
            d = td.getEntityDelta(a2.getId(), A.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert A.class.getName().equals(d.getEntityName());
            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().size() == 1;
            assert "anna".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());

            d = td.getEntityDelta(b2.getId(), B.class.getName());
            assert ChangeType.INSERT.equals(d.getChangeType());
            assert B.class.getName().equals(d.getEntityName());
            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().size() == 2;
            assert "ben".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert ((Date)Formats.testDateFormat.parseObject("02/02/1972")).
                equals(((PrimitiveDelta)d.getScalarDelta("birthDate")).getValue());

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

    // Tests initially developed in PostInsertEntityTest -------------------------------------------

    @Test(enabled = false)
    public void testSimpleCascade() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            C c = new C();
            c.setName("charlie");

            D d = new D();
            d.setName("diane");

            c.setD(d);

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(c.getId(), C.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;

            assert "charlie".equals(ed.getPrimitiveDelta("name").getValue());

            EntityReferenceDelta erd = ed.getEntityReferenceDelta("d");
            assert d.getId().equals(erd.getId());
            assert D.class.getName().equals(erd.getEntityName());

            ed = td.getEntityDelta(d.getId(), D.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;

            assert "diane".equals(ed.getPrimitiveDelta("name").getValue());
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
    public void testCascade_TwoTransactions_Delta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();

            s.beginTransaction();

            D d = new D();
            d.setName("diane");

            s.save(d);

            s.getTransaction().commit();
            s.close();

            s = sf.openSession();

            s.beginTransaction();

            // we do this to modify the order in which post-insert events are sent
            d = (D)s.get(D.class, d.getId());

            C c = new C();
            c.setName("charlie");
            c.setD(d);

            s.save(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;
            AuditTransaction tx = null;

            tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(d.getId(), D.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;

            assert "diane".equals(ed.getPrimitiveDelta("name").getValue());

            tx = txs.get(1);

            td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            ed = td.getEntityDelta(c.getId(), C.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;

            assert "charlie".equals(ed.getPrimitiveDelta("name").getValue());
            EntityReferenceDelta erd = ed.getEntityReferenceDelta("d");
            assert d.getId().equals(erd.getId());
            assert D.class.getName().equals(erd.getEntityName());
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
    public void testCascade_TwoTransactions_MultipleEntities_Delta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();

            s.beginTransaction();

            D d = new D();
            d.setName("diane");

            s.save(d);

            s.getTransaction().commit();
            s.close();

            s = sf.openSession();

            s.beginTransaction();

            // we do this to modify the order in which post-insert events are sent
            d = (D)s.get(D.class, d.getId());

            C c = new C();
            c.setName("charlie");
            c.setD(d);

            C c2 = new C();
            c2.setName("connie");
            c2.setD(d);

            s.save(c);
            s.save(c2);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;
            AuditTransaction tx = null;

            tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(d.getId(), D.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;

            assert "diane".equals(ed.getPrimitiveDelta("name").getValue());

            tx = txs.get(1);

            td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 2;

            ed = td.getEntityDelta(c.getId(), C.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;
            assert "charlie".equals(ed.getPrimitiveDelta("name").getValue());
            EntityReferenceDelta erd = ed.getEntityReferenceDelta("d");
            assert d.getId().equals(erd.getId());
            assert D.class.getName().equals(erd.getEntityName());

            ed = td.getEntityDelta(c2.getId(), C.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;
            assert "connie".equals(ed.getPrimitiveDelta("name").getValue());
            erd = ed.getEntityReferenceDelta("d");
            assert d.getId().equals(erd.getId());
            assert D.class.getName().equals(erd.getEntityName());
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

    // Tests initially developed in PostInsertCollectionsTest --------------------------------------

    @Test(enabled = false)
    public void testInsert_EmptyCollection() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            WA wa = new WA();
            s.save(wa);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(wa.getId(), WA.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().isEmpty();
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
    public void testAddOneInCollection() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            WA wa = new WA();
            wa.setName("wasabi");

            WB wb = new WB();
            wb.setName("wbang");

            wa.getWbs().add(wb);
            wb.setWa(wa);

            s.save(wa);
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(wa.getId(), WA.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().size() == 1;
            assert "wasabi".equals(ed.getPrimitiveDelta("name").getValue());
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("wbs");
            assert WB.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 1;
            assert ids.contains(wb.getId());


            ed = td.getEntityDelta(wb.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;

            PrimitiveDelta pd = ed.getPrimitiveDelta("name");
            assert "wbang".equals(pd.getValue());
            EntityReferenceDelta erd = ed.getEntityReferenceDelta("wa");
            assert WA.class.getName().equals(erd.getEntityName());
            assert wa.getId().equals(erd.getId());
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
    public void testAddOneInCollection_NoBidirectionality() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            WA wa = new WA();
            wa.setName("alana");

            WB wb = new WB();
            wb.setName("baja");

            wa.getWbs().add(wb);

            s.save(wa);
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(wa.getId(), WA.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().size() == 1;
            assert "alana".equals(ed.getPrimitiveDelta("name").getValue());
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("wbs");
            assert WB.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 1;
            assert ids.contains(wb.getId());


            ed = td.getEntityDelta(wb.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;

            PrimitiveDelta pd = ed.getPrimitiveDelta("name");
            assert "baja".equals(pd.getValue());
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
    public void testAddTwoInCollection_Bidirectionality() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            WA wa = new WA();
            wa.setName("wasabi");

            WB wb = new WB();
            wb.setName("wbang");

            WB wb2 = new WB();
            wb2.setName("wbong");

            wa.getWbs().add(wb);
            wa.getWbs().add(wb2);

            wb.setWa(wa);
            wb2.setWa(wa);

            s.save(wa);
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 3;

            EntityDelta ed = td.getEntityDelta(wa.getId(), WA.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().size() == 1;
            assert "wasabi".equals(ed.getPrimitiveDelta("name").getValue());
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("wbs");
            assert WB.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 2;
            assert ids.contains(wb.getId());
            assert ids.contains(wb2.getId());

            ed = td.getEntityDelta(wb.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;
            assert "wbang".equals(ed.getPrimitiveDelta("name").getValue());
            EntityReferenceDelta erd = ed.getEntityReferenceDelta("wa");
            assert WA.class.getName().equals(erd.getEntityName());
            assert wa.getId().equals(erd.getId());

            ed = td.getEntityDelta(wb2.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;
            assert "wbong".equals(ed.getPrimitiveDelta("name").getValue());
            erd = ed.getEntityReferenceDelta("wa");
            assert WA.class.getName().equals(erd.getEntityName());
            assert wa.getId().equals(erd.getId());
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
    public void testAddTwoInCollection_NoBidirectionalityFromWBToWA() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            WA wa = new WA();
            wa.setName("wasabi");

            WB wb = new WB();
            wb.setName("wbang");

            WB wb2 = new WB();
            wb2.setName("wbong");

            wa.getWbs().add(wb);
            wa.getWbs().add(wb2);

            // we stop here, no bidirectionality from WB to WA

            s.save(wa);
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 3;

            EntityDelta ed = td.getEntityDelta(wa.getId(), WA.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().size() == 1;
            assert "wasabi".equals(ed.getPrimitiveDelta("name").getValue());
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("wbs");
            assert WB.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 2;
            assert ids.contains(wb.getId());
            assert ids.contains(wb2.getId());

            ed = td.getEntityDelta(wb.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert "wbang".equals(ed.getPrimitiveDelta("name").getValue());

            ed = td.getEntityDelta(wb2.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert "wbong".equals(ed.getPrimitiveDelta("name").getValue());
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
    public void testModifyOneFromCollection() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            // create the state in database, without auditing
            assert !HibernateAudit.isStarted();

            Session s = sf.openSession();
            s.beginTransaction();

            WA wa = new WA();
            wa.setName("wasabi");

            WB wb = new WB();
            wb.setName("wbang");

            WB wb2 = new WB();
            wb2.setName("wbong");

            wa.getWbs().add(wb);
            wa.getWbs().add(wb2);

            s.save(wa);
            s.getTransaction().commit();
            s.close();

            // load the state from the database
            s = sf.openSession();
            s.beginTransaction();

            wa = (WA)s.get(WA.class, wa.getId());
            assert "wasabi".equals(wa.getName());
            List<WB> wbs = wa.getWbs();
            assert wbs.size() == 2;

            // enable audit
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            WB newWb = new WB();
            newWb.setName("wbung");
            wa.getWbs().set(0, newWb);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(wa.getId(), WA.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getScalarDeltas().size() == 0;
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("wbs");
            assert WB.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 2;
            assert ids.contains(wb2.getId());
            assert ids.contains(newWb.getId());

            ed = td.getEntityDelta(newWb.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert "wbung".equals(ed.getPrimitiveDelta("name").getValue());
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
    public void testInsert_ACollectionAndNothingElseButEmptyState() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            WA wa = new WA();
            WB wb = new WB();
            wa.getWbs().add(wb);

            Session s = sf.openSession();
            s.beginTransaction();
            s.save(wa);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(wa.getId(), WA.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("wbs");
            assert WB.class.getName().equals(cd.getMemberEntityName());
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 1;
            assert ids.contains(wb.getId());

            ed = td.getEntityDelta(wb.getId(), WB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();
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

    // Tests initially developed in PostInsertTuplizerEntityTest -----------------------------------

    @Test(enabled = false)
    public void testManyToOne_OneIsTuplizer() throws Exception
    {
        Configuration config = new Configuration();
        config.configure(getHibernateConfigurationFileName());

        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
        // enough)

        String xaMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA' table='XA'>\n" +
            "        <id name='id' type='long'>\n" +
            "            <generator class='native'/>\n" +
            "        </id>\n" +
            "        <property name='name' type='string'/>\n" +
            "        <many-to-one name='xb' column='xb_id' entity-name='XB' cascade='all'/>\n" +
            "    </class>\n" +
            "</hibernate-mapping>";

        String xbMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
            "      <id name='id' type='long'>\n" +
            "         <generator class='native'/>\n" +
            "      </id>\n" +
            "      <property name='name' type='string'/>\n" +
            "   </class>\n" +
            "</hibernate-mapping>";

        config.addInputStream(new ByteArrayInputStream(xaMapping.getBytes()));
        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            XA xa = new XA();
            XB xb = new XB();

            XBTuplizer tuplizer = new XBTuplizer();
            tuplizer.setPropertyValue(xb, "name", "xbone");

            xa.setXb(xb);
            s.save(xa);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(xa.getId(), XA.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().size() == 1;
            assert ed.getCollectionDeltas().isEmpty();

            EntityReferenceDelta erd = ed.getEntityReferenceDelta("xb");

            // TODO this test is faked, fix it when addressing HBA-80
            //assert "XB".equals(erd.getEntityName());
            assert XB.class.getName().equals(erd.getEntityName());

            assert xb.getId().equals(erd.getId());

            // TODO this test is faked, fix it when addressing HBA-80
            //ed = td.getEntityDelta(xb.getId(), "XB");
            ed = td.getEntityDelta(xb.getId(), XB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert "xbone".equals(ed.getPrimitiveDelta("name").getValue());
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
    public void testManyToOne_OneIsTuplizer_Collection() throws Exception
    {
        Configuration config = new Configuration();
        config.configure(getHibernateConfigurationFileName());

        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
        // enough)

        String xa2Mapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA2' table='XA2'>\n" +
            "        <id name='id' type='long'>\n" +
            "            <generator class='native'/>\n" +
            "        </id>\n" +
            "        <set name='xbs' cascade='all'>\n" +
            "            <key column='xa_id'/>\n" +
            "            <one-to-many entity-name='XB'/>\n" +
            "        </set>\n" +
            "    </class>\n" +
            "</hibernate-mapping>";

        String xbMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
            "      <id name='id' type='long'>\n" +
            "         <generator class='native'/>\n" +
            "      </id>\n" +
            "      <property name='name' type='string'/>\n" +
            "   </class>\n" +
            "</hibernate-mapping>";

        config.addInputStream(new ByteArrayInputStream(xa2Mapping.getBytes()));
        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            XA2 xa2 = new XA2();
            XB xbone = new XB();
            XB xbtwo = new XB();

            XBTuplizer tuplizer = new XBTuplizer();
            tuplizer.setPropertyValue(xbone, "name", "xbone");
            tuplizer.setPropertyValue(xbtwo, "name", "xbtwo");

            Set<XB> xbs = xa2.getXbs();
            xbs.add(xbone);
            xbs.add(xbtwo);

            s.save(xa2);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 3;

            EntityDelta ed = td.getEntityDelta(xa2.getId(), XA2.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 1;

            CollectionDelta cd = ed.getCollectionDelta("xbs");

            // TODO this test is faked, fix it when addressing HBA-80
            //assert "XB".eequals(cd.getMemberEntityName());
            assert XB.class.getName().equals(cd.getMemberEntityName());

            assert cd.getIds().size() == 2;
            assert cd.getIds().contains(xbone.getId());
            assert cd.getIds().contains(xbtwo.getId());

            // TODO this test is faked, fix it when addressing HBA-80
            //ed = td.getEntityDelta(xbone.getId(), "XB");
            ed = td.getEntityDelta(xbone.getId(), XB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());

            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert "xbone".equals(ed.getPrimitiveDelta("name").getValue());

            // TODO this test is faked, fix it when addressing HBA-80
            //ed = td.getEntityDelta(xbtwo.getId(), "XB");
            ed = td.getEntityDelta(xbtwo.getId(), XB.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());

            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            assert "xbtwo".equals(ed.getPrimitiveDelta("name").getValue());
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
    public void testManyToOne_OneIsTuplizer_EmptyCollection() throws Exception
    {
        Configuration config = new Configuration();
        config.configure(getHibernateConfigurationFileName());

        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
        // enough)

        String xa2Mapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA2' table='XA2'>\n" +
            "        <id name='id' type='long'>\n" +
            "            <generator class='native'/>\n" +
            "        </id>\n" +
            "        <set name='xbs' cascade='all'>\n" +
            "            <key column='xa_id'/>\n" +
            "            <one-to-many entity-name='XB'/>\n" +
            "        </set>\n" +
            "    </class>\n" +
            "</hibernate-mapping>";

        String xbMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
            "<hibernate-mapping>\n" +
            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
            "      <id name='id' type='long'>\n" +
            "         <generator class='native'/>\n" +
            "      </id>\n" +
            "      <property name='name' type='string'/>\n" +
            "   </class>\n" +
            "</hibernate-mapping>";

        config.addInputStream(new ByteArrayInputStream(xa2Mapping.getBytes()));
        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            XA2 xa2 = new XA2();

            // empty collection

            s.save(xa2);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 1;

            EntityDelta ed = td.getEntityDelta(xa2.getId(), XA2.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().isEmpty();
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

    /**
     * https://jira.novaordis.org/browse/HBA-132
     */
    @Test(enabled = false)
    public void testInsert_TypeCache() throws Exception
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
            a.setName("x");

            Session s = sf.openSession();
            s.beginTransaction();
            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());
            assert td.getEntityDeltas().size() == 1;
            EntityDelta ed = td.getEntityDelta(a.getId(), A.class.getName());
            assert ed != null;

            // now use already cached types and fields

            s = sf.openSession();
            s.beginTransaction();
            a = new A();
            a.setName("y");
            s.save(a);
            s.getTransaction().commit();
            s.close();

            txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;
            td = HibernateAudit.getDelta(txs.get(1).getId());
            assert td.getEntityDeltas().size() == 1;
            ed = td.getEntityDelta(a.getId(), A.class.getName());
            assert ed != null;
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
     * https://jira.novaordis.org/browse/HBA-132
     */
    @Test(enabled = true)
    public void testInsert_TypeCache_NonUniqueException() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(H.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            H h = new H();
            h.setS0("s0");
            h.setS1("s0");

            Session s = sf.openSession();
            s.beginTransaction();
            s.save(h);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());
            assert td.getEntityDeltas().size() == 1;
            EntityDelta ed = td.getEntityDelta(h.getId(), H.class.getName());
            assert ed != null;
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
