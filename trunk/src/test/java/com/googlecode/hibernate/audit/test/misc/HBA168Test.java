package com.googlecode.hibernate.audit.test.misc;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.misc.data.B;
import com.googlecode.hibernate.audit.test.misc.data.C;
import com.googlecode.hibernate.audit.HibernateAudit;

/**
 * Test for https://jira.novaordis.org/browse/HBA-168.
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
public class HBA168Test extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void test() throws Exception
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

            Session s = sf.openSession();
            s.beginTransaction();

            B b = new B();
            C c = new C();
            b.getCs().add(c);
            s.save(b);

            s.getTransaction().commit();
            s.close();

            s  = sf.openSession();
            s.beginTransaction();

            s.update(b);

            s.getTransaction().commit();
            s.close();
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
