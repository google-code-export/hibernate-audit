package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.googlecode.hibernate.audit.HibernateAudit;

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
        config.configure("/hibernate.cfg.xml");
        config.addAnnotatedClass(A.class);
        SessionFactory sf = config.buildSessionFactory();
        HibernateAudit.enable(sf);

        A a = new A();
        a.setName("alice");

        Session s = sf.openSession();
        Transaction t = s.beginTransaction();

        s.save(a);

        t.commit();

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
