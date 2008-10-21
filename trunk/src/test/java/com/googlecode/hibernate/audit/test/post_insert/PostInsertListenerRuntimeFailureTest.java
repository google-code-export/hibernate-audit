package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.EventSource;
import org.hibernate.impl.SessionFactoryImpl;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.A;
import com.googlecode.hibernate.audit.HibernateAuditException;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.LogicalGroupIdProvider;
import com.googlecode.hibernate.audit.listener.Listeners;
import com.googlecode.hibernate.audit.listener.AuditEventListener;

import java.sql.SQLException;
import java.io.Serializable;

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
public class PostInsertListenerRuntimeFailureTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertListenerRuntimeFailureTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testRuntimeFailureOnMockListener() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImpl sf = null;

        try
        {
            sf = (SessionFactoryImpl)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Listeners.
                installAuditEventListener(sf, "post-insert", new BrokenPostInsertEventListner());

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(new A());

            try
            {
                s.getTransaction().commit();
                throw new Error("should've failed");
            }
            catch(HibernateAuditException e)
            {
                assert e.getCause() instanceof SQLException;
                Transaction t = s.getTransaction();
                
                // my mock TM dissasociates a rolled back transaction from the thread, so I cannot
                // test t.wasRolledBack() here
                assert !t.isActive();
                assert !t.wasCommitted();
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
    }

    @Test(enabled = true)
    public void testRuntimeFailureOnRealListener() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImpl sf = null;

        try
        {
            sf = (SessionFactoryImpl)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());

            // we use a BrokenLogicalGroupIdProvider that throws an  ExoticRuntimeException smack
            // in the middle of event processing

            HibernateAudit.register(sf, new BrokenLogicalGroupIdProvider());

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(new A());

            try
            {
                s.getTransaction().commit();
                throw new Error("should've failed");
            }
            catch(HibernateAuditException e)
            {
                assert e.getCause() instanceof ExoticRuntimeException;
                Transaction t = s.getTransaction();

                // my mock TM dissasociates a rolled back transaction from the thread, so I cannot
                // test t.wasRolledBack() here
                assert !t.isActive();
                assert !t.wasCommitted();
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
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    class BrokenPostInsertEventListner implements AuditEventListener, PostInsertEventListener
    {
        public void onPostInsert(PostInsertEvent event)
        {
            try
            {
                throw new SQLException("bogus SQL exception");
            }
            catch(Throwable t)
            {
                Transaction tx = event.getSession().getTransaction();

                try
                {
                    tx.rollback();
                }
                catch(Throwable t2)
                {
                    log.error("could not rollback current transaction", t2);
                }

                throw new HibernateAuditException("bogus SQL exception carrier", t);
            }
        }
    }

    class BrokenLogicalGroupIdProvider implements LogicalGroupIdProvider
    {
        public Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity)
        {
            throw new ExoticRuntimeException();
        }
    }

    class ExoticRuntimeException extends RuntimeException
    {
    }
}
