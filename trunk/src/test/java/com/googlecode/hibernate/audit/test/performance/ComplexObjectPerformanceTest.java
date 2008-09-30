package com.googlecode.hibernate.audit.test.performance;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.performance.data.Root;
import com.googlecode.hibernate.audit.test.performance.data.LevelOne;
import com.googlecode.hibernate.audit.test.performance.data.LevelTwo;
import com.googlecode.hibernate.audit.HibernateAudit;

import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 *@author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class ComplexObjectPerformanceTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ComplexObjectPerformanceTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private List<Result> results;

    // Constructors --------------------------------------------------------------------------------

    public ComplexObjectPerformanceTest()
    {
        results = new ArrayList<Result>();
    }

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testComplex() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(Root.class);
        config.addAnnotatedClass(LevelOne.class);
        config.addAnnotatedClass(LevelTwo.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();
            Root root = Root.random(10, 1, 1);
            Date t1 = new Date();
            s.save(root);
            s.getTransaction().commit();
            Date t2 = new Date();

            s.close();

            results.add(new Result("intial", t2.getTime() - t1.getTime()));

//            for(int i = 0; i < 2; i++)
//            {
//                s = sf.openSession();
//                s.beginTransaction();
//                root = Root.random(10, 1, 1);
//                t1 = new Date();
//                s.save(root);
//                s.getTransaction().commit();
//                s.close();
//                t2 = new Date();
//
//                results.add(new Result("save #" + i, t2.getTime() - t1.getTime()));
//            }
        }
        finally
        {
            displayResults();

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

    private void displayResults()
    {
        log.info("---------------------------------------------------------------");

        for(Result r: results)
        {
            log.info(r.comment + "\t\t" + r.ms + " ms");
        }

        log.info("---------------------------------------------------------------");
    }

    // Inner classes -------------------------------------------------------------------------------

    private class Result
    {
        String comment;
        long ms;

        Result(String comment, long ms)
        {
            this.comment = comment;
            this.ms = ms;

        }
    }
}
