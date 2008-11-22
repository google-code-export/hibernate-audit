package com.googlecode.hibernate.audit.test.logical_group_id;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.HibernateAuditTest;
import com.googlecode.hibernate.audit.test.logical_group_id.data.C;
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
public class ThreadBoundLogicalGroupIdTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAuditTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

//    @Test(enabled = true) TODO this test is not relevant anymore, as we don't perform a logical group id consistency check
//    public void testHibernateFailure_ProgrammerForgetsToRolldBackTransaction_WithAudit()
//        throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(C.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            // enter a database record that will cause a failure down the road. In this case, it's
//            // an unique constraint violation, but it can be anything, including programming
//            // errors downstream
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//            C c = new C();
//            c.setS("will_trigger_unique_constraint_violation");
//            s.save(c);
//            s.getTransaction().commit();
//            s.close();
//
//            // everything fine so far ...
//
//            // start Audit, which will catch the problem
//
//            ThreadBoundLogicalGroupIdProvider lgip = new ThreadBoundLogicalGroupIdProvider();
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf, lgip);
//
//            // simulate a deal context
//            lgip.bindLogicalGroupId(new Long(7777));
//
//            // attempt a benign persistence operation, that will fail due to the unique constraint
//            // violation
//
//            s = sf.openSession();
//            Transaction tx1 = s.beginTransaction();
//            c = new C();
//            c.setS("will_trigger_unique_constraint_violation");
//            s.save(c);
//
//            try
//            {
//                tx1.commit();
//                throw new Error("this should have failed with constraint violation");
//            }
//            catch(Exception e)
//            {
//                log.debug("yes, constraint violation is what expected: " + e.getMessage());
//            }
//
//            // here is the trouble  starts, we "forget" to roll back the transaction, so we leave
//            // a problem lurking under the covers ...
//
//            // ... because we reuse the thread for a different transaction, from a completely
//            // different deal context
//
//            lgip.bindLogicalGroupId(new Long(8888));
//
//            s = sf.openSession();
//            Transaction tx2 = s.beginTransaction();
//
//            c = new C();
//            c.setS("benign value");
//            s.save(c);
//
//            // and now the problems becomes obvious, it's caught by Audit:
//
//            try
//            {
//                tx2.commit();
//                throw new Error("this should have failed with IllegalStateException");
//            }
//            catch(HibernateAuditException e)
//            {
//                // yes, exactly what we were expecting ...
//
//                IllegalStateException ise = (IllegalStateException)e.getCause();
//                String message = ise.getMessage();
//                assert message.indexOf("inconsistent logical groups") != -1;
//            }
//
//            // QED!
//
//            // put the broken transaction to rest, so I can exit the test
//
//            tx1.rollback();
//
//            // tx2 is rolled back by Audit ...
//
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

    @Test(enabled = true)
    public void testHibernateFailure_ProgrammerForgetsToRolldBackTransaction_WithoutAudit()
        throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            // enter a database record that will cause a failure down the road. In this case, it's
            // an unique constraint violation, but it can be anything, including programming
            // errors downstream

            Session s = sf.openSession();
            s.beginTransaction();
            C c = new C();
            c.setS("will_trigger_unique_constraint_violation");
            s.save(c);
            s.getTransaction().commit();
            s.close();

            // failed first attemtpt

            s = sf.openSession();
            Transaction tx1 = s.beginTransaction();
            c = new C();
            c.setS("will_trigger_unique_constraint_violation");
            s.save(c);

            try
            {
                tx1.commit();
                throw new Error("this should have failed with constraint violation");
            }
            catch(Exception e)
            {
                log.debug("yes, constraint violation is what expected: " + e.getMessage());
            }

            // ... "forgetting" to roll back ...

            s = sf.openSession();
            Transaction tx2 = s.beginTransaction();

            c = new C();
            c.setS("benign value");
            s.save(c);

            // and the second commit succeeds ...
            tx2.commit();

            // ... indeed we find 'benign value' in the database:

            s = sf.openSession();
            s.beginTransaction();
            C dc = (C)s.get(C.class,  c.getId());
            assert "benign value".equals(dc.getS());
            s.getTransaction().commit();

            // ... while we're stuck with a failed transaction and a jammed connection under the
            //  covers ...


            // put the broken transaction to rest, so I can exit the test
            tx1.rollback();
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