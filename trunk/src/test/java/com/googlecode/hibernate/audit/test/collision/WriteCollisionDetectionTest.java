package com.googlecode.hibernate.audit.test.collision;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.collision.data.C;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.HibernateAuditException;
import com.googlecode.hibernate.audit.collision.WriteCollisionDetector;
import com.googlecode.hibernate.audit.collision.WriteCollisionException;

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
public class WriteCollisionDetectionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteCollisionDetectionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testCollisionDetection_NoReferenceVersionInThreadLocal() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            WriteCollisionDetector wcd = HibernateAudit.getManager().getWriteCollisionDetector();

            assert !wcd.isWriteCollisionDetectionEnabled();

            wcd.setWriteCollisionDetectionEnabled(true);
            assert wcd.isWriteCollisionDetectionEnabled();

            Session s = sf.openSession();
            s.beginTransaction();
            C c = new C();
            s.save(c);
            s.getTransaction().commit();
            s.close();

            s = sf.openSession();
            s.beginTransaction();
            C da = (C)s.get(C.class, c.getId());
            da.setI(1);
            s.update(da);
            try
            {
                s.getTransaction().commit();
                throw new Error("should fail");
            }
            catch(HibernateAuditException e)
            {
                Throwable t = e.getCause();
                assert t instanceof IllegalStateException;
                log.debug(">>> " + t.getMessage());

                // transaction already rolled back by the audit listener
            }
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
    public void testCollisionDetection_NoCollision() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            WriteCollisionDetector wcd = HibernateAudit.getManager().getWriteCollisionDetector();

            assert !wcd.isWriteCollisionDetectionEnabled();

            wcd.setWriteCollisionDetectionEnabled(true);
            assert wcd.isWriteCollisionDetectionEnabled();

            Session s = sf.openSession();
            s.beginTransaction();
            C c = new C();
            s.save(c);
            s.getTransaction().commit();
            s.close();

            // no other transaction is concurrently changing A, so we're safe to do this:

            Long refVersion = HibernateAudit.
                getLatestTransaction(C.class.getName(), c.getId()).getId();

            WriteCollisionDetector.setReferenceVersion(refVersion);

            s = sf.openSession();
            s.beginTransaction();
            C da = (C)s.get(C.class, c.getId());
            da.setI(1);
            s.update(da);
            s.getTransaction().commit();
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
    public void testCollisionDetection_Collision() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            WriteCollisionDetector wcd = HibernateAudit.getManager().getWriteCollisionDetector();

            assert !wcd.isWriteCollisionDetectionEnabled();

            wcd.setWriteCollisionDetectionEnabled(true);
            assert wcd.isWriteCollisionDetectionEnabled();

            Session s = sf.openSession();
            s.beginTransaction();
            C c = new C();
            s.save(c);
            s.getTransaction().commit();

            // no other transaction is concurrently changing A, so we're safe to do this:

            Long refVersion = HibernateAudit.
                getLatestTransaction(C.class.getName(), c.getId()).getId();

            WriteCollisionDetector.setReferenceVersion(refVersion);

            // we simulate a concurrent transaction, collision detection won't kick in

            s.beginTransaction();
            c.setI(2);
            s.update(c);
            s.getTransaction().commit();

            s.close();

            // we keep the same reference version in the threadlocal

            s = sf.openSession();
            s.beginTransaction();
            C da = (C)s.get(C.class, c.getId());
            da.setI(3);
            s.update(da);

            try
            {
                s.getTransaction().commit();
            }
            catch(HibernateAuditException e)
            {
                Throwable cause = e.getCause();
                assert cause instanceof WriteCollisionException;
                log.debug(">>> " + cause.getMessage());

                // transaction is rolled back by the audit listener
            }
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
