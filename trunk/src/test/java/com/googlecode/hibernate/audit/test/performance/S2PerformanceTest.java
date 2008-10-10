package com.googlecode.hibernate.audit.test.performance;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.performance.data.s2.D;
import com.googlecode.hibernate.audit.test.performance.data.s2.P;
import com.googlecode.hibernate.audit.test.performance.data.s2.MT;
import com.googlecode.hibernate.audit.test.performance.data.s2.MD;
import com.googlecode.hibernate.audit.test.performance.data.s2.CS;
import com.googlecode.hibernate.audit.test.performance.data.s2.DPR;
import com.googlecode.hibernate.audit.test.performance.data.s2.CRD;
import com.googlecode.hibernate.audit.test.performance.data.s2.MDL;
import com.googlecode.hibernate.audit.test.performance.data.s2.DP;
import com.googlecode.hibernate.audit.test.performance.data.s2.R;
import com.googlecode.hibernate.audit.test.performance.data.s2.RRepository;
import com.googlecode.hibernate.audit.test.performance.data.s2.Scenario;
import com.googlecode.hibernate.audit.test.performance.data.s2.TypicalScenario;
import com.googlecode.hibernate.audit.HibernateAudit;

import java.util.Date;

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
public class S2PerformanceTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(S2PerformanceTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public S2PerformanceTest()
    {
    }

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void test() throws Exception
    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(D.class);
//        config.addAnnotatedClass(P.class);
//        config.addAnnotatedClass(CRD.class);
//        config.addAnnotatedClass(CS.class);
//        config.addAnnotatedClass(DP.class);
//        config.addAnnotatedClass(DPR.class);
//        config.addAnnotatedClass(MD.class);
//        config.addAnnotatedClass(MDL.class);
//        config.addAnnotatedClass(MT.class);
//        config.addAnnotatedClass(R.class);
//
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            RRepository rRepository = new RRepository(10);
//            rRepository.populate(sf, true);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
////            HibernateAudit.startRuntime(sf.getSettings());
////            HibernateAudit.register(sf);
//
//            Scenario ts = new TypicalScenario();
//
//            D d = D.create(ts, rRepository);
//
//            Date t1 = new Date();
//
//            s.save(d);
//            s.getTransaction().commit();
//
//            Date t2 = new Date();
//
//            s.close();
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
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
