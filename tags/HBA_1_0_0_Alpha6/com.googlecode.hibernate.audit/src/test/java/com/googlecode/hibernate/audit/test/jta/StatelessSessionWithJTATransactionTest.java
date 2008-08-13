package com.googlecode.hibernate.audit.test.jta;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.HibernateAudit;

import javax.naming.InitialContext;
import javax.transaction.Status;
import javax.transaction.UserTransaction;
import java.util.List;

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
public class StatelessSessionWithJTATransactionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(StatelessSessionWithJTATransactionTest.class);

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
        InitialContext ic = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            // start a JTA transaction
            ic = new InitialContext();
            String utJNDIName = getUserTransactionJNDIName();
            UserTransaction ut = (UserTransaction)ic.lookup(utJNDIName);
            ut.begin();

            A a = new A();
            a.setName("alice");

            Session s = sf.getCurrentSession();

            assert Status.STATUS_ACTIVE == ut.getStatus();

            s.save(a);

            ut.commit();

            List ts = HibernateAudit.query("from AuditTransaction");
            assert ts.size() == 1;

            HibernateAudit.disableAll();
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

            if (ic != null)
            {
                ic.close();
            }
        }
    }

    @Test(enabled = true)
    public void testTwoInsertsTwoTransactions() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;
        InitialContext ic = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            // start JTA transaction 1
            ic = new InitialContext();
            String utJNDIName = getUserTransactionJNDIName();
            UserTransaction ut = (UserTransaction)ic.lookup(utJNDIName);
            ut.begin();

            Session s = sf.getCurrentSession();

            A a = new A();
            a.setName("alice");
            log.debug("saving " + a);
            s.save(a);

            a = new A();
            a.setName("alex");
            log.debug("saving " + a);
            s.save(a);

            log.debug("commit");

            ut.commit();

            log.debug("commit successful");

            // start JTA transaction 2
            ut.begin();

            s = sf.getCurrentSession();

            a = new A();
            a.setName("albert");
            s.save(a);

            a = new A();
            a.setName("ana");
            s.save(a);

            log.debug("commit 2");

            ut.commit();

            log.debug("commit 2 successful");

            List ts = HibernateAudit.query("from AuditTransaction");
            assert ts.size() == 2;

            ts = HibernateAudit.query("from AuditEventPair");
            assert ts.size() == 4;

            HibernateAudit.disableAll();
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

            if (ic != null)
            {
                ic.close();
            }
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
