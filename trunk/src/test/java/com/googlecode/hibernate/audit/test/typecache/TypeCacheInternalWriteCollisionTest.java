package com.googlecode.hibernate.audit.test.typecache;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.impl.SessionImpl;
import org.hibernate.impl.SessionFactoryImpl;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.typecache.data.A;
import com.googlecode.hibernate.audit.test.typecache.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.model.AuditTypeField;

import java.util.concurrent.Exchanger;
import java.util.concurrent.CountDownLatch;
import java.util.Random;
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
public class TypeCacheInternalWriteCollisionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(TypeCacheInternalWriteCollisionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // transaction wait before committing in ms
    private long sleepTime = 5000;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testEnclosingJTATransaction_ConcurrentWriteCollision_PrimitiveType()
        throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImpl sf = null;
        Session s = null;

        try
        {
            sf = (SessionFactoryImpl)config.buildSessionFactory();
            final SessionFactory sameSf = sf;

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            final TypeCache typeCache = HibernateAudit.getManager().getTypeCache();

            final Exchanger<Object> exchanger = new Exchanger<Object>();
            final CountDownLatch step1 = new CountDownLatch(1);
            final CountDownLatch step2 = new CountDownLatch(1);

            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Session s = (SessionImpl)sameSf.openSession();

                        log.debug("beginning transaction ...");
                        s.beginTransaction();

                        AuditType pt = typeCache.getAuditPrimitiveType(Random.class);

                        assert Random.class.equals(pt.getClassInstance());

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
                            exchanger.exchange(pt);
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

            // signal the other thread it can start sleeping/committing
            step2.countDown();

            // write the database from the main thread, this will block until the other transaction
            // commits and releases the locks
            log.debug("trying to get from cache ....");

            AuditType pt = typeCache.getAuditPrimitiveType(Random.class);

            assert Random.class.equals(pt.getClassInstance());

            log.debug("commiting ...");
            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // test instance identity
            assert result == pt;

            // make sure there's only one in the database
            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            s = isf.openSession();
            s.beginTransaction();

            Query q = s.createQuery("from AuditType as t where t.className = :className");
            q.setString("className", Random.class.getName());
            List r = q.list();

            s.getTransaction().commit();

            assert r.size() == 1;
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
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testEnclosingJTATransaction_ConcurrentWriteCollision_EntityType()
        throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImpl sf = null;
        Session s = null;

        try
        {
            sf = (SessionFactoryImpl)config.buildSessionFactory();
            final SessionFactory sameSf = sf;

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            final TypeCache typeCache = HibernateAudit.getManager().getTypeCache();

            final Exchanger<Object> exchanger = new Exchanger<Object>();
            final CountDownLatch step1 = new CountDownLatch(1);
            final CountDownLatch step2 = new CountDownLatch(1);

            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Session s = (SessionImpl)sameSf.openSession();

                        log.debug("beginning transaction ...");
                        s.beginTransaction();

                        AuditEntityType et = typeCache.getAuditEntityType(Long.class, A.class);

                        assert A.class.equals(et.getClassInstance());
                        assert Long.class.equals(et.getIdClassInstance());

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
                            exchanger.exchange(et);
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

            // signal the other thread it can start sleeping/committing
            step2.countDown();

            // write the database from the main thread, this will block until the other transaction
            // commits and releases the locks
            log.debug("trying to get from cache ....");

            AuditEntityType et = typeCache.getAuditEntityType(Long.class, A.class);

            assert A.class.equals(et.getClassInstance());
            assert Long.class.equals(et.getIdClassInstance());

            log.debug("commiting ...");
            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // test instance identity
            assert result == et;

            // make sure there's only one in the database
            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            s = isf.openSession();
            s.beginTransaction();

            Query q = s.createQuery("from AuditEntityType");
            List r = q.list();

            s.getTransaction().commit();

            assert r.size() == 1;
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
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testEnclosingJTATransaction_ConcurrentWriteCollision_CollectionType()
        throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImpl sf = null;
        Session s = null;

        try
        {
            sf = (SessionFactoryImpl)config.buildSessionFactory();
            final SessionFactory sameSf = sf;

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            final TypeCache typeCache = HibernateAudit.getManager().getTypeCache();

            final Exchanger<Object> exchanger = new Exchanger<Object>();
            final CountDownLatch step1 = new CountDownLatch(1);
            final CountDownLatch step2 = new CountDownLatch(1);

            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Session s = (SessionImpl)sameSf.openSession();

                        log.debug("beginning transaction ...");
                        s.beginTransaction();

                        AuditCollectionType ct = typeCache.
                            getAuditCollectionType(List.class, A.class);

                        assert A.class.equals(ct.getClassInstance());
                        assert List.class.equals(ct.getCollectionClassInstance());

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
                            exchanger.exchange(ct);
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

            // signal the other thread it can start sleeping/committing
            step2.countDown();

            // write the database from the main thread, this will block until the other transaction
            // commits and releases the locks
            log.debug("trying to get from cache ....");

            AuditCollectionType ct = typeCache.getAuditCollectionType(List.class, A.class);

            assert A.class.equals(ct.getClassInstance());
            assert List.class.equals(ct.getCollectionClassInstance());

            log.debug("commiting ...");
            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // test instance identity
            assert result == ct;

            // make sure there's only one in the database
            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            s = isf.openSession();
            s.beginTransaction();

            Query q = s.createQuery("from AuditCollectionType");
            List r = q.list();

            s.getTransaction().commit();

            assert r.size() == 1;
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
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testEnclosingJTATransaction_ConcurrentWriteCollision_Field_ExistingType()
        throws Throwable
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImpl sf = null;
        Session s = null;

        try
        {
            sf = (SessionFactoryImpl)config.buildSessionFactory();
            final SessionFactory sameSf = sf;

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            final TypeCache typeCache = HibernateAudit.getManager().getTypeCache();

            final Exchanger<Object> exchanger = new Exchanger<Object>();
            final CountDownLatch step1 = new CountDownLatch(1);
            final CountDownLatch step2 = new CountDownLatch(1);

            // enter the type in cache

            s = (SessionImpl)sameSf.openSession();
            s.beginTransaction();
            final AuditEntityType et = typeCache.getAuditEntityType(Long.class, A.class);
            assert A.class.equals(et.getClassInstance());
            assert Long.class.equals(et.getIdClassInstance());
            s.getTransaction().commit();
            s.close();

            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        Session s = (SessionImpl)sameSf.openSession();

                        log.debug("beginning transaction ...");
                        s.beginTransaction();

                        AuditTypeField f = typeCache.getAuditTypeField("s", et);
                        assert "s".equals(f.getName());
                        assert et == f.getType();

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
                            exchanger.exchange(f);
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

            // signal the other thread it can start sleeping/committing
            step2.countDown();

            // write the database from the main thread, this will block until the other transaction
            // commits and releases the locks
            log.debug("trying to get from cache ....");

            AuditTypeField f = typeCache.getAuditTypeField("s", et);
            assert "s".equals(f.getName());
            assert et == f.getType();

            log.debug("commiting ...");
            s.getTransaction().commit();

            Object result = exchanger.exchange(null);
            if (result instanceof Throwable)
            {
                throw (Throwable)result;
            }

            // test instance identity
            assert result == f;

            // make sure there's only one in the database
            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            s = isf.openSession();
            s.beginTransaction();

            Query q = s.createQuery("from AuditTypeField");
            List r = q.list();

            s.getTransaction().commit();

            assert r.size() == 1;
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
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-189
//    public void testEnclosingJTATransaction_ConcurrentWriteCollision_TypeAndField()
//        throws Throwable
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        config.addAnnotatedClass(B.class);
//        SessionFactoryImpl sf = null;
//        Session s = null;
//
//        try
//        {
//            sf = (SessionFactoryImpl)config.buildSessionFactory();
//            final SessionFactory sameSf = sf;
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            final TypeCache typeCache = HibernateAudit.getManager().getTypeCache();
//
//            final Exchanger<Object> exchanger = new Exchanger<Object>();
//            final CountDownLatch step1 = new CountDownLatch(1);
//            final CountDownLatch step2 = new CountDownLatch(1);
//
//            new Thread(new Runnable()
//            {
//                public void run()
//                {
//                    try
//                    {
//                        Session s = (SessionImpl)sameSf.openSession();
//
//                        log.debug("beginning transaction ...");
//                        s.beginTransaction();
//
//                        AuditEntityType et = typeCache.getAuditEntityType(Long.class, A.class);
//                        AuditTypeField f = typeCache.getAuditTypeField("s", et);
//
//                        assert A.class.equals(et.getClassInstance());
//                        assert Long.class.equals(et.getIdClassInstance());
//                        assert "s".equals(f.getName());
//                        assert et == f.getType();
//
//                        // tell the main thread that the database was written, but not committed
//                        // from the parallel transaction
//                        step1.countDown();
//                        step2.await();
//
//                        // need to sleep and then commit, if main thread's notification will never
//                        // come because it's blocked trying to acquire a lock held by this
//                        // transaction
//
//                        log.debug("\n\nsleeping for " + sleepTime + " ms ...\n\n");
//                        Thread.sleep(sleepTime);
//                        log.debug("done sleeping, committing ...");
//
//                        s.getTransaction().commit();
//                        s.close();
//
//                        try
//                        {
//                            exchanger.exchange(f);
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
//                            exchanger.exchange(t);
//                        }
//                        catch(InterruptedException e)
//                        {
//                            // ignore
//                        }
//                    }
//                }
//            }, "ThreadOne").start();
//
//            // wait for the signal that the database was written, but not committed, from
//            // the parallel thread
//            step1.await();
//
//            s = sf.openSession();
//            s.beginTransaction();
//
//            // signal the other thread it can start sleeping/committing
//            step2.countDown();
//
//            // write the database from the main thread, this will block until the other transaction
//            // commits and releases the locks
//            log.debug("trying to get from cache ....");
//
//            AuditEntityType et = typeCache.getAuditEntityType(Long.class, A.class);
//            AuditTypeField f = typeCache.getAuditTypeField("s", et);
//
//            assert A.class.equals(et.getClassInstance());
//            assert Long.class.equals(et.getIdClassInstance());
//            assert "s".equals(f.getName());
//            assert et == f.getType();
//
//            log.debug("commiting ...");
//            s.getTransaction().commit();
//
//            Object result = exchanger.exchange(null);
//            if (result instanceof Throwable)
//            {
//                throw (Throwable)result;
//            }
//
//            // test instance identity
//            assert result == f;
//
//            // make sure there's only one in the database
//            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
//            s = isf.openSession();
//            s.beginTransaction();
//
//            Query q = s.createQuery("from AuditEntityType");
//            List r = q.list();
//
//            s.getTransaction().commit();
//
//            assert r.size() == 1;
//        }
//        catch(Exception e)
//        {
//            if (s != null && s.getTransaction() != null)
//            {
//                s.getTransaction().rollback();
//            }
//
//            throw new Error("failure", e);
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
//    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}