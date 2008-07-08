package com.googlecode.hibernate.audit.test.jta;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

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
                log.debug(e.getMessage());
            }

            List rs = HibernateAudit.query("from AuditTransaction");
            assert rs.size() == 0;
            rs = HibernateAudit.query("from AuditEvent");
            assert rs.size() == 0;
            rs = HibernateAudit.query("from AuditType");
            assert rs.size() == 0;
            rs = HibernateAudit.query("from AuditPair");
            assert rs.size() == 0;

            HibernateAudit.disable();
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
