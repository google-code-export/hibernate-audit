package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditPair;

import java.util.List;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

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
public class PostInsertTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testSingleInsert() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Date t1 = new Date();

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(a);

            t.commit();

            Date t2 = new Date();

            // make sure information was logged. Since we wipe out tables for each test, only one
            // audit transaction is expected to be found

            List ts = HibernateAudit.query("from AuditTransaction");

            assert ts.size() == 1;

            AuditTransaction at = (AuditTransaction)ts.get(0);

            assert at.getTimestamp().getTime() >= floorTime(t1.getTime());
            assert at.getTimestamp().getTime() <= t2.getTime();

            List es = HibernateAudit.query("from AuditEvent");

            assert ts.size() == 1;

            AuditEvent ae = (AuditEvent)es.get(0);

            assert AuditEventType.INSERT.equals(ae.getType());
            assert ts.remove(ae.getTransaction());
            assert A.class.getName().equals(ae.getEntityClassName());
            assert a.getId().equals(ae.getEntityId());

            List nvps = HibernateAudit.query("from AuditPair as ap where ap.event = :event", ae);

            assert nvps.size() == 1;

            AuditPair nvp = (AuditPair)nvps.get(0);

            assert "name".equals(nvp.getName());
            assert "alice".equals(nvp.getValue());

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

    @Test(enabled = true)
    public void testSuccesiveInserts() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Set<Long> expectedEntityIds = new HashSet<Long>();
            Set<String> expectedPairValues = new HashSet<String>();

            Date t1 = new Date();

            A a = new A();
            a.setName("alice");
            expectedPairValues.add(a.getName());

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(a);

            t.commit();

            expectedEntityIds.add(a.getId());

            Date t2 = new Date();

            a = new A();
            a.setName("alex");
            expectedPairValues.add(a.getName());

            s = sf.openSession();
            t = s.beginTransaction();

            s.save(a);

            t.commit();

            expectedEntityIds.add(a.getId());

            Date t3 = new Date();

            List ts = HibernateAudit.query("from AuditTransaction as a order by a.timestamp asc");

            assert ts.size() == 2;

            AuditTransaction at = (AuditTransaction)ts.get(0);
            assert at.getTimestamp().getTime() >= floorTime(t1.getTime());
            assert at.getTimestamp().getTime() <= t2.getTime();

            at = (AuditTransaction)ts.get(1);
            assert at.getTimestamp().getTime() >= floorTime(t2.getTime());
            assert at.getTimestamp().getTime() <= t3.getTime();

            List es = HibernateAudit.query("from AuditEvent");

            assert ts.size() == 2;

            AuditEvent ae = (AuditEvent)es.get(0);
            assert AuditEventType.INSERT.equals(ae.getType());
            assert ts.remove(ae.getTransaction());
            assert A.class.getName().equals(ae.getEntityClassName());
            assert expectedEntityIds.remove(ae.getEntityId());

            List nvps = HibernateAudit.query("from AuditPair as ap where ap.event = :event", ae);

            assert nvps.size() == 1;

            AuditPair nvp = (AuditPair)nvps.get(0);
            assert "name".equals(nvp.getName());
            assert expectedPairValues.remove((String)nvp.getValue());

            ae = (AuditEvent)es.get(1);
            assert AuditEventType.INSERT.equals(ae.getType());
            assert ts.remove(ae.getTransaction());
            assert A.class.getName().equals(ae.getEntityClassName());
            assert expectedEntityIds.remove(ae.getEntityId());

            nvps = HibernateAudit.query("from AuditPair as ap where ap.event = :event", ae);

            assert nvps.size() == 1;

            nvp = (AuditPair)nvps.get(0);
            assert "name".equals(nvp.getName());
            assert expectedPairValues.remove((String)nvp.getValue());

            HibernateAudit.disable();
        }
        finally
        {
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
