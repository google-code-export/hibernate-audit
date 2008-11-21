package com.googlecode.hibernate.audit.test.misc;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.Query;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.misc.data.B;
import com.googlecode.hibernate.audit.test.misc.data.C;
import com.googlecode.hibernate.audit.HibernateAudit;

import java.util.List;

/**
 * Test for https://jira.novaordis.org/browse/HBA-181.
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
public class HBA181Test extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = false)
    public void testGenericSubQuery_HibernateLevel() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        config.addAnnotatedClass(C.class);

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            Session s = sf.openSession();

            // the query doesn't make to much sense, it's the parser and parameter substitution
            // we're testing here
            String qs =
                "from com.googlecode.hibernate.audit.test.misc.data.B where " +
                "id in ( select id from com.googlecode.hibernate.audit.test.misc.data.C as c " +
                "where c.s = :s )";

            Query q = s.createQuery(qs);
            q.setParameter("s", "blah"); // should not throw exception

            List result = q.list(); // should not throw exception
            assert result.isEmpty();
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }


    @Test(enabled = true)
    public void testGenericSubQuery() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        config.addAnnotatedClass(C.class);

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // the query doesn't make to much sense, it's the parser and parameter substitution
            // we're testing here
            String qs =
                "from AuditTransaction as t where " +
                "t in ( select transaction from AuditEvent as e where e.id = :auditEventId )";
            

            List result = HibernateAudit.query(qs, new Long(888)); // shouldn't throw exception
            assert result.isEmpty();
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
