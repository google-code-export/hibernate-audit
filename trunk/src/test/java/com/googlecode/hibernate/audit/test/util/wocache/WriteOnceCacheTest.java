package com.googlecode.hibernate.audit.test.util.wocache;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.HibernateException;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.wocache.data.A;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCache;
import com.googlecode.hibernate.audit.util.wocache.CacheQuery;

import java.util.List;

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
public class WriteOnceCacheTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteOnceCacheTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = false)
    public void testAccessCacheWitouthExternalTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            assert 0 == cache.getHitCount();
            assert 0 == cache.getMissCount();
            assert 0 == cache.getDatabaseInsertionCount();

            CacheQuery<A> cquery = new CacheQuery<A>(A.class, "s", "blah");
            A a = cache.get(cquery);

            assert 0 == cache.getHitCount();
            assert 1 == cache.getMissCount();
            assert 1 == cache.getDatabaseInsertionCount();

            Long id = a.getId();
            assert id != null;
            assert "blah".equals(a.getS());

            cache.clear();

            a = (A)cache.get(cquery);

            assert 0 == cache.getHitCount();
            assert 1 == cache.getMissCount();
            assert 0 == cache.getDatabaseInsertionCount();

            assert id.equals(a.getId());
            assert "blah".equals(a.getS());

            a = (A)cache.get(cquery);

            assert 1 == cache.getHitCount();
            assert 1 == cache.getMissCount();
            assert 0 == cache.getDatabaseInsertionCount();

            assert id.equals(a.getId());
            assert "blah".equals(a.getS());
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
    public void testAccessCacheInPresenceOfJTATransaction_DatabaseInteractionFails()
        throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            // enter two A instances with the same 'name'

            s = sf.openSession();
            s.beginTransaction();
            s.save(new A("alice"));
            s.save(new A("alice"));
            s.getTransaction().commit();

            // make sure we have two 'alices'
            s.beginTransaction();
            Query q = s.createQuery("from A where s = 'alice'");
            List alices = q.list();
            assert alices.size() == 2;
            s.getTransaction().commit();

            // we start an enclosing JTA transaction
            s.beginTransaction();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            try
            {
                cache.get(new CacheQuery<A>(A.class, "s", "alice"));
                throw new Error("should've failed");
            }
            catch(Exception e)
            {
                log.debug(">>> " + e.getMessage());
                assert !s.getTransaction().isActive();
                assert s.getTransaction().wasRolledBack();

                try
                {
                    s.getTransaction().commit();
                    new Error("should've failed");
                }
                catch(HibernateException e2)
                {
                    log.debug(">>> " + e2.getMessage());
                }
            }
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = false)
    public void testAccessCacheInPresenceOfJTATransaction_DatabaseInteractionSucceeds()
        throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            s = sf.openSession();

            // we start a JTA transaction
            s.beginTransaction();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);
            CacheQuery<A> cquery = new CacheQuery<A>(A.class, "s", "blah");

            A a = cache.get(cquery);

            throw new RuntimeException("NOT YET IMPLEMENTED");

        }
        finally
        {
            if (s != null)
            {
                s.getTransaction().commit();
            }

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
