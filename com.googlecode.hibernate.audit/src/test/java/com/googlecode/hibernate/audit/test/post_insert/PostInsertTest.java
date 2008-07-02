package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
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

    private static final Logger log = Logger.getLogger(PostInsertTest.class);

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

        // make sure information was logged

        String qs = "from AuditTransaction as a where a.timestamp >= :t1 and a.timestamp <= :t2";
        List ts = HibernateAudit.query(qs, t1, t2);

        assert ts.size() == 1;

        AuditTransaction at = (AuditTransaction)ts.get(0);
        assert at.getTimestamp().getTime() >= t1.getTime();
        assert at.getTimestamp().getTime() <= t2.getTime();

        HibernateAudit.disable();
        sf.close();
    }

    @Test(enabled = false)
    public void testSuccesiveInserts() throws Exception
    {
        throw new Exception("NOT YET IMPLEMENTED");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
