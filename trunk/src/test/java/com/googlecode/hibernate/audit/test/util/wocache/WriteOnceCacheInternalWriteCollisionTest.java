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
public class WriteOnceCacheInternalWriteCollisionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log =
        Logger.getLogger(WriteOnceCacheInternalWriteCollisionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // transaction wait before committing in ms
    private long sleepTime = 5000;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testEnclosingJTATransaction_ConcurrentWrite_NoCollision() throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(F.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            final WriteOnceCache<F> cache = new WriteOnceCache<F>(sf);

            final CacheQuery<F> cQuery =
                new CacheQuery<F>(F.class, "s", "alice", "i", new Integer(7));

            CacheQuery<F> cQuery2 = new CacheQuery<F>(F.class, "s", "anna", "i", new Integer(7));

            // access the cache from the main and a parallel tread

            final SessionFactory sameSf = sf;
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
                        s = (SessionImpl)sameSf.openSession();
                        log.debug("beginning transaction ...");
                        s.beginTransaction();

                        log.debug("getting from cache ...");
                        F cf = cache.get(cQuery);
                        assert "alice".equals(cf.getS());
                        assert new Integer(7).equals(cf.getI());

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

            s = sf.openSession();
            s.beginTransaction();

            // cache should be empty, otherwise it's a "dirty read"
            assert cache.getFromCacheOnly(cQuery) == null;
            assert cache.getFromCacheOnly(cQuery2) == null;

            // signal the other thread it can start sleeping/committing
            step2.countDown();

            // write the database from the main thread, this will block until the other transaction
            // commits and releases the locks
            log.debug("trying to get from cache ....");
            F cf2 = cache.get(cQuery2);

            assert "anna".equals(cf2.getS());
            assert new Integer(7).equals(cf2.getI());

            log.debug("commiting ...");
            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            F cf = (F)result;

            assert "alice".equals(cf.getS());
            assert new Integer(7).equals(cf.getI());

            assert cf != cf2;
            assert !cf.getId().equals(cf2.getId());

            // make sure there are two in the database
            s = sf.openSession();
            s.beginTransaction();
            long fc = ((Long)s.createQuery("select count(*) from F").uniqueResult()).longValue();

            log.debug(">>> f count: " + fc);
            assert fc == 2;

            s.getTransaction().commit();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
            }

            throw new Error("failure", e);
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
    public void testEnclosingJTATransaction_ConcurrentWriteCollision_TimedTransaction_CompleteKey()
        throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(F.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            final WriteOnceCache<F> cache = new WriteOnceCache<F>(sf);
            final CacheQuery<F> cQuery =
                new CacheQuery<F>(F.class, "s", "alice", "i", new Integer(7));

            // access the cache from the main and a parallel tread and cause write collision

            final SessionFactory sameSf = sf;
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
                        s = (SessionImpl)sameSf.openSession();
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

            s = sf.openSession();
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
            assert result == cf;

            // make sure there's only one in the database
            s = sf.openSession();
            s.beginTransaction();
            long fc = ((Long)s.createQuery("select count(*) from F").uniqueResult()).longValue();

            log.debug(">>> f count: " + fc);
            assert fc == 1;

            s.getTransaction().commit();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
            }

            throw new Error("failure", e);
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
    public void testEnclosingJTATransaction_ConcurrentWriteCollision_TimedTransaction_PartialNullKey()
        throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(F.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            final WriteOnceCache<F> cache = new WriteOnceCache<F>(sf);
            final CacheQuery<F> cQuery = new CacheQuery<F>(F.class, "s", "alice");

            // access the cache from the main and a parallel tread and cause write collision

            final SessionFactory sameSf = sf;
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
                        s = (SessionImpl)sameSf.openSession();
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

            s = sf.openSession();
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

            log.debug("commiting ...");
            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // test instance identity
            assert result == cf;

            // make sure there's only one in the database
            s = sf.openSession();
            s.beginTransaction();
            long fc = ((Long)s.createQuery("select count(*) from F").uniqueResult()).longValue();

            log.debug(">>> f count: " + fc);
            assert fc == 1;

            s.getTransaction().commit();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
            }

            throw new Error("failure", e);
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
    public void testEnclosingJTATransaction_ConcurrentWriteCollision_ThreeTimedTransactions()
        throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(F.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            final WriteOnceCache<F> cache = new WriteOnceCache<F>(sf);
            final CacheQuery<F> cQuery = new CacheQuery<F>(F.class, "s", "alice");

            // access the cache from the main and a parallel tread and cause write collision

            final SessionFactory sameSf = sf;
            final Exchanger<Object> exchanger = new Exchanger<Object>();
            final Exchanger<Object> exchanger2 = new Exchanger<Object>();

            final CountDownLatch step1 = new CountDownLatch(1);
            final CountDownLatch step2 = new CountDownLatch(1);
            final CountDownLatch step3 = new CountDownLatch(1);

            new Thread(new Runnable()
            {
                public void run()
                {
                    SessionImpl s = null;

                    try
                    {
                        s = (SessionImpl)sameSf.openSession();
                        log.debug("beginning transaction ...");
                        s.beginTransaction();

                        log.debug("getting from cache ...");
                        F cf = cache.get(cQuery);
                        assert "alice".equals(cf.getS());

                        // tell thread 2 that the database was written, but not committed
                        // from the parallel transaction
                        step1.countDown();

                        step3.await();

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
                            step2.countDown();
                            step3.countDown();
                            exchanger.exchange(t);
                        }
                        catch(InterruptedException e)
                        {
                            // ignore
                        }
                    }
                }
            }, "ThreadOne").start();

            new Thread(new Runnable()
            {
                public void run()
                {
                    SessionImpl s = null;

                    try
                    {
                        step1.await();

                        s = (SessionImpl)sameSf.openSession();
                        log.debug("beginning transaction ...");
                        s.beginTransaction();

                        // cache should be empty, otherwise it's a "dirty read"
                        assert cache.getFromCacheOnly(cQuery) == null;

                        // tell the main thread to try to get from cache and block
                        step2.countDown();

                        log.debug("getting from cache ...");
                        F cf = cache.get(cQuery);
                        assert "alice".equals(cf.getS());

                        s.getTransaction().commit();
                        s.close();

                        try
                        {
                            exchanger2.exchange(cf);
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
                            step2.countDown();
                            step3.countDown();
                            exchanger2.exchange(t);
                        }
                        catch(InterruptedException e)
                        {
                            // ignore
                        }
                    }
                }
            }, "ThreadTwo").start();

            // wait for the signal that the database was written, but not committed, from
            // the parallel thread and thread 2 is attempting to read
            step2.await();

            s = sf.openSession();
            s.beginTransaction();

            // cache should be empty, otherwise it's a "dirty read"
            assert cache.getFromCacheOnly(cQuery) == null;

            // signal thread 1 it can start sleeping/committing
            step3.countDown();

            // write the database from the main thread, this will block until the other transaction
            // commits and releases the locks
            log.debug("trying to get from cache ....");
            F cf = cache.get(cQuery);
            log.debug("commiting ...");
            s.getTransaction().commit();

            assert "alice".equals(cf.getS());

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // test instance identity
            assert result == cf;

            result = exchanger2.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // test instance identity
            assert result == cf;

            // make sure there's only one in the database
            s = sf.openSession();
            s.beginTransaction();
            long fc = ((Long)s.createQuery("select count(*) from F").uniqueResult()).longValue();

            log.debug(">>> f count: " + fc);
            assert fc == 1;

            s.getTransaction().commit();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
            }

            throw new Error("failure", e);
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-129
//    public void testNoEnclosingTransaction_ConcurrentWriteCollision() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-34,
//                                https://jira.novaordis.org/browse/HBA-129
//    public void testEnclosingLocalTransaction_ConcurrentWriteCollision() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}