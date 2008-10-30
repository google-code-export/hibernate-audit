package com.googlecode.hibernate.audit.test.performance;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.performance.data.s2.D;
import com.googlecode.hibernate.audit.test.performance.data.s2.RRepository;
import com.googlecode.hibernate.audit.test.performance.data.s2.TypicalScenario;
import com.googlecode.hibernate.audit.test.performance.util.Series;
import com.googlecode.hibernate.audit.test.performance.util.Run;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.util.packinsp.PackageInspector;
import com.googlecode.hibernate.audit.util.packinsp.Filter;

import javax.persistence.Entity;
import java.util.Set;
import java.lang.annotation.Annotation;

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

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public S2PerformanceTest()
    {
    }

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void performanceTest() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());

        Set<Class> entities = new PackageInspector(D.class).inspect(new Filter()
        {
            public boolean accept(Class c)
            {
                for(Annotation a: c.getAnnotations())
                {
                    if (a.annotationType().equals(Entity.class))
                    {
                        return true;
                    }
                }

                return false;
            }
        });

        for(Class c: entities)
        {
            config.addAnnotatedClass(c);
        }

        SessionFactoryImplementor sf = null;

        Series series = new Series(20);

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            RRepository rRepository = new RRepository(10);
            rRepository.populate(sf, true);

            System.setProperty("hba.show_sql", "false");
            System.setProperty("hba.jdbc.batch_size", "500");

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            for(Run run: series.getRuns())
            {
                Session s = sf.openSession();
                s.beginTransaction();

                D d = D.create(new TypicalScenario(), rRepository);

                run.startClock();

                s.save(d);
                s.getTransaction().commit();

                run.stopClock();

                s.close();
            }
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }

        series.printStatistics(false);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
