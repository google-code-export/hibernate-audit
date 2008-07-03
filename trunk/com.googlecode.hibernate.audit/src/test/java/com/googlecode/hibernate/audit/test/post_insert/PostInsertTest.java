package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.Date;

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
public class PostInsertTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testSingleInsert() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure("/hibernate-thread.cfg.xml");
        config.addAnnotatedClass(A.class);
        SessionFactory sf = config.buildSessionFactory();
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
        assert at.getTimestamp().getTime() >= t1.getTime();
        assert at.getTimestamp().getTime() <= t2.getTime();

        HibernateAudit.disable();
        sf.close();
    }

    @Test(enabled = true)
    public void testSuccesiveInserts() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure("/hibernate-thread.cfg.xml");
        config.addAnnotatedClass(A.class);
        SessionFactory sf = config.buildSessionFactory();
        HibernateAudit.enable(sf);

        Date t1 = new Date();

        A a = new A();
        a.setName("alice");

        Session s = sf.openSession();
        Transaction t = s.beginTransaction();

        s.save(a);

        t.commit();

        Date t2 = new Date();

        a = new A();
        a.setName("alex");

        s = sf.openSession();
        t = s.beginTransaction();

        s.save(a);

        t.commit();

        Date t3 = new Date();

        List ts = HibernateAudit.query("from AuditTransaction as a order by a.timestamp asc");

        assert ts.size() == 2;

        AuditTransaction at = (AuditTransaction)ts.get(0);
        assert at.getTimestamp().getTime() >= t1.getTime();
        assert at.getTimestamp().getTime() <= t2.getTime();

        at = (AuditTransaction)ts.get(1);
        assert at.getTimestamp().getTime() >= t2.getTime();
        assert at.getTimestamp().getTime() <= t3.getTime();

        HibernateAudit.disable();
        sf.close();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
