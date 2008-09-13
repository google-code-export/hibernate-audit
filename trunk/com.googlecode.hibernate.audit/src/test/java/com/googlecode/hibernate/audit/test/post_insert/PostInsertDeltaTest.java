package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.A;
import com.googlecode.hibernate.audit.test.post_insert.data.B;
import com.googlecode.hibernate.audit.test.post_insert.data.C;
import com.googlecode.hibernate.audit.test.post_insert.data.D;
import com.googlecode.hibernate.audit.test.util.Formats;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.ScalarDelta;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.EntityReferenceDelta;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.Set;
import java.util.Date;

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

    @Test(enabled = true)
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
    
    @Test(enabled = true)
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

    @Test(enabled = true)
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
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 1;
            ScalarDelta sd = ed.getScalarDelta("name");
            PrimitiveDelta pd = (PrimitiveDelta)sd;
            assert String.class.equals(pd.getType());
            assert "alice".equals(pd.getValue());

            ed = td.getEntityDelta(a2.getId(), A.class.getName());
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

    @Test(enabled = true)
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

    @Test(enabled = true)
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
            assert d.getScalarDeltas().size() == 1;
            assert "alice".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert A.class.getName().equals(d.getEntityName());

            d = td.getEntityDelta(b.getId(), B.class.getName());
            assert d.getScalarDeltas().size() == 1;
            assert "bob".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert B.class.getName().equals(d.getEntityName());

            d = td.getEntityDelta(b2.getId(), B.class.getName());
            assert d.getScalarDeltas().size() == 1;
            assert "ben".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert B.class.getName().equals(d.getEntityName());

            d = td.getEntityDelta(a2.getId(), A.class.getName());
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

    @Test(enabled = true)
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
            assert A.class.getName().equals(d.getEntityName());
            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().size() == 2;
            assert "alice".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert new Integer(30).equals(((PrimitiveDelta)d.getScalarDelta("age")).getValue());

            d = td.getEntityDelta(b.getId(), B.class.getName());
            assert B.class.getName().equals(d.getEntityName());
            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().size() == 2;
            assert "bob".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());
            assert ((Date)Formats.testDateFormat.parseObject("01/01/1971")).
                equals(((PrimitiveDelta)d.getScalarDelta("birthDate")).getValue());

            td = HibernateAudit.getDelta(txs.get(1).getId());

            assert td.getEntityDeltas().size() == 2;
            d = td.getEntityDelta(a2.getId(), A.class.getName());
            assert A.class.getName().equals(d.getEntityName());
            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().size() == 1;
            assert "anna".equals(((PrimitiveDelta)d.getScalarDelta("name")).getValue());

            d = td.getEntityDelta(b2.getId(), B.class.getName());
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

    @Test(enabled = true)
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
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().size() == 2;

            assert "charlie".equals(ed.getPrimitiveDelta("name").getValue());

            EntityReferenceDelta erd = ed.getEntityReferenceDelta("d");
            assert d.getId().equals(erd.getId());
            assert D.class.getName().equals(erd.getEntityName());

            ed = td.getEntityDelta(d.getId(), D.class.getName());
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

