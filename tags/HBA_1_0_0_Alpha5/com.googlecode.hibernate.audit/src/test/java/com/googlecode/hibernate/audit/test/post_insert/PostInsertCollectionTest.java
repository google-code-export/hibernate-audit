package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.WA;
import com.googlecode.hibernate.audit.test.post_insert.data.WB;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventType;
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
    public void testAddOneInCollection() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);
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

            List<Long> targetIds = new ArrayList<Long>(Arrays.asList(wa.getId(), wb.getId()));

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
            assert events.size() == 2;

            for(Object o: events)
            {
                AuditEvent e = (AuditEvent)o;

                assert at.equals(e.getTransaction());
                assert AuditEventType.INSERT.equals(e.getType());
                assert expectedTargetTypes.remove(e.getTargetType());
                assert targetIds.remove(e.getTargetId());
            }

            List pairs = HibernateAudit.query("from AuditEventPair");
            assert pairs.size() == 4;

            List<String> expectedStringValues = new ArrayList<String>(Arrays.
                asList(wa.getName(),
                       wb.getName(),
                       Long.toString(wa.getId()))); // the WA's id as a foreign key in WB's table.

            for(Object o: pairs)
            {
                AuditEventPair pair = (AuditEventPair)o;

                if (pair.isCollection())
                {
                    AuditEventCollectionPair cp = (AuditEventCollectionPair)pair;
                    List<Long> ids = cp.getIds();

                    assert 1 == ids.size();
                    assert ids.remove(wb.getId());
                }
                else
                {
                    // TODO we're not testing pair.getValue() because at this time is not implemented
                    assert expectedStringValues.remove(pair.getStringValue());
                }
            }

            assert expectedStringValues.isEmpty();

            HibernateAudit.disableAll();
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
    public void testAddOneInCollection_Delta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);
            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            WA wa = new WA();
            wa.setName("alana");

            WB wb = new WB();
            wb.setName("baja");

            wa.getWbs().add(wb);
            wb.setWa(wa);

            s.save(wa);
            t.commit();

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            Long waId = wa.getId();
            Long wbId = wb.getId();

            WA base = new WA();
            HibernateAudit.delta(base, waId, at.getId());

            assert waId.equals(base.getId());
            assert "alana".equals(wa.getName());

            List<WB> wbs = base.getWbs();
            assert !wa.getWbs().equals(wbs);

            assert wbs.size() == 1;

            WB postTWb = wbs.get(0);
            assert postTWb != wb;

            assert postTWb.getId().equals(wbId);
            assert "baja".equals(postTWb.getName());

            assert base == postTWb.getWa();

            HibernateAudit.disableAll();
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
    public void testAddOneInCollection_NoBidirectionality_Delta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);
            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            WA wa = new WA();
            wa.setName("alana");

            WB wb = new WB();
            wb.setName("baja");

            wa.getWbs().add(wb);

            s.save(wa);
            t.commit();

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            Long waId = wa.getId();
            Long wbId = wb.getId();

            WA base = new WA();
            HibernateAudit.delta(base, waId, at.getId());

            assert waId.equals(base.getId());
            assert "alana".equals(wa.getName());

            List<WB> wbs = base.getWbs();
            assert !wa.getWbs().equals(wbs);

            assert wbs.size() == 1;

            WB postTWb = wbs.get(0);
            assert postTWb != wb;

            assert postTWb.getId().equals(wbId);
            assert "baja".equals(postTWb.getName());

            // no bidirectionality
            assert postTWb.getWa() == null;

            HibernateAudit.disableAll();
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
    public void testAddTwoInCollection_Bidirectionality() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);
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
            
            HibernateAudit.disableAll();
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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);
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

            HibernateAudit.disableAll();
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

    @Test(enabled = false) // FUNCTIONALITY NOT AVAILABLE
    public void testModifyOneFromCollection() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            // create the state in database, without auditing
            assert !HibernateAudit.isEnabled(null);

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            WA wa = new WA();
            wa.setName("wasabi");

            WB wb = new WB();
            wb.setName("wbang");

            WB wb2 = new WB();
            wb2.setName("wbong");

//            wa.addWb(wb);
//            wa.addWb(wb2);

            s.save(wa);
            t.commit();
            s.close();

            // load the state from the database

            s = sf.openSession();
            t = s.beginTransaction();

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
            //HibernateAudit.enable(sf);

            WB newWb = new WB();
            newWb.setName("wbung");
            // replacing "wbang" with "wbung"
            wbs.set(0, newWb);

            t.commit();
            s.close();

            HibernateAudit.disableAll();
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

    @Test(enabled = false) // FUNCTIONALITY NOT AVAILABLE
    public void testRemoveTwoFromCollection() throws Exception
    {
    }

    @Test(enabled = false) // FUNCTIONALITY NOT AVAILABLE
    public void testModifyOneInCollection() throws Exception
    {
    }

    @Test(enabled = true)
    public void testInsert_ACollectionAndNothingElseButEmptyState() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();

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
