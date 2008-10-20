package com.googlecode.hibernate.audit.test.util.wocache;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.wocache.data.A;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;

/**
 * Tests zeroing in on the cause of https://jira.novaordis.org/browse/HBA-138 failures.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class WriteOnceCacheAndTransactionsTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteOnceCacheAndTransactionsTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------


//    /**
//     * See https://jira.novaordis.org/browse/HBA-138
//     */
//    TODO Uncomment after fixing https://jira.novaordis.org/browse/HBA-138
//    @Test(enabled = true)
//    public void testSynchronizationConcurrentModificationException_UserTransaction()
//        throws Exception
//    {
//        log.debug("testSynchronizationConcurrentModificationException");
//
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        InitialContext ic = new InitialContext();
//        SessionFactoryImplementor sf = null;
//        UserTransaction ut = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            ut = (UserTransaction)ic.lookup(getUserTransactionJNDIName());
//
//            ut.begin();
//
//            Session s = sf.getCurrentSession();
//
//            A a = new A();
//            a.setS("alice");
//            s.save(a);
//
//            ut.commit();
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions();
//            assert txs.size() == 1;
//        }
//        finally
//        {
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//
//            if (ic != null)
//            {
//                ic.close();
//            }
//        }
//    }

    /**
     * See https://jira.novaordis.org/browse/HBA-138
     */
    @Test(enabled = true)
    public void testSynchronizationConcurrentModificationException_HibernateTransaction()
        throws Exception
    {

        log.debug("testSynchronizationConcurrentModificationException_HibernateTransaction");

        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            a.setS("alice");
            s.save(a);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
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