//    @Test(enabled = true)
//    public void testSimpleCascade_Delta() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(C.class);
//        config.addAnnotatedClass(D.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            C c = new C();
//            c.setName("charlie");
//
//            D d = new D();
//            d.setName("diane");
//
//            c.setD(d);
//
//            Session s = sf.openSession();
//            Transaction t = s.beginTransaction();
//
//            s.save(c);
//
//            t.commit();
//
//            Long cId = c.getId();
//            Long dId = d.getId();
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            C cBase = new C();
//            HibernateAudit.delta(cBase, cId, at.getId());
//
//            assert cId.equals(cBase.getId());
//
//            assert "charlie".equals(cBase.getName());
//
//            D recreatedD = cBase.getD();
//            assert recreatedD != d;
//            assert dId.equals(recreatedD.getId());
//            assert "diane".equals(recreatedD.getName());
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testCascade_TwoTransactions_Delta() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(C.class);
//        config.addAnnotatedClass(D.class);
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
//
//            // transaction one
//            s.beginTransaction();
//
//            D d = new D();
//            d.setName("diane");
//
//            s.save(d);
//
//            Long dId = d.getId();
//
//            s.getTransaction().commit();
//            s.close();
//
//            s = sf.openSession();
//
//            // transaction two
//            s.beginTransaction();
//
//            // we do this to modify the order in which post-insert events are sent
//            d = (D)s.get(D.class, dId);
//
//            C c = new C();
//            c.setName("charlie");
//            c.setD(d);
//
//            s.save(c);
//
//            s.getTransaction().commit();
//
//            Long cId = c.getId();
//
//            List transactions = HibernateAudit.query("from AuditTransaction as a order by a.id");
//            assert transactions.size() == 2;
//            AuditTransaction at = (AuditTransaction)transactions.get(1);
//
//            C cBase = new C();
//            HibernateAudit.delta(cBase, cId, at.getId());
//
//            assert cId.equals(cBase.getId());
//
//            assert "charlie".equals(cBase.getName());
//
//            D recreatedD = cBase.getD();
//            assert recreatedD != d;
//            assert dId.equals(recreatedD.getId());
//            assert "diane".equals(recreatedD.getName());
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testCascade_TwoTransactions_MultipleEntities_Delta() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(C.class);
//        config.addAnnotatedClass(D.class);
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
//
//            // transaction one
//            s.beginTransaction();
//
//            D d = new D();
//            d.setName("diane");
//
//            s.save(d);
//
//            Long dId = d.getId();
//
//            s.getTransaction().commit();
//            s.close();
//
//            s = sf.openSession();
//
//            // transaction two
//            s.beginTransaction();
//
//            // we do this to modify the order in which post-insert events are sent
//            d = (D)s.get(D.class, dId);
//
//            C c = new C();
//            c.setName("charlie");
//            c.setD(d);
//
//            C c2 = new C();
//            c2.setName("connie");
//            c2.setD(d);
//
//            s.save(c);
//            s.save(c2);
//
//            s.getTransaction().commit();
//
//            Long cId = c.getId();
//            Long cId2 = c2.getId();
//
//            List transactions = HibernateAudit.query("from AuditTransaction as a order by a.id");
//            assert transactions.size() == 2;
//            AuditTransaction at = (AuditTransaction)transactions.get(1);
//
//            C cBase = new C();
//            HibernateAudit.delta(cBase, cId, at.getId());
//
//            assert cId.equals(cBase.getId());
//
//            assert "charlie".equals(cBase.getName());
//
//            D recreatedD = cBase.getD();
//            assert recreatedD != d;
//            assert dId.equals(recreatedD.getId());
//            assert "diane".equals(recreatedD.getName());
//
//            cBase = new C();
//            HibernateAudit.delta(cBase, cId2, at.getId());
//
//            assert cId2.equals(cBase.getId());
//
//            assert "connie".equals(cBase.getName());
//
//            recreatedD = cBase.getD();
//            assert recreatedD != d;
//            assert dId.equals(recreatedD.getId());
//            assert "diane".equals(recreatedD.getName());
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }

    // coming from PostInsertCollectionsTest

