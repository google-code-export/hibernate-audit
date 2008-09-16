package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionFactoryImplementor;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.WA;
import com.googlecode.hibernate.audit.test.post_insert.data.WB;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

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
public class PostInsertCollectionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertCollectionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testPostInsert_EmptyCollection() throws Exception
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

            List list = HibernateAudit.query("from AuditEvent as e where e.transaction = ?", tx);
            assert list.size() == 2;

            AuditEvent ae = (AuditEvent)list.get(0);
            assert (ChangeType.INSERT.equals(ae.getType()));
            assert wa.getId().equals(ae.getTargetId());

            List pairs = HibernateAudit.query("from AuditEventPair as p where p.event = ?", ae);
            assert pairs.isEmpty();

            ae = (AuditEvent)list.get(1);
            assert (ChangeType.UPDATE.equals(ae.getType()));
            assert wa.getId().equals(ae.getTargetId());

            pairs = HibernateAudit.query("from AuditEventPair as p where p.event = ?", ae);
            assert pairs.size() == 1;

            AuditEventCollectionPair aecp = (AuditEventCollectionPair)pairs.get(0);
            assert aecp.getIds().isEmpty();
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
    public void testPostInsert_EmptyCollection2() throws Exception
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
            wa.setWbs(new ArrayList<WB>());

            s.save(wa);
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            List list = HibernateAudit.query("from AuditEvent as e where e.transaction = ?", tx);
            assert list.size() == 2;

            AuditEvent ae = (AuditEvent)list.get(0);
            assert (ChangeType.INSERT.equals(ae.getType()));
            assert wa.getId().equals(ae.getTargetId());

            List pairs = HibernateAudit.query("from AuditEventPair as p where p.event = ?", ae);
            assert pairs.isEmpty();

            ae = (AuditEvent)list.get(1);
            assert (ChangeType.UPDATE.equals(ae.getType()));
            assert wa.getId().equals(ae.getTargetId());

            pairs = HibernateAudit.query("from AuditEventPair as p where p.event = ?", ae);
            assert pairs.size() == 1;

            AuditEventCollectionPair aecp = (AuditEventCollectionPair)pairs.get(0);
            assert aecp.getIds().isEmpty();
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
            Transaction t = s.beginTransaction();

            WA wa = new WA();
            wa.setName("wasabi");

            WB wb = new WB();
            wb.setName("wbang");

            wa.getWbs().add(wb);
            wb.setWa(wa);

            s.save(wa);
            t.commit();

            // verify the data is in the database
            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;

            AuditTransaction at = (AuditTransaction)transactions.get(0);

            List types = HibernateAudit.query("from AuditType");
            assert types.size() == 4;

            Set<AuditType> expectedTargetTypes = new HashSet<AuditType>();

            for(Object o: types)
            {
                AuditType type = (AuditType)o;

                if (type.isEntityType())
                {
                    expectedTargetTypes.add(type);
                }
                else if (type.isCollectionType())
                {
                    AuditCollectionType ct = (AuditCollectionType)type;
                    assert List.class.equals(ct.getCollectionClassInstance());
                    assert WB.class.equals(ct.getClassInstance());
                }
                else if (String.class.equals(type.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type " + type);
                }
            }

            assert expectedTargetTypes.size() == 2;

            List events = HibernateAudit.query("from AuditEvent");
            assert events.size() == 3; // two INSERTS, one UPDATE

            for(Object o: events)
            {
                AuditEvent e = (AuditEvent)o;
                assert at.equals(e.getTransaction());
                assert expectedTargetTypes.contains(e.getTargetType());

                List pairs = HibernateAudit.query("from AuditEventPair as p where p.event = ?", e);
                Long id = e.getTargetId();
                if (wa.getId().equals(id))
                {
                    if (ChangeType.INSERT.equals(e.getType()))
                    {
                        assert pairs.size() == 2;

                        for(Object o2: pairs)
                        {
                            if (o2 instanceof AuditEventCollectionPair)
                            {
                                AuditEventCollectionPair cp = (AuditEventCollectionPair)o2;
                                List<Long> ids = cp.getIds();
                                assert ids.size() == 1;
                                assert ids.contains(wb.getId());
                            }
                            else
                            {
                                AuditEventPair p = (AuditEventPair)o2;
                                assert "wasabi".equals(p.getValue());
                            }
                        }

                    }
                    else if (ChangeType.UPDATE.equals(e.getType()))
                    {
                        assert pairs.size() == 1;
                        AuditEventCollectionPair cp = (AuditEventCollectionPair)pairs.get(0);
                        List<Long> ids = cp.getIds();
                        assert ids.size() == 1;
                        assert ids.contains(wb.getId());
                    }
                    else
                    {
                        throw new Error("unexpected type " + e.getType());
                    }
                }
                else if (wb.getId().equals(id))
                {
                    assert ChangeType.INSERT.equals(e.getType());
                    assert pairs.size() == 2;

                    for(Object o2: pairs)
                    {
                        AuditEventPair p = (AuditEventPair)o2;
                        assert "wbang".equals(p.getValue()) || wa.getId().equals(p.getValue());
                    }
                }
                else
                {
                    throw new Error("unexpected id " + id);
                }
            }
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

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-107
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

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-107
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

    @Test(enabled = true)
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
            Transaction t = s.beginTransaction();

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
            t.commit();

            Long waId = wa.getId();
            Long wbId = wb.getId();
            Long wb2Id = wb2.getId();
            Set<Long> expectedWbIds = new HashSet<Long>();
            expectedWbIds.add(wbId);
            expectedWbIds.add(wb2Id);

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            WA baseA = new WA();
            HibernateAudit.delta(baseA, waId, at.getId());

            assert waId.equals(baseA.getId());
            assert "wasabi".equals(wa.getName());

            List<WB> wbs = baseA.getWbs();
            assert !wa.getWbs().equals(wbs);

            assert wbs.size() == 2;

            for(WB b: wbs)
            {
                assert b != wb;
                assert b != wb2;
                assert expectedWbIds.remove(b.getId());
                assert baseA == b.getWa();
                if (wb.getId().equals(b.getId()))
                {
                    assert wb.getName().equals(b.getName());
                }
                else if (wb2.getId().equals(b.getId()))
                {
                    assert wb2.getName().equals(b.getName());
                }
                else
                {
                    throw new Error("did not expect " + b.getId());
                }
            }

            assert expectedWbIds.isEmpty();
            
            HibernateAudit.stopRuntime();
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
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
            Transaction t = s.beginTransaction();

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
            t.commit();

            Long waId = wa.getId();
            Long wbId = wb.getId();
            Long wb2Id = wb2.getId();
            Set<Long> expectedWbIds = new HashSet<Long>();
            expectedWbIds.add(wbId);
            expectedWbIds.add(wb2Id);

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            WA baseA = new WA();
            HibernateAudit.delta(baseA, waId, at.getId());

            assert waId.equals(baseA.getId());
            assert "wasabi".equals(wa.getName());

            List<WB> wbs = baseA.getWbs();
            assert !wa.getWbs().equals(wbs);

            assert wbs.size() == 2;

            for(WB b: wbs)
            {
                assert b != wb;
                assert b != wb2;
                assert expectedWbIds.remove(b.getId());
                assert b.getWa() == null;
                if (wb.getId().equals(b.getId()))
                {
                    assert wb.getName().equals(b.getName());
                }
                else if (wb2.getId().equals(b.getId()))
                {
                    assert wb2.getName().equals(b.getName());
                }
                else
                {
                    throw new Error("did not expect " + b.getId());
                }
            }

            assert expectedWbIds.isEmpty();
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

            List<String> expected = new ArrayList<String>(Arrays.asList("wbang", "wbong"));
            List<WB> wbs = wa.getWbs();
            assert wbs.size() == 2;
            for(WB i: wbs)
            {
                assert expected.remove(i.getName());
            }

            // enable auditing
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            WB newWb = new WB();
            newWb.setName("wbung");
            wa.getWbs().set(0, newWb);

            s.getTransaction().commit();
            s.close();

            // "raw" access test

            List<AuditTransaction> txs = HibernateAudit.getTransactions(wa.getId());
            assert txs.size() == 1;

            AuditTransaction tx = txs.get(0);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 2;

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;
                assert tx.equals(ae.getTransaction());
                ChangeType ct = ae.getType();

                List pairs =
                    HibernateAudit.query("from AuditEventPair as p where p.event = :event", ae);

                if (ChangeType.INSERT.equals(ct))
                {
                    assert newWb.getId().equals(ae.getTargetId());
                    assert pairs.size() == 1;
                    AuditEventPair p = (AuditEventPair)pairs.get(0);
                    assert "name".equals(p.getField().getName());
                    assert "wbung".equals(p.getValue());
                }
                else if (ChangeType.UPDATE.equals(ct))
                {
                    assert wa.getId().equals(ae.getTargetId());
                    assert pairs.size() == 1;
                    AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);
                    assert "wbs".equals(p.getField().getName());
                    List ids = (List)p.getValue();
                    assert ids.size() == 2;
                    assert newWb.getId().equals(ids.get(0));
                    assert wb2.getId().equals(ids.get(1));
                }
                else
                {
                    throw new Error("invalid state");
                }
            }
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

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(wa.getId());

            assert transactions.size() == 1;

            Long txId = transactions.get(0).getId();

            WA base = new WA();
            HibernateAudit.delta(base, wa.getId(), txId);

            assert wa.getId().equals(base.getId());
            assert base.getName() == null;

            List<WB> wbs = wa.getWbs();
            assert wbs.size() == 1;

            WB wbCopy = wbs.get(0);
            assert wbCopy.getId().equals(wb.getId());
            assert wbCopy.getName() == null;
            assert wbCopy.getWa() == null;
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

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
