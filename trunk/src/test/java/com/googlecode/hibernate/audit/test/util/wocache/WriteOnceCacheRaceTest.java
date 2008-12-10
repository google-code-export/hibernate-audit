package com.googlecode.hibernate.audit.test.util.wocache;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.impl.SessionImpl;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.wocache.data.F;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCache;
import com.googlecode.hibernate.audit.util.wocache.CacheQuery;

import java.util.concurrent.Exchanger;
import java.util.concurrent.CountDownLatch;

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
public class WriteOnceCacheRaceTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteOnceCacheRaceTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // transaction wait before committing in ms
    private long sleepTime = 200;

    private int loopCount = 100;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    /**
     * TODO partially commented out, see https://jira.novaordis.org/browse/HBA-189, do uncomment
     */
    @Test(enabled = true)
    public void testRaceOne() throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(F.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            WriteOnceCache<F> cache = new WriteOnceCache<F>(sf);
            CacheQuery<F> cQuery = new CacheQuery<F>(F.class, "s", "alice", "i", 7);

            for(int i = 0; i < loopCount; i++)
            {
                log.debug("run number " + i);
                runOnce(sf, cache, cQuery);
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

    /**
     * TODO partially commented out, see https://jira.novaordis.org/browse/HBA-189, do uncomment
     */
    @Test(enabled = true)
    public void testRaceTwo() throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(F.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            WriteOnceCache<F> cache = new WriteOnceCache<F>(sf);
            CacheQuery<F> cQuery = new CacheQuery<F>(F.class, "s", "alice", "i", 7);

            // enter data in database
            Session s = sf.openSession();
            s.beginTransaction();
            s.save(new F("alice", 7));
            s.getTransaction().commit();
            s.close();

            for(int i = 0; i < loopCount; i++)
            {
                log.debug("run number " + i);
                runOnce(sf, cache, cQuery);
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


    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    /**
     * Access the cache from the main and a parallel tread and cause write collision.
     */
    private void runOnce(final SessionFactory sf,
                         final WriteOnceCache<F> cache,
                         final CacheQuery<F> cQuery) throws Throwable
    {
        final Exchanger<Object> exchanger = new Exchanger<Object>();
        final CountDownLatch step1 = new CountDownLatch(1);
        final CountDownLatch step2 = new CountDownLatch(1);

        new Thread(new Runnable()
        {
            public void run()
            {
                SessionImpl s = null;

                try
                {
                    s = (SessionImpl)sf.openSession();

                    log.debug("beginning transaction ...");
                    s.beginTransaction();

                    log.debug("getting from cache ...");
                    F cf = cache.get(cQuery);
                    assert "alice".equals(cf.getS());

                    // tell the main thread that the database was written, but not committed
                    // from the parallel transaction
                    step1.countDown();
                    step2.await();

                    // need to sleep and then commit, if main thread's notification will never
                    // come because it's blocked trying to acquire a lock held by this
                    // transaction

                    log.debug("\n\nsleeping for " + sleepTime + " ms ...\n\n");
                    Thread.sleep(sleepTime);
                    log.debug("done sleeping, committing ...");

                    s.getTransaction().commit();
                    s.close();

                    try
                    {
                        exchanger.exchange(cf);
                    }
                    catch(InterruptedException e)
                    {
                        // ignore
                    }
                }
                catch(Throwable t)
                {
                    try
                    {
                        // release all locks and bubble up the exception
                        step1.countDown();
                        exchanger.exchange(t);
                    }
                    catch(InterruptedException e)
                    {
                        // ignore
                    }
                }
            }
        }, "ThreadOne").start();

        // wait for the signal that the database was written, but not committed, from
        // the parallel thread
        step1.await();

        Session s = sf.openSession();
        s.beginTransaction();

        // cache should be empty, otherwise it's a "dirty read"
        assert cache.getFromCacheOnly(cQuery) == null;

        // signal the other thread it can start sleeping/committing
        step2.countDown();

        // write the database from the main thread, this will block until the other transaction
        // commits and releases the locks
        log.debug("trying to get from cache ....");
        F cf = cache.get(cQuery);

        assert "alice".equals(cf.getS());
        assert new Integer(7).equals(cf.getI());

        log.debug("commiting ...");
        s.getTransaction().commit();

        Object result = exchanger.exchange(null);
        if (result instanceof Throwable)
        {
            throw (Throwable)result;
        }

        // test instance identity

        // TODO Uncomment after fixing https://jira.novaordis.org/browse/HBA-189
        //
        // assert result == cf;
        //

        // make sure there's only one in the database
        s = sf.openSession();
        s.beginTransaction();
        long fc = ((Long)s.createQuery("select count(*) from F").uniqueResult()).longValue();
        assert fc == 1;

        s.getTransaction().commit();
        s.close();

        cache.clear();
    }

    // Inner classes -------------------------------------------------------------------------------

}