//    @Test(enabled = true)
//    public void testAddOneInCollection() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(WA.class);
//        config.addAnnotatedClass(WB.class);
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
//            Transaction t = s.beginTransaction();
//
//            WA wa = new WA();
//            wa.setName("wasabi");
//
//            WB wb = new WB();
//            wb.setName("wbang");
//
//            wa.getWbs().add(wb);
//            wb.setWa(wa);
//
//            s.save(wa);
//            t.commit();
//
//            // verify the data is in the database
//
//            List<Long> targetIds = new ArrayList<Long>(Arrays.asList(wa.getId(), wb.getId()));
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            List types = HibernateAudit.query("from AuditType");
//            assert types.size() == 4;
//
//            Set<AuditType> expectedTargetTypes = new HashSet<AuditType>();
//
//            for(Object o: types)
//            {
//                AuditType type = (AuditType)o;
//
//                if (type.isEntityType())
//                {
//                    expectedTargetTypes.add(type);
//                }
//                else if (type.isCollectionType())
//                {
//                    AuditCollectionType ct = (AuditCollectionType)type;
//                    assert List.class.equals(ct.getCollectionClassInstance());
//                    assert WB.class.equals(ct.getClassInstance());
//                }
//                else if (String.class.equals(type.getClassInstance()))
//                {
//                    // ok
//                }
//                else
//                {
//                    throw new Error("unexpected type " + type);
//                }
//            }
//
//            assert expectedTargetTypes.size() == 2;
//
//            List events = HibernateAudit.query("from AuditEvent");
//            assert events.size() == 2;
//
//            for(Object o: events)
//            {
//                AuditEvent e = (AuditEvent)o;
//
//                assert at.equals(e.getTransaction());
//                assert ChangeType.INSERT.equals(e.getType());
//                assert expectedTargetTypes.remove(e.getTargetType());
//                assert targetIds.remove(e.getTargetId());
//            }
//
//            List pairs = HibernateAudit.query("from AuditEventPair");
//            assert pairs.size() == 4;
//
//            List<String> expectedStringValues = new ArrayList<String>(Arrays.
//                asList(wa.getName(),
//                       wb.getName(),
//                       Long.toString(wa.getId()))); // the WA's id as a foreign key in WB's table.
//
//            for(Object o: pairs)
//            {
//                AuditEventPair pair = (AuditEventPair)o;
//
//                if (pair.isCollection())
//                {
//                    AuditEventCollectionPair cp = (AuditEventCollectionPair)pair;
//                    List<Long> ids = cp.getIds();
//
//                    assert 1 == ids.size();
//                    assert ids.remove(wb.getId());
//                }
//                else
//                {
//                    // TODO we're not testing pair.getValue() because at this time is not implemented
//                    assert expectedStringValues.remove(pair.getStringValue());
//                }
//            }
//
//            assert expectedStringValues.isEmpty();
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testAddOneInCollection_Delta() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(WA.class);
//        config.addAnnotatedClass(WB.class);
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
//            Transaction t = s.beginTransaction();
//
//            WA wa = new WA();
//            wa.setName("alana");
//
//            WB wb = new WB();
//            wb.setName("baja");
//
//            wa.getWbs().add(wb);
//            wb.setWa(wa);
//
//            s.save(wa);
//            t.commit();
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            Long waId = wa.getId();
//            Long wbId = wb.getId();
//
//            WA base = new WA();
//            HibernateAudit.delta(base, waId, at.getId());
//
//            assert waId.equals(base.getId());
//            assert "alana".equals(wa.getName());
//
//            List<WB> wbs = base.getWbs();
//            assert !wa.getWbs().equals(wbs);
//
//            assert wbs.size() == 1;
//
//            WB postTWb = wbs.get(0);
//            assert postTWb != wb;
//
//            assert postTWb.getId().equals(wbId);
//            assert "baja".equals(postTWb.getName());
//
//            assert base == postTWb.getWa();
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testAddOneInCollection_NoBidirectionality_Delta() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(WA.class);
//        config.addAnnotatedClass(WB.class);
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
//            Transaction t = s.beginTransaction();
//
//            WA wa = new WA();
//            wa.setName("alana");
//
//            WB wb = new WB();
//            wb.setName("baja");
//
//            wa.getWbs().add(wb);
//
//            s.save(wa);
//            t.commit();
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            Long waId = wa.getId();
//            Long wbId = wb.getId();
//
//            WA base = new WA();
//            HibernateAudit.delta(base, waId, at.getId());
//
//            assert waId.equals(base.getId());
//            assert "alana".equals(wa.getName());
//
//            List<WB> wbs = base.getWbs();
//            assert !wa.getWbs().equals(wbs);
//
//            assert wbs.size() == 1;
//
//            WB postTWb = wbs.get(0);
//            assert postTWb != wb;
//
//            assert postTWb.getId().equals(wbId);
//            assert "baja".equals(postTWb.getName());
//
//            // no bidirectionality
//            assert postTWb.getWa() == null;
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testAddTwoInCollection_Bidirectionality() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(WA.class);
//        config.addAnnotatedClass(WB.class);
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
//            Transaction t = s.beginTransaction();
//
//            WA wa = new WA();
//            wa.setName("wasabi");
//
//            WB wb = new WB();
//            wb.setName("wbang");
//
//            WB wb2 = new WB();
//            wb2.setName("wbong");
//
//            wa.getWbs().add(wb);
//            wa.getWbs().add(wb2);
//
//            wb.setWa(wa);
//            wb2.setWa(wa);
//
//            s.save(wa);
//            t.commit();
//
//            Long waId = wa.getId();
//            Long wbId = wb.getId();
//            Long wb2Id = wb2.getId();
//            Set<Long> expectedWbIds = new HashSet<Long>();
//            expectedWbIds.add(wbId);
//            expectedWbIds.add(wb2Id);
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            WA baseA = new WA();
//            HibernateAudit.delta(baseA, waId, at.getId());
//
//            assert waId.equals(baseA.getId());
//            assert "wasabi".equals(wa.getName());
//
//            List<WB> wbs = baseA.getWbs();
//            assert !wa.getWbs().equals(wbs);
//
//            assert wbs.size() == 2;
//
//            for(WB b: wbs)
//            {
//                assert b != wb;
//                assert b != wb2;
//                assert expectedWbIds.remove(b.getId());
//                assert baseA == b.getWa();
//                if (wb.getId().equals(b.getId()))
//                {
//                    assert wb.getName().equals(b.getName());
//                }
//                else if (wb2.getId().equals(b.getId()))
//                {
//                    assert wb2.getName().equals(b.getName());
//                }
//                else
//                {
//                    throw new Error("did not expect " + b.getId());
//                }
//            }
//
//            assert expectedWbIds.isEmpty();
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testAddTwoInCollection_NoBidirectionalityFromWBToWA() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(WA.class);
//        config.addAnnotatedClass(WB.class);
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
//            Transaction t = s.beginTransaction();
//
//            WA wa = new WA();
//            wa.setName("wasabi");
//
//            WB wb = new WB();
//            wb.setName("wbang");
//
//            WB wb2 = new WB();
//            wb2.setName("wbong");
//
//            wa.getWbs().add(wb);
//            wa.getWbs().add(wb2);
//
//            // we stop here, no bidirectionality from WB to WA
//
//            s.save(wa);
//            t.commit();
//
//            Long waId = wa.getId();
//            Long wbId = wb.getId();
//            Long wb2Id = wb2.getId();
//            Set<Long> expectedWbIds = new HashSet<Long>();
//            expectedWbIds.add(wbId);
//            expectedWbIds.add(wb2Id);
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            WA baseA = new WA();
//            HibernateAudit.delta(baseA, waId, at.getId());
//
//            assert waId.equals(baseA.getId());
//            assert "wasabi".equals(wa.getName());
//
//            List<WB> wbs = baseA.getWbs();
//            assert !wa.getWbs().equals(wbs);
//
//            assert wbs.size() == 2;
//
//            for(WB b: wbs)
//            {
//                assert b != wb;
//                assert b != wb2;
//                assert expectedWbIds.remove(b.getId());
//                assert b.getWa() == null;
//                if (wb.getId().equals(b.getId()))
//                {
//                    assert wb.getName().equals(b.getName());
//                }
//                else if (wb2.getId().equals(b.getId()))
//                {
//                    assert wb2.getName().equals(b.getName());
//                }
//                else
//                {
//                    throw new Error("did not expect " + b.getId());
//                }
//            }
//
//            assert expectedWbIds.isEmpty();
//
//            HibernateAudit.stopRuntime();
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true)
//    public void testModifyOneFromCollection() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(WA.class);
//        config.addAnnotatedClass(WB.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            // create the state in database, without auditing
//            assert !HibernateAudit.isStarted();
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            WA wa = new WA();
//            wa.setName("wasabi");
//
//            WB wb = new WB();
//            wb.setName("wbang");
//
//            WB wb2 = new WB();
//            wb2.setName("wbong");
//
//            wa.getWbs().add(wb);
//            wa.getWbs().add(wb2);
//
//            s.save(wa);
//            s.getTransaction().commit();
//            s.close();
//
//            // load the state from the database
//
//            s = sf.openSession();
//            s.beginTransaction();
//
//            wa = (WA)s.get(WA.class, wa.getId());
//
//            assert "wasabi".equals(wa.getName());
//
//            List<String> expected = new ArrayList<String>(Arrays.asList("wbang", "wbong"));
//            List<WB> wbs = wa.getWbs();
//            assert wbs.size() == 2;
//            for(WB i: wbs)
//            {
//                assert expected.remove(i.getName());
//            }
//
//            // enable auditing
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            WB newWb = new WB();
//            newWb.setName("wbung");
//            wa.getWbs().set(0, newWb);
//
//            s.getTransaction().commit();
//            s.close();
//
//            // "raw" access test
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions(wa.getId());
//            assert txs.size() == 1;
//
//            AuditTransaction tx = txs.get(0);
//
//            List events =
//                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);
//
//            assert events.size() == 2;
//
//            for(Object o: events)
//            {
//                AuditEvent ae = (AuditEvent)o;
//                assert tx.equals(ae.getTransaction());
//                ChangeType ct = ae.getType();
//
//                List pairs =
//                    HibernateAudit.query("from AuditEventPair as p where p.event = :event", ae);
//
//                if (ChangeType.INSERT.equals(ct))
//                {
//                    assert newWb.getId().equals(ae.getTargetId());
//                    assert pairs.size() == 1;
//                    AuditEventPair p = (AuditEventPair)pairs.get(0);
//                    assert "name".equals(p.getField().getName());
//                    assert "wbung".equals(p.getValue());
//                }
//                else if (ChangeType.UPDATE.equals(ct))
//                {
//                    assert wa.getId().equals(ae.getTargetId());
//                    assert pairs.size() == 1;
//                    AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);
//                    assert "wbs".equals(p.getField().getName());
//                    List ids = (List)p.getValue();
//                    assert ids.size() == 2;
//                    assert newWb.getId().equals(ids.get(0));
//                    assert wb2.getId().equals(ids.get(1));
//                }
//                else
//                {
//                    throw new Error("invalid state");
//                }
//            }
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
//    public void testInsert_ACollectionAndNothingElseButEmptyState() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(WA.class);
//        config.addAnnotatedClass(WB.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            WA wa = new WA();
//            WB wb = new WB();
//            wa.getWbs().add(wb);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            s.save(wa);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(wa.getId());
//
//            assert transactions.size() == 1;
//
//            Long txId = transactions.get(0).getId();
//
//            WA base = new WA();
//            HibernateAudit.delta(base, wa.getId(), txId);
//
//            assert wa.getId().equals(base.getId());
//            assert base.getName() == null;
//
//            List<WB> wbs = wa.getWbs();
//            assert wbs.size() == 1;
//
//            WB wbCopy = wbs.get(0);
//            assert wbCopy.getId().equals(wb.getId());
//            assert wbCopy.getName() == null;
//            assert wbCopy.getWa() == null;
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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


    // coming from PostInsertTuplizerEntityTest

