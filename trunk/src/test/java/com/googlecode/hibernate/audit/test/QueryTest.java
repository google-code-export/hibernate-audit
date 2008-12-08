package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.cfg.AnnotationConfiguration;
import com.googlecode.hibernate.audit.util.QueryParameters;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.data.TimestampEntity;

import java.util.List;
import java.util.Date;
import java.sql.Timestamp;

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
public class QueryTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testTimestampQuery() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(TimestampEntity.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            Session s = sf.openSession();
            s.beginTransaction();

            Date t1 = new Date();

            Thread.sleep(1000);

            TimestampEntity te = new TimestampEntity();
            te.setTimestamp(new Date());
            s.save(te);

            s.getTransaction().commit();
            s.close();

            Thread.sleep(1000);

            Date t2 = new Date();

            Session qSession = sf.openSession();
            qSession.beginTransaction();

            String qs = "from TimestampEntity as te where te.timestamp <= :t1";
            Query q = qSession.createQuery(qs);
            QueryParameters.fill(q, new Timestamp(t1.getTime()));
            List result = q.list();

            assert result.isEmpty();

            qs = "from TimestampEntity as te where te.timestamp <= :t2";
            q = qSession.createQuery(qs);
            QueryParameters.fill(q, new Timestamp(t2.getTime()));
            result = q.list();

            assert result.size() == 1;

            qs = "from TimestampEntity as te where te.timestamp >= :t1 and te.timestamp <= :t2";
            q = qSession.createQuery(qs);
            QueryParameters.fill(q, new Timestamp(t1.getTime()), new Timestamp(t2.getTime()));
            result = q.list();

            assert result.size() == 1;

            qSession.getTransaction().commit();
            qSession.close();
        }
        finally
        {
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
