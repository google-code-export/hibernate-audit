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
import com.googlecode.hibernate.audit.test.util.wocache.data.E;
import com.googlecode.hibernate.audit.test.util.wocache.data.D;
import com.googlecode.hibernate.audit.test.util.wocache.data.G;
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
    @Test(enabled = true)
    public void testNoEnclosingTransaction_SuccessfulDatabaseInsert() throws Exception
    {
        log.debug("testNoEnclosingTransaction_SuccessfulDatabaseInsert");

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

            assert id.equals(a.getId());
            assert "blah".equals(a.getS());

            a = (A)cache.get(new CacheQuery<A>(A.class, "s", "blah"));

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

    @Test(enabled = true)
    public void testNoEnclosingTransaction_NoInsertQuery() throws Exception
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

            assert null == cache.get(new CacheQuery<A>(A.class, false, "s", "blah"));

            // also make sure there's nothing in the database

            Session s = sf.openSession();
            s.beginTransaction();
            List result = s.createQuery("from A").list();
            s.getTransaction().commit();
            s.close();

            assert result.isEmpty();

            // insert it in the database

            A a = cache.get(new CacheQuery<A>(A.class, "s", "blah"));

            Long id = a.getId();
            assert id != null;
            assert "blah".equals(a.getS());

            // clear the cache

            cache.clear();

            // make sure it's there when we retrieve with 'no insert' option

            a = cache.get(new CacheQuery<A>(A.class, false, "s", "blah"));

            id = a.getId();
            assert id != null;
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


    @Test(enabled = true)
    public void testNoEnclosingTransaction_DatabaseFailure_Duplicates() throws Exception
    {
        log.debug("testNoEnclosingTransaction_DatabaseFailure_Duplicates");

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

            // make sure we still have two 'alices'
            s = sf.openSession();
            s.beginTransaction();
            assert s.createQuery("from A where s = 'alice'").list().size() == 2;
            s.getTransaction().commit();
            s.close();
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
    @Test(enabled = true)
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

            // make sure we still have an active transaction going on
            assert ((SessionImpl)s).isTransactionInProgress();

            // re-enter the cache on the same thread/same transaction

            A ca2 = cache.get(new CacheQuery<A>(A.class, "s", "alice"));

            assert ca == ca2;

            // make sure we still have an active transaction going on
            assert ((SessionImpl)s).isTransactionInProgress();

            assert id.equals(ca.getId());
            assert "alice".equals(ca.getS());
            assert ca.getI() == null;

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
    public void testEnclosingJTATransaction_NoInsertQuery() throws Throwable
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

            // use 'no-insert' option

            assert null == cache.get(new CacheQuery<A>(A.class, false, "s", "alice"));

            // insert it in the database

            A a = cache.get(new CacheQuery<A>(A.class, "s", "blah"));

            s.getTransaction().commit();

            Long id = a.getId();
            assert id != null;
            assert "blah".equals(a.getS());

            // clear the cache

            cache.clear();

            s.beginTransaction();

            // make sure it's there when we retrieve with 'no insert' option

            a = cache.get(new CacheQuery<A>(A.class, false, "s", "blah"));

            id = a.getId();
            assert id != null;
            assert "blah".equals(a.getS());
            s.getTransaction().commit();
            s.close();
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // https://jira.novaordis.org/browse/HBA-127
    @Test(enabled = true)
    public void testEnclosingJTATransaction_InTransactionVisibility() throws Throwable
    {
        log.debug("testEnclosingJTATransaction_InTransactionVisibility");

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
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testEnclosingJTATransaction_EnclosingTransactionRollback() throws Throwable
    {
        log.debug("testEnclosingJTATransaction_EnclosingTransactionRollback");

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

            CacheQuery<A> cQuery = new CacheQuery<A>(A.class, "s", "alice");
            A ca = cache.get(cQuery);

            Long id = ca.getId();
            assert id != null;
            assert "alice".equals(ca.getS());
            assert ca.getI() == null;

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
    public void testEnclosingJTATransaction_DatabaseFailureOnWriteOnce() throws Exception
    {
        log.debug("testEnclosingJTATransaction_DatabaseFailureOnWriteOnce");

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
                    throw new Error("should've failed");
                }
                catch(HibernateException e2)
                {
                    log.debug(">>> " + e2.getMessage());
                }
            }

            s.beginTransaction();
            assert s.createQuery("from A where s = 'alice'").list().size() == 2;
            s.getTransaction().commit();
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
//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-34
//    public void testEnclosingLocalTransaction_SuccessfulDatabaseInsert() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//
//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-34
//    public void testEnclosingLocalTransaction_EnclosingTransactionRollback() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//
//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-34
//    public void testEnclosingLocalTransaction_DatabaseFailureOnWriteOnce() throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }
//

    @Test(enabled = true)
    public void testEnclosingJTATransaction_Insure_ReadCommitted_Commit() throws Throwable
    {
        log.debug("testEnclosingJTATransaction_Insure_ReadCommitted_Commit");

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
            
            // commit the other transaction
            step2.countDown();

            // wait for the transaction to be committed
            step3.await();

            A ca = cache.getFromCacheOnly(cQuery);
            Long id = ca.getId();
            assert id != null;
            assert "alice".equals(ca.getS());

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

    @Test(enabled = true)
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

            // rollback the other transaction
            step2.countDown();

            // wait for the transaction to rollback
            step3.await();

            assert cache.getFromCacheOnly(cQuery) == null;

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

    // https://jira.novaordis.org/browse/HBA-133
    @Test(enabled = true)
    public void testSameJTATransaction_SameEntityTwice() throws Throwable
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

            CacheQuery<A> cQuery = new CacheQuery<A>(A.class, "s", "alice");
            A ca = cache.get(cQuery);

            Long id = ca.getId();
            assert id != null;
            assert "alice".equals(ca.getS());

            // run the same query again, same transaction, we should get the same object instance

            A ca2 = cache.get(cQuery);

            assert ca == ca2;

            s.getTransaction().commit();

            A ca3 = cache.get(cQuery);

            assert ca == ca3;
            assert ca == ca2;
            
            s.close();
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-34
//    public void testSameLocalTransaction_SameEntityTwice() throws Throwable
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }

    @Test(enabled = true)
    public void testSameJTATransaction_DifferentEntitiesTwice() throws Throwable
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

            A ca0 = cache.get(new CacheQuery<A>(A.class, "s", "alice"));
            A ca1 = cache.get(new CacheQuery<A>(A.class, "s", "anna", "i", 31));

            Long id0 = ca0.getId();
            assert id0 != null;
            assert "alice".equals(ca0.getS());

            Long id1 = ca1.getId();
            assert id1 != null;
            assert "anna".equals(ca1.getS());

            // run the same query again, same transaction, we should get the same object instance

            A ca2 = cache.get(new CacheQuery<A>(A.class, "s", "alice"));
            A ca3 = cache.get(new CacheQuery<A>(A.class, "s", "anna", "i", 31));

            assert ca0 == ca2;
            assert ca1 == ca3;

            s.getTransaction().commit();

            A ca4 = cache.get(new CacheQuery<A>(A.class, "s", "alice"));
            A ca5 = cache.get(new CacheQuery<A>(A.class, "s", "anna", "i", 31));

            assert ca4 == ca0;
            assert ca5 == ca1;

            s.close();
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
    public void testTwoCaches() throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(D.class);
        config.addAnnotatedClass(E.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            s = sf.openSession();

            WriteOnceCache<D> dcache = new WriteOnceCache<D>(sf);
            WriteOnceCache<E> ecache = new WriteOnceCache<E>(sf);

            s.beginTransaction();

            E e = ecache.get(new CacheQuery<E>(E.class, "i", 10));

            s.getTransaction().commit();

            s.beginTransaction();
            
            D d = dcache.get(new CacheQuery<D>(D.class, "e", e));

            s.getTransaction().commit();

            assert e == d.getE();

            s.close();
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
    public void testKeyWithMultipleProperties() throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(G.class);
        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();
            s = sf.openSession();

            WriteOnceCache<G> cache = new WriteOnceCache<G>(sf);

            s.beginTransaction();
            G gone = cache.get(new CacheQuery<G>(G.class, "s1", "blah"));
            s.getTransaction().commit();

            assert gone.getId() != null;
            assert "blah".equals(gone.getS1());
            assert gone.getS2() == null;
            assert gone.getS3() == null;

            s.beginTransaction();
            G gtwo = cache.get(new CacheQuery<G>(G.class, "s1", "blah", "s2", "clah"));
            s.getTransaction().commit();

            assert !gtwo.getId().equals(gone.getId());
            assert "blah".equals(gtwo.getS1());
            assert "clah".equals(gtwo.getS2());
            assert gtwo.getS3() == null;


            // hit the cache again, outside a transaction

            G ggone = cache.get(new CacheQuery<G>(G.class, "s1", "blah"));
            G ggtwo = cache.get(new CacheQuery<G>(G.class, "s1", "blah", "s2", "clah"));

            assert ggone.getId().equals(gone.getId());
            assert "blah".equals(ggone.getS1());
            assert ggone.getS2() == null;
            assert ggone.getS3() == null;

            assert ggtwo.getId().equals(gtwo.getId());
            assert "blah".equals(ggtwo.getS1());
            assert "clah".equals(ggtwo.getS2());
            assert ggtwo.getS3() == null;
        }
        finally
        {
            if (s != null)
            {
                s.close();
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
