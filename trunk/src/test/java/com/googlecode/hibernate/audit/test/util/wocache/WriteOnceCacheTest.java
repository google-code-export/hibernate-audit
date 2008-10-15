package com.googlecode.hibernate.audit.test.util.wocache;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.HibernateException;
import org.hibernate.impl.SessionImpl;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.wocache.data.A;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCache;
import com.googlecode.hibernate.audit.util.wocache.CacheQuery;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCacheException;

import javax.naming.InitialContext;
import javax.transaction.UserTransaction;
import javax.transaction.Status;
import java.util.List;
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
public class WriteOnceCacheTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteOnceCacheTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    /**
     * This test checks behavior if the object doesn't exist in the database and also if it exists.
     */
    @Test(enabled = false)
    public void testNoEnclosingTransaction_SuccessfulDatabaseInsert() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        // make sure there isn't any kind of active transaction laying around

        String utJndiName = getUserTransactionJNDIName();
        InitialContext ic = new InitialContext();
        UserTransaction ut = (UserTransaction)ic.lookup(utJndiName);
        assert Status.STATUS_NO_TRANSACTION == ut.getStatus();

        try
        {
            sf = config.buildSessionFactory();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            A a = cache.get(new CacheQuery<A>(A.class, "s", "blah"));

            Long id = a.getId();
            assert id != null;
            assert "blah".equals(a.getS());

            cache.clear();

            a = (A)cache.get(new CacheQuery<A>(A.class, "s", "blah"));

            // TODO counters broken https://jira.novaordis.org/browse/HBA-128
//            assert 0 == cache.getHitCount();
//            assert 1 == cache.getMissCount();
//            assert 0 == cache.getDatabaseInsertionCount();

            assert id.equals(a.getId());
            assert "blah".equals(a.getS());

            a = (A)cache.get(new CacheQuery<A>(A.class, "s", "blah"));

            // TODO counters broken https://jira.novaordis.org/browse/HBA-128
//            assert 1 == cache.getHitCount();
//            assert 1 == cache.getMissCount();
//            assert 0 == cache.getDatabaseInsertionCount();

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

        // make sure there isn't any kind of active transaction laying around
        assert Status.STATUS_NO_TRANSACTION == ut.getStatus();
        ic.close();
    }

    @Test(enabled = false)
    public void testNoEnclosingTransaction_DatabaseFailure_Duplicates() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        // make sure there isn't any kind of active transaction laying around

        String utJndiName = getUserTransactionJNDIName();
        InitialContext ic = new InitialContext();
        UserTransaction ut = (UserTransaction)ic.lookup(utJndiName);
        assert Status.STATUS_NO_TRANSACTION == ut.getStatus();

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
            s.close();

            // make sure we have two 'alices'
            s = sf.openSession();
            s.beginTransaction();
            assert s.createQuery("from A where s = 'alice'").list().size() == 2;
            s.getTransaction().commit();
            s.close();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            try
            {
                cache.get(new CacheQuery<A>(A.class, "s", "alice"));
                throw new Error("should've failed");
            }
            catch(WriteOnceCacheException e)
            {
                log.debug(">>> " + e.getMessage());
            }

            assert 0 == cache.getLoad();

            // make sure we still have two 'alices'
            s = sf.openSession();
            s.beginTransaction();
            assert s.createQuery("from A where s = 'alice'").list().size() == 2;
            s.getTransaction().commit();
            s.close();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
            }
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }

        // make sure there isn't any kind of active transaction laying around
        assert Status.STATUS_NO_TRANSACTION == ut.getStatus();
        ic.close();
    }

    /**
     * This test checks behavior if the object doesn't exist in the database and also if it exists.
     */
    @Test(enabled = false)
    public void testEnclosingJTATransaction_SuccessfulDatabaseInsert() throws Throwable
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
            s.beginTransaction();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            A ca = cache.get(new CacheQuery<A>(A.class, "s", "alice"));

            Long id = ca.getId();
            assert id != null;
            assert "alice".equals(ca.getS());
            assert ca.getI() == null;

            assert cache.getLoad() == 1;

            // make sure we still have an active transaction going on
            assert ((SessionImpl)s).isTransactionInProgress();

            // re-enter the cache on the same thread/same transaction

            // TODO reentrance is broken https://jira.novaordis.org/browse/HBA-130, uncomment this
            //ca = cache.get(new CacheQuery<A>(A.class, "s", "alice"));

            // make sure we still have an active transaction going on
            assert ((SessionImpl)s).isTransactionInProgress();

            assert id.equals(ca.getId());
            assert "alice".equals(ca.getS());
            assert ca.getI() == null;

            assert cache.getLoad() == 1;

            // make sure write-only is not in the database yet, probe from a different thread not
            // to interfere with the on-going transaction
            final SessionFactory sameSf = sf;
            final Exchanger<Object> exchanger = new Exchanger<Object>();
            new Thread(new Runnable()
            {
                public void run()
                {
                    SessionImpl s = null;
                    try
                    {
                        s = (SessionImpl)sameSf.openSession();

                        assert !s.isTransactionInProgress();
                        s.beginTransaction();

                        Long c = (Long)s.createQuery("select count(*) from A").uniqueResult();

                        s.getTransaction().commit();

                        exchanger.exchange(c); // pass the result to the main thread
                    }
                    catch(Throwable t)
                    {
                        try
                        {
                            exchanger.exchange(t);
                        }
                        catch(InterruptedException e)
                        {
                            // ignore
                        }
                    }
                    finally
                    {
                        if (s != null)
                        {
                            s.close();
                        }

                    }
                }
            }, "Database Probe").start();

            // wait for the database probe thread to finish
            Object databaseProbeResult = exchanger.exchange(null);

            if (databaseProbeResult instanceof Throwable)
            {
                // database probe failed
                throw (Throwable)databaseProbeResult;
            }

            assert ((Long)databaseProbeResult).longValue() == 0;

            s.getTransaction().commit();
            s.close();

            // make sure write-only is in the database now

            s = sf.openSession();
            s.beginTransaction();
            A da = (A)s.createQuery("from A").uniqueResult();
            assert id.equals(da.getId());
            assert "alice".equals(da.getS());
            assert da.getI() == null;
            s.getTransaction().commit();
            s.close();

            // hit the cache again in absence of any transaction
            ca = cache.get(new CacheQuery<A>(A.class, "s", "alice"));

            assert id.equals(ca.getId());
            assert "alice".equals(ca.getS());
            assert ca.getI() == null;

            assert cache.getLoad() == 1;
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
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

    @Test(enabled = true)
    public void testEnclosingJTATransaction_InTransactionVisibility() throws Throwable
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

            // we start an enclosing JTA transaction
            s.beginTransaction();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            CacheQuery<A> cQuery = new CacheQuery<A>(A.class, "s", "alice");
            A ca = cache.get(cQuery);

            Long id = ca.getId();
            assert id != null;
            assert "alice".equals(ca.getS());
            assert ca.getI() == null;

            // make sure we still have an active transaction going on
            assert ((SessionImpl)s).isTransactionInProgress();

            // from this transaction, look into the database, we should see the write-once data
            A da = (A)s.createQuery("from A").uniqueResult();
            assert da != null;
            assert id.equals(da.getId());
            assert "alice".equals(da.getS());
            assert da.getI() == null;

            s.getTransaction().commit();
            s.close();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                 s.getTransaction().rollback();
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
    public void testEnclosingJTATransaction_EnclosingTransactionRollback() throws Throwable
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

            // we start an enclosing JTA transaction
            s.beginTransaction();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            CacheQuery<A> cQuery = new CacheQuery<A>(A.class, "s", "alice");
            A ca = cache.get(cQuery);

            Long id = ca.getId();
            assert id != null;
            assert "alice".equals(ca.getS());
            assert ca.getI() == null;

            assert cache.getLoad() == 1;

            // make sure we still have an active transaction going on
            assert ((SessionImpl)s).isTransactionInProgress();

            s.getTransaction().rollback();
            s.close();

            // database should be empty ...
            s = sf.openSession();
            s.beginTransaction();
            List result = s.createQuery("from A").list();
            assert result.isEmpty();
            s.getTransaction().commit();
            s.close();

            // and so the cache ...
            assert null == cache.getFromCacheOnly(cQuery);
            assert cache.getLoad() == 0;
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
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
    public void testEnclosingJTATransaction_DatabaseFailureOnWriteOnce() throws Exception
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
            assert s.createQuery("from A where s = 'alice'").list().size() == 2;
            s.getTransaction().commit();

            // start an enclosing JTA transaction
            s.beginTransaction();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            try
            {
                cache.get(new CacheQuery<A>(A.class, "s", "alice"));
                throw new Error("should've failed");
            }
            catch(WriteOnceCacheException e)
            {
                log.debug(">>> " + e.getMessage());
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

            assert cache.getLoad() == 0;

            s.beginTransaction();
            assert s.createQuery("from A where s = 'alice'").list().size() == 2;
            s.getTransaction().commit();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
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

//    /**
//     * This test checks behavior if the object doesn't exist in the database and also if it exists.
//     */
//    @Test(enabled = false) TODO https://jira.novaordis.org/browse/HBA-34
//    public void testEnclosingLocalTransaction_SuccessfulDatabaseInsert() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//
//    @Test(enabled = false) TODO https://jira.novaordis.org/browse/HBA-34
//    public void testEnclosingLocalTransaction_EnclosingTransactionRollback() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//
//    @Test(enabled = false) TODO https://jira.novaordis.org/browse/HBA-34
//    public void testEnclosingLocalTransaction_DatabaseFailureOnWriteOnce() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//

    @Test(enabled = false)
    public void testEnclosingJTATransaction_Insure_ReadCommitted_Commit() throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            final WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);
            final CacheQuery<A> cQuery = new CacheQuery<A>(A.class, "s", "alice");

            // load the cache from a parallel thread

            final SessionFactory sameSf = sf;
            final Exchanger<Object> exchanger = new Exchanger<Object>();
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
                        s.beginTransaction();

                        A ca = cache.get(cQuery);
                        Long id = ca.getId();
                        assert  id != null;
                        assert "alice".equals(ca.getS());
                        assert cache.getLoad() == 1;

                        // tell the main thread that the cache was hit
                        step1.countDown();

                        // wait for the commit signal from the main thread
                        step2.await();

                        log.debug("Cache Loader committing ...");
                        s.getTransaction().commit();
                        s.close();

                        // tell the main thread commit completed successfully
                        step3.countDown();

                        try
                        {
                            exchanger.exchange(id);
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
                            step3.countDown();
                            exchanger.exchange(t);
                        }
                        catch(InterruptedException e)
                        {
                            // ignore
                        }
                    }
                }
            }, "Cache Loader").start();

            // wait for the cache to be loaded
            step1.await();

            // cache should be empty, otherwise it's a "dirty read"

            s = sf.openSession();
            s.beginTransaction();

            assert cache.getFromCacheOnly(cQuery) == null;
            
            assert cache.getLoad() == 0;

            // commit the other transaction
            step2.countDown();

            // wait for the transaction to be committed
            step3.await();

            A ca = cache.getFromCacheOnly(cQuery);
            Long id = ca.getId();
            assert id != null;
            assert "alice".equals(ca.getS());

            assert cache.getLoad() == 1;

            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            assert ((Long)result).equals(id);
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
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
    public void testEnclosingJTATransaction_Insure_ReadCommitted_Rollback() throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            final WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);
            final CacheQuery<A> cQuery = new CacheQuery<A>(A.class, "s", "alice");

            // load the cache from a parallel thread

            final SessionFactory sameSf = sf;
            final Exchanger<Object> exchanger = new Exchanger<Object>();
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
                        s.beginTransaction();

                        A ca = cache.get(cQuery);
                        Long id = ca.getId();
                        assert  id != null;
                        assert "alice".equals(ca.getS());

                        assert cache.getLoad() == 1;

                        // tell the main thread that the cache was hit
                        step1.countDown();

                        // wait for the rollback signal from the main thread
                        step2.await();

                        log.debug("Cache Loader rolling back ...");
                        s.getTransaction().rollback();
                        s.close();

                        // tell the main thread rollback completed
                        step3.countDown();

                        try
                        {
                            exchanger.exchange(id);
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
                            step3.countDown();
                            exchanger.exchange(t);
                        }
                        catch(InterruptedException e)
                        {
                            // ignore
                        }
                    }
                }
            }, "Cache Loader").start();

            // wait for the cache to be loaded
            step1.await();

            // cache should be empty, otherwise it's a "dirty read"

            s = sf.openSession();
            s.beginTransaction();

            assert cache.getFromCacheOnly(cQuery) == null;

            assert cache.getLoad() == 0;

            // rollback the other transaction
            step2.countDown();

            // wait for the transaction to rollback
            step3.await();

            assert cache.getFromCacheOnly(cQuery) == null;

            assert cache.getLoad() == 1;

            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // make sure there's nothing in database
            s = sf.openSession();
            s.beginTransaction();
            assert ((Long)s.createQuery("select count(*) from A").uniqueResult()).longValue() == 0;
            s.getTransaction().commit();
        }
        catch(Exception e)
        {
            if (s != null && s.getTransaction() != null)
            {
                s.getTransaction().rollback();
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

//    @Test(enabled = false) TODO https://jira.novaordis.org/browse/HBA-129
//    public void testEnclosingJTATransaction_ConcurrentWriteCollision() throws Throwable
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        SessionFactory sf = null;
//        Session s = null;
//
//        try
//        {
//            sf = config.buildSessionFactory();
//
//            final WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);
//            final CacheQuery<A> cQuery = new CacheQuery<A>(A.class, "s", "alice");
//
//            // access the cache from the main and a parallel tread and cause write collision
//
//            final SessionFactory sameSf = sf;
//            final Exchanger<Object> exchanger = new Exchanger<Object>();
//            final CountDownLatch step1 = new CountDownLatch(1);
//            final CountDownLatch step2 = new CountDownLatch(1);
//            final CountDownLatch step3 = new CountDownLatch(1);
//
//            new Thread(new Runnable()
//            {
//                public void run()
//                {
//                    SessionImpl s = null;
//                    try
//                    {
//                        s = (SessionImpl)sameSf.openSession();
//                        s.beginTransaction();
//
//                        A ca = cache.get(cQuery);
//                        assert "alice".equals(ca.getS());
//
//                        // tell the main thread that the database was written from the parallel
//                        // transaction
//                        step1.countDown();
//
//                        // wait for the main thread to signal that it has written the database too
//                        step2.await();
//
//                        log.debug("commiting ...");
//                        s.getTransaction().commit();
//                        s.close();
//
//                        // tell the main thread the commit completed
//                        step3.countDown();
//
//                        try
//                        {
//                            exchanger.exchange(ca.getId());
//                        }
//                        catch(InterruptedException e)
//                        {
//                            // ignore
//                        }
//                    }
//                    catch(Throwable t)
//                    {
//                        try
//                        {
//                            // release all locks and bubble up the exception
//                            step1.countDown();
//                            step3.countDown();
//                            exchanger.exchange(t);
//                        }
//                        catch(InterruptedException e)
//                        {
//                            // ignore
//                        }
//                    }
//                }
//            }, "Cache Loader").start();
//
//            // wait for the signal that the database was written from the parallel thread
//            step1.await();
//
//            s = sf.openSession();
//            s.beginTransaction();
//
//            // cache should be empty, otherwise it's a "dirty read"
//            assert cache.getFromCacheOnly(cQuery) == null;
//
//            // write the database from the main thread
//
//            A ca = cache.get(cQuery);
//            assert "alice".equals(ca.getS());
//
//            // tell the parallel thread that the main thread has written the database
//            step2.countDown();
//
//            // wait for the other transaction to commit
//            step3.await();
//
//            // TODO this should throw some sort of exception, as we do have a collision
//            // See https://jira.novaordis.org/browse/HBA-129
//            s.getTransaction().commit();
//
//            // make sure there's only one in the database
//            s = sf.openSession();
//            s.beginTransaction();
//            long aCount =
//                ((Long)s.createQuery("select count(*) from A").uniqueResult()).longValue();
//            log.debug(">>> aCount: " + aCount);
//            assert 1 == aCount;
//
//            s.getTransaction().commit();
//
//            Object result = exchanger.exchange(null);
//            if (result instanceof Throwable)
//            {
//                throw (Throwable)result;
//            }
//        }
//    catch(Exception e)
//    {
//        if (s != null && s.getTransaction() != null)
//        {
//            s.getTransaction().rollback();
//        }
//    }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
    
//    @Test(enabled = false) TODO https://jira.novaordis.org/browse/HBA-129
//    public void testNoEnclosingTransaction_ConcurrentWriteCollision() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//

//    @Test(enabled = false) TODO https://jira.novaordis.org/browse/HBA-34,
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
