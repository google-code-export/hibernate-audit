package com.googlecode.hibernate.audit.test.misc;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.HibernateAuditTest;
import com.googlecode.hibernate.audit.test.misc.data.A;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditTransaction;

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
public class HibernateFailureTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAuditTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testHibernateFailure_CorrectlyRolledBackTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            // enter a placeholder that will trigger uniqueness constraint violation
            Session s = sf.openSession();
            s.beginTransaction();
            A a = new A();
            a.setS("alice");
            s.save(a);
            s.getTransaction().commit();
            s.close();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            s = sf.openSession();
            s.beginTransaction();

            a = new A();
            a.setS("alice");

            s.save(a);

            log.debug("committing ...");

            try
            {
                s.getTransaction().commit();
                throw new Error("this should have failed");
            }
            catch(Exception e)
            {
                log.debug(">>>> " + e.getMessage());
            }

            s.getTransaction().rollback();

            // make sure the thread is clean of AuditTransaction
            assert Manager.getCurrentAuditTransaction() == null;
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
    public void testHibernateFailure_FailureToRollBackTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            // enter a placeholder that will trigger uniqueness constraint violation
            Session s = sf.openSession();
            s.beginTransaction();
            A a = new A();
            a.setS("alice");
            s.save(a);
            s.getTransaction().commit();
            s.close();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            s = sf.openSession();
            s.beginTransaction();

            a = new A();
            a.setS("alice");

            s.save(a);

            log.debug("committing ...");

            try
            {
                s.getTransaction().commit();
                throw new Error("this should have failed");
            }
            catch(Exception e)
            {
                log.debug(">>>> " + e.getMessage());
            }

            // we don't roll back, which means we leave garbage hanging by the thread
            // THIS IS BAD, IT SHOULDN'T HAPPEN, I AM JUST MAKING A POINT!

            AuditTransaction at = Manager.getCurrentAuditTransaction();

            assert at != null; // bad, this is what will break programs!

            // finally, put it to rest

            s.getTransaction().rollback();

            // make sure the thread is clean of AuditTransaction
            assert Manager.getCurrentAuditTransaction() == null;
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