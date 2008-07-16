package com.googlecode.hibernate.audit.test.different_factory;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import com.googlecode.hibernate.audit.HibernateAudit;

/**
 * Tests "different session factory for audit" functionality.
 * See https://jira.novaordis.org/browse/HBA-11.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class DifferentSessionFactoryTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DifferentSessionFactoryTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // disable when working on https://jira.novaordis.org/browse/HBA-11 
    @Test(enabled = false)
    public void testOne() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure("hibernate.cfg.xml");
        config.addAnnotatedClass(A.class);

        SessionFactory sessionFactory = config.buildSessionFactory();

        HibernateAudit.enable(sessionFactory);

        Session session = sessionFactory.openSession();
        Transaction tx = session.beginTransaction();

        A a = new A();
        a.setName("alice");
        session.save(a);

        tx.commit();

        sessionFactory.close();

        log.info("done");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
