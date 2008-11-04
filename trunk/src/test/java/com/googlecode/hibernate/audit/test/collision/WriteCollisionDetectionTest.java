package com.googlecode.hibernate.audit.test.collision;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.collision.data.A;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.HibernateAuditException;
import com.googlecode.hibernate.audit.collision.WriteCollisionDetector;

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
    public void testCollisionDetection_NoReferenceVersion() throws Exception
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

            WriteCollisionDetector wcd = HibernateAudit.getManager().getWriteCollisionDetector();

            assert !wcd.isWriteCollisionDetectionEnabled();

            wcd.setWriteCollisionDetectionEnabled(true);
            assert wcd.isWriteCollisionDetectionEnabled();

            Session s = sf.openSession();
            s.beginTransaction();
            A a = new A();
            s.save(a);
            s.getTransaction().commit();
            s.close();

            s = sf.openSession();
            s.beginTransaction();
            A da = (A)s.get(A.class, a.getId());
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

//    @Test(enabled = true)
//    public void testCollisionDetection() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            WriteCollisionDetector wcd = HibernateAudit.getManager().getWriteCollisionDetector();
//
//            assert !wcd.isWriteCollisionDetectionEnabled();
//
//            wcd.setWriteCollisionDetectionEnabled(true);
//            assert wcd.isWriteCollisionDetectionEnabled();
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//            A a = new A();
//            s.save(a);
//            s.getTransaction().commit();
//            s.close();
//
//            WriteCollisionDetector.setReferenceVersion(new Long(7));
//
//            s = sf.openSession();
//            s.beginTransaction();
//            A da = (A)s.get(A.class, a.getId());
//            da.setI(1);
//            s.update(da);
//            s.getTransaction().commit();
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
