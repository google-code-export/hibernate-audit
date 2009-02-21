package com.googlecode.hibernate.audit.test.jta;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.mock.jca.MockJTAAwareDataSource;
import com.googlecode.hibernate.audit.HibernateAudit;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import java.util.List;

/**
 * A test that simulates a failed commit.
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
public class FailedCommitTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(FailedCommitTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testFailedCommit() throws Exception
    {
        InitialContext ic = new InitialContext();

        MockJTAAwareDataSource ds = (MockJTAAwareDataSource)ic.lookup(getDataSourceJNDIName());
        ds.setBroken(true); // this will make connections fail on commit

        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            UserTransaction ut = (UserTransaction)ic.lookup(getUserTransactionJNDIName());

            ut.begin();

            Session s = sf.getCurrentSession();

            A a = new A();
            a.setName("alice");
            s.save(a);

            try
            {
                ut.commit();
                throw new Error("should've failed");
            }
            catch(Exception e)
            {
                // ok, we're expecting this
                log.debug(">>>>> " + e.getMessage());
            }

            // restore mock data source's sanity so we can perform queries
            ds.setBroken(false);

            List rs = HibernateAudit.query("from AuditTransaction");
            assert rs.size() == 0;
            rs = HibernateAudit.query("from AuditEvent");
            assert rs.size() == 0;
            rs = HibernateAudit.query("from AuditType");
            assert rs.size() == 0;
            rs = HibernateAudit.query("from AuditEventPair");
            assert rs.size() == 0;
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
        }
        finally
        {
            // restore mock data source's sanity so further tests won't fail
            ds.setBroken(false);

            HibernateAudit.stopRuntime();

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
     public void testSuccessfulCommitFollowUp() throws Exception
     {
         InitialContext ic = new InitialContext();

         AnnotationConfiguration config = new AnnotationConfiguration();
         config.configure(getHibernateConfigurationFileName());
         config.addAnnotatedClass(A.class);
         SessionFactoryImplementor sf = null;

         try
         {
             sf = (SessionFactoryImplementor)config.buildSessionFactory();

             HibernateAudit.startRuntime(sf.getSettings());
             HibernateAudit.register(sf);

             UserTransaction ut = (UserTransaction)ic.lookup(getUserTransactionJNDIName());

             ut.begin();

             Session s = sf.getCurrentSession();

             A a = new A();
             a.setName("alice");
             s.save(a);

             ut.commit();

             List rs = HibernateAudit.query("from AuditTransaction");
             assert rs.size() == 1;
             rs = HibernateAudit.query("from AuditEvent");
             assert rs.size() == 1;
         }
         finally
         {
             HibernateAudit.stopRuntime();

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