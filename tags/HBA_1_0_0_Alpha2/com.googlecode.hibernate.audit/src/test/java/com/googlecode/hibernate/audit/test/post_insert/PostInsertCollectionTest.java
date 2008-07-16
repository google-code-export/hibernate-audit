package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditEventPair;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Collection;

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
                else if (String.class.equals(type.getClassInstance()))
                {
                    // ok
                }
                else if (Collection.class.equals(type.getClassInstance()))
                {
                    // temporary ok
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
                       "COLLECTION - NOT YET IMPLEMENTED",
                       Long.toString(wa.getId()))); // the WA's id as a foreign key in WB's table.

            for(Object o: pairs)
            {
                AuditEventPair pair = (AuditEventPair)o;

                // TODO we're not testing pair.getValue() because at this time is not implemented
                assert expectedStringValues.remove(pair.getStringValue());
            }

            HibernateAudit.disable();
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

    @Test(enabled = false)
    public void testAddTwoInCollection() throws Exception
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

//            wa.addWb(wb);
//            wa.addWb(wb2);

            s.save(wa);
            t.commit();

//            Set<Long> expectedTargetIds = new HashSet<Long>();
//            expectedTargetIds.add(c.getId());
//            expectedTargetIds.add(d.getId());
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            List types = HibernateAudit.query("from AuditType");
//            assert types.size() == 3;
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
//                assert AuditEventType.INSERT.equals(e.getType());
//                assert expectedTargetTypes.remove(e.getTargetType());
//                assert expectedTargetIds.remove(e.getTargetId());
//            }
//
//            List pairs = HibernateAudit.query("from AuditEventPair");
//            assert pairs.size() == 3;
//
//            Set<String> expectedStringValues = new HashSet<String>();
//            expectedStringValues.add(c.getName());
//            expectedStringValues.add(d.getName());
//            expectedStringValues.add(Long.toString(d.getId()));
//
//            for(Object o: pairs)
//            {
//                AuditEventPair pair = (AuditEventPair)o;
//
//                // TODO we're not testing pair.getValue() because at this time is not implemented
//                assert expectedStringValues.remove(pair.getStringValue());
//            }

            HibernateAudit.disable();
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

    @Test(enabled = false)
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
            assert !HibernateAudit.isEnabled();

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


//            if (true)
//            {
//                throw new RuntimeException("NOT FINISHED");
//            }

//            Set<Long> expectedTargetIds = new HashSet<Long>();
//            expectedTargetIds.add(c.getId());
//            expectedTargetIds.add(d.getId());
//
//            List transactions = HibernateAudit.query("from AuditTransaction");
//            assert transactions.size() == 1;
//
//            AuditTransaction at = (AuditTransaction)transactions.get(0);
//
//            List types = HibernateAudit.query("from AuditType");
//            assert types.size() == 3;
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
//                assert AuditEventType.INSERT.equals(e.getType());
//                assert expectedTargetTypes.remove(e.getTargetType());
//                assert expectedTargetIds.remove(e.getTargetId());
//            }
//
//            List pairs = HibernateAudit.query("from AuditEventPair");
//            assert pairs.size() == 3;
//
//            Set<String> expectedStringValues = new HashSet<String>();
//            expectedStringValues.add(c.getName());
//            expectedStringValues.add(d.getName());
//            expectedStringValues.add(Long.toString(d.getId()));
//
//            for(Object o: pairs)
//            {
//                AuditEventPair pair = (AuditEventPair)o;
//
//                // TODO we're not testing pair.getValue() because at this time is not implemented
//                assert expectedStringValues.remove(pair.getStringValue());
//            }

            //HibernateAudit.disable();
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

    @Test(enabled = false)
    public void testRemoveTwoFromCollection() throws Exception
    {
        throw new Exception("NOT YET IMPELEMENTED");
    }

    @Test(enabled = false)
    public void testModifyOneInCollection() throws Exception
    {
        // we'll see what exactly that means
        throw new Exception("NOT YET IMPELEMENTED");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