//    @Test(enabled = true)
//    public void testManyToOne_OneIsTuplizer() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xaMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA' table='XA'>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <property name='name' type='string'/>\n" +
//            "        <many-to-one name='xb' column='xb_id' entity-name='XB' cascade='all'/>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xbMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xaMapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
//
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
//            XA xa = new XA();
//            XB xb = new XB();
//
//            XBTuplizer tuplizer = new XBTuplizer();
//            tuplizer.setPropertyValue(xb, "name", "xbone");
//
//            xa.setXb(xb);
//
//            s.save(xa);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa.getId());
//            assert transactions.size() == 1;
//
//            XA base = new XA();
//            HibernateAudit.delta(base, xa.getId(), transactions.get(0).getId());
//
//            XB restored = base.getXb();
//            assert xb.getId().equals(restored.getId());
//            assert "xbone".equals(restored.getName());
//
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
////    @Test(enabled = true) // TODO https://jira.novaordis.org/browse/HBA-81
////    public void testMissingMutatorThatMayBeSalvagedByTuplizer() throws Exception
////    {
////        Configuration config = new Configuration();
////        config.configure(getHibernateConfigurationFileName());
////
////        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
////        // enough)
////
////        String xaMapping =
////            "<?xml version='1.0'?>\n" +
////            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
////            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
////            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
////            "<hibernate-mapping>\n" +
////            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA' table='XA'>\n" +
////            "        <id name='id' type='long'>\n" +
////            "            <generator class='native'/>\n" +
////            "        </id>\n" +
////            "        <property name='name' type='string'/>\n" +
////            "        <many-to-one name='xc' column='xc_id' entity-name='XC' cascade='all'/>\n" +
////            "    </class>\n" +
////            "</hibernate-mapping>";
////
////        String xcMapping =
////            "<?xml version='1.0'?>\n" +
////            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
////            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
////            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
////            "<hibernate-mapping>\n" +
////            "   <class entity-name='XC' name='com.googlecode.hibernate.audit.test.post_insert.data.XC' table='XC'>\n" +
////            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XCTuplizer'/>\n" +
////            "      <id name='id' type='long'>\n" +
////            "         <generator class='native'/>\n" +
////            "      </id>\n" +
////            "      <property name='name' type='string'/>\n" +
////            "   </class>\n" +
////            "</hibernate-mapping>";
////
////        config.addInputStream(new ByteArrayInputStream(xaMapping.getBytes()));
////        config.addInputStream(new ByteArrayInputStream(xcMapping.getBytes()));
////
////        SessionFactoryImplementor sf = null;
////
////        try
////        {
////            sf = (SessionFactoryImplementor)config.buildSessionFactory();
////
////            HibernateAudit.startRuntime(sf.getSettings());
////            HibernateAudit.register(sf);
////
////            Session s = sf.openSession();
////            s.beginTransaction();
////
////            XA xa = new XA();
////            XC xc = new XC();
////
////            XCTuplizer tuplizer = new XCTuplizer();
////            tuplizer.setPropertyValue(xc, "name", "xcone");
////
////            xa.setXc(xc);
////
////            s.save(xa);
////
////            s.getTransaction().commit();
////            s.close();
////
////            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa.getId());
////            assert transactions.size() == 1;
////
////            XA base = new XA();
////            HibernateAudit.delta(base, xa.getId(), transactions.get(0).getId());
////
////            XC restored = base.getXc();
////            assert xc.getId().equals(restored.getId());
////            assert "xcone".equals(restored.getName());
////        }
////        catch(Exception e)
////        {
////            log.error("test failed unexpectedly", e);
////            throw e;
////        }
////        finally
////        {
////            HibernateAudit.stopRuntime();
////
////            if (sf != null)
////            {
////                sf.close();
////            }
////        }
////    }
//
//    @Test(enabled = true)
//    public void testManyToOne_OneIsTuplizer_Collection() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xa2Mapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA2' table='XA2'>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <set name='xbs' cascade='all'>\n" +
//            "            <key column='xa_id'/>\n" +
//            "            <one-to-many entity-name='XB'/>\n" +
//            "        </set>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xbMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xa2Mapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
//
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
//            XA2 xa2 = new XA2();
//            XB xbone = new XB();
//            XB xbtwo = new XB();
//
//            XBTuplizer tuplizer = new XBTuplizer();
//            tuplizer.setPropertyValue(xbone, "name", "xbone");
//            tuplizer.setPropertyValue(xbtwo, "name", "xbtwo");
//
//            Set<XB> xbs = xa2.getXbs();
//            xbs.add(xbone);
//            xbs.add(xbtwo);
//
//            s.save(xa2);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa2.getId());
//            assert transactions.size() == 1;
//
//            XA2 base = new XA2();
//            HibernateAudit.delta(base, xa2.getId(), transactions.get(0).getId());
//
//            Set<XB> restored = base.getXbs();
//            assert restored.size() == 2;
//
//            for(XB xb: restored)
//            {
//                if (xbone.getId().equals(xb.getId()))
//                {
//                    assert "xbone".equals(xb.getName());
//                }
//                else if (xbtwo.getId().equals(xb.getId()))
//                {
//                    assert "xbtwo".equals(xb.getName());
//                }
//                else
//                {
//                    throw new Error("unexpected " + xb);
//                }
//            }
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
////    @Test(enabled = true) // TODO 1.1 https://jira.novaordis.org/browse/HBA-107
////    public void testManyToOne_BothAreTuplizers_Collection() throws Exception
////    {
////        Configuration config = new Configuration();
////        config.configure(getHibernateConfigurationFileName());
////
////        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
////        // enough)
////
////        String xa3Mapping =
////            "<?xml version='1.0'?>\n" +
////            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
////            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
////            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
////            "<hibernate-mapping>\n" +
////            "   <class entity-name='XA3' name='com.googlecode.hibernate.audit.test.post_insert.data.XA3' table='XA3'>\n" +
////            "        <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XA3Tuplizer'/>\n" +
////            "        <id name='id' type='long'>\n" +
////            "            <generator class='native'/>\n" +
////            "        </id>\n" +
////            "        <set name='xbs' cascade='all'>\n" +
////            "            <key column='xa_id'/>\n" +
////            "            <one-to-many entity-name='XB'/>\n" +
////            "        </set>\n" +
////            "    </class>\n" +
////            "</hibernate-mapping>";
////
////        String xbMapping =
////            "<?xml version='1.0'?>\n" +
////            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
////            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
////            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
////            "<hibernate-mapping>\n" +
////            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
////            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
////            "      <id name='id' type='long'>\n" +
////            "         <generator class='native'/>\n" +
////            "      </id>\n" +
////            "      <property name='name' type='string'/>\n" +
////            "   </class>\n" +
////            "</hibernate-mapping>";
////
////        config.addInputStream(new ByteArrayInputStream(xa3Mapping.getBytes()));
////        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
////
////        SessionFactoryImplementor sf = null;
////
////        try
////        {
////            sf = (SessionFactoryImplementor)config.buildSessionFactory();
////
////            HibernateAudit.startRuntime(sf.getSettings());
////            HibernateAudit.register(sf);
////
////            Session s = sf.openSession();
////            s.beginTransaction();
////
////            XA3 xa3 = new XA3();
////            XB xbone = new XB();
////            XB xbtwo = new XB();
////
////            XBTuplizer xbTuplizer = new XBTuplizer();
////            xbTuplizer.setPropertyValue(xbone, "name", "xbone");
////            xbTuplizer.setPropertyValue(xbtwo, "name", "xbtwo");
////
////            Set<XB> xbs = new HashSet<XB>();
////            xbs.add(xbone);
////            xbs.add(xbtwo);
////            XA3Tuplizer xa3Tuplizer = new XA3Tuplizer();
////            xa3Tuplizer.setPropertyValue(xa3, "xbs", xbs);
////
////            s.save("XA3", xa3);
////
////            s.getTransaction().commit();
////            s.close();
////
////            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa3.getId());
////            assert transactions.size() == 1;
////
////            XA3 base = new XA3();
////            HibernateAudit.delta(base, "XA3", xa3.getId(), transactions.get(0).getId());
////
////            assert xa3 != base;
////            assert xa3.getId().equals(base.getId());
////
////            Set<XB> xbsREstored = base.getXbs();
////            assert xbsREstored != xbs;
////
////            assert xbsREstored.size() == 2;
////
////            for(XB xb: xbsREstored)
////            {
////                assert xb != xbone;
////                assert xb != xbtwo;
////
////                if (xbone.getId().equals(xb.getId()))
////                {
////                    assert "xbone".equals(xb.getName());
////                }
////                else if (xbtwo.getId().equals(xb.getId()))
////                {
////                    assert "xbtwo".equals(xb.getName());
////                }
////                else
////                {
////                    throw new Error("unexpected " + xb);
////                }
////            }
////        }
////        catch(Exception e)
////        {
////            log.error("test failed unexpectedly", e);
////            throw e;
////        }
////        finally
////        {
////            HibernateAudit.stopRuntime();
////
////            if (sf != null)
////            {
////                sf.close();
////            }
////        }
////    }
//
//    @Test(enabled = true)
//    public void testManyToOne_OneIsTuplizer_EmptyCollection() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xa2Mapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA2' table='XA2'>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <set name='xbs' cascade='all'>\n" +
//            "            <key column='xa_id'/>\n" +
//            "            <one-to-many entity-name='XB'/>\n" +
//            "        </set>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xbMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xa2Mapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
//
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
//            XA2 xa2 = new XA2();
//
//            // empty collection
//
//            s.save(xa2);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa2.getId());
//            assert transactions.size() == 1;
//
//            XA2 base = new XA2();
//            HibernateAudit.delta(base, xa2.getId(), transactions.get(0).getId());
//
//            Set<XB> restored = base.getXbs();
//            assert restored.isEmpty();
//
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
