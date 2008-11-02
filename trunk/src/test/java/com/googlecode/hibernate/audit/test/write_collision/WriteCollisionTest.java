package com.googlecode.hibernate.audit.test.write_collision;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.write_collision.data.A;
import com.googlecode.hibernate.audit.test.write_collision.data.Root;
import com.googlecode.hibernate.audit.test.write_collision.data.Shared;
import com.googlecode.hibernate.audit.test.util.RendezVous;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.RootIdProvider;
import com.googlecode.hibernate.audit.HibernateAuditException;

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
public class WriteCollisionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteCollisionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Primitive Updates ---------------------------------------------------------------------------

    /**
     * Detecting write collision if anything under root has changed.
     */
    @Test(enabled = false)
    public void testCoarseWriteCollisionDetection() throws Throwable
    {
        log.debug("testCoarseWriteCollisionDetection");

        AnnotationConfiguration conf = new AnnotationConfiguration();
        conf.configure(getHibernateConfigurationFileName());
        conf.getProperties().setProperty("hibernate.show_sql", "false");
        conf.addAnnotatedClass(Root.class);
        conf.addAnnotatedClass(Shared.class);
        conf.addAnnotatedClass(A.class);

        final SessionFactoryImplementor sf = (SessionFactoryImplementor)conf.buildSessionFactory();

        try
        {
            System.setProperty("hba.show_sql", "false");

            HibernateAudit.startRuntime(sf.getSettings());
            RootIdProvider rip = new RootIdProvider(Root.class);
            HibernateAudit.register(sf, rip);

            // write intial data in database

            final Long rootId = init(sf, rip);

            // read/write data in two separated transactions, on two separate threads

            final RendezVous rendezVous = new RendezVous("THREAD1", "THREAD2");

            log.debug("\n\nstarting parallel threads\n");

            new Thread(new Runnable()
            {
                public void run()
                {
                    new TwoPhaseUpdater(rootId, sf, rendezVous).update();
                }
            }, "THREAD1").start();

            new Thread(new Runnable()
            {
                public void run()
                {
                    new TwoPhaseUpdater(rootId, sf, rendezVous).update();
                }
            }, "THREAD2").start();

            rendezVous.awaitEnd();

            assert null == rendezVous.getThrowable("THREAD1");
            Throwable t = rendezVous.getThrowable("THREAD2");
            assert t instanceof HibernateAuditException;
            log.debug(">>>>> " + t.getMessage());
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
    public void testFinelyGrainedWriteCollisionDetection() throws Throwable
    {
        AnnotationConfiguration conf = new AnnotationConfiguration();
        conf.configure(getHibernateConfigurationFileName());
        conf.getProperties().setProperty("hibernate.show_sql", "false");
        conf.addAnnotatedClass(Root.class);
        conf.addAnnotatedClass(Shared.class);
        conf.addAnnotatedClass(A.class);

        final SessionFactoryImplementor sf = (SessionFactoryImplementor)conf.buildSessionFactory();

        try
        {
            System.setProperty("hba.show_sql", "false");

            HibernateAudit.startRuntime(sf.getSettings());
            RootIdProvider rip = new RootIdProvider(Root.class);
            HibernateAudit.register(sf, rip);

            // write intial data in database

            final Long rootId = init(sf, rip);

            // read/write data in two separated transactions, on two separate threads

            final RendezVous rendezVous = new RendezVous("THREAD1", "THREAD2");

            log.debug("\n\nstarting parallel threads\n");

            new Thread(new Runnable()
            {
                public void run()
                {
                    new TwoPhaseUpdater(rootId, sf, rendezVous).update();
                }
            }, "THREAD1").start();

            new Thread(new Runnable()
            {
                public void run()
                {
                    new TwoPhaseUpdater(rootId, sf, rendezVous).update();
                }
            }, "THREAD2").start();

            rendezVous.awaitEnd();

            assert null == rendezVous.getThrowable("THREAD1");
            Throwable t = rendezVous.getThrowable("THREAD2");
            assert t instanceof HibernateAuditException;
            log.debug(">>>>> " + t.getMessage());
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

    /**
     * @return the root's id
     */
    private Long init(SessionFactory sf, RootIdProvider rip) throws Exception
    {
        Session s = sf.openSession();
        s.beginTransaction();

        Root root = new Root();
        rip.setRoot(root);
        root.setS("r");
        Shared shared = new Shared();
        shared.setS("s");
        A a = new A();
        a.setS("a");
        root.getAs().add(a);
        a.setRoot(root);
        root.setShared(shared);

        s.save(root);

        s.getTransaction().commit();
        s.close();

        return root.getId();
    }

    // Inner classes -------------------------------------------------------------------------------

    private class TwoPhaseUpdater
    {
        private Long rootId;
        private RendezVous rendezVous;
        private SessionFactory sf;

        TwoPhaseUpdater(Long rootId, SessionFactory sf, RendezVous rendezVous)
        {
            this.rootId = rootId;
            this.sf = sf;
            this.rendezVous = rendezVous;
        }

        public void update()
        {
            try
            {
                rendezVous.begin();

                VersionedEntity<Root> ve = read(rootId);

                rendezVous.swapControl();

                Root root = ve.getEntity();
                root.getAs().get(0).setS("a modified by " + Thread.currentThread().getName());
                
                log.debug(Thread.currentThread().getName() +
                          " modified state in memory, root.a.s = " + root.getAs().get(0).getS());

                rendezVous.swapControl();

                write(ve);

                rendezVous.end();
            }
            catch(Throwable t)
            {
                rendezVous.abort(t);
            }
        }

        /**
         * @return a detached root and its version.
         */
        private VersionedEntity<Root> read(Long id) throws Exception
        {
            String tn = Thread.currentThread().getName();
            log.debug(tn + " reading root from database");

            Session s  = sf.openSession();
            s.beginTransaction();

            Root root = (Root)s.get(Root.class, id);
            Long changeId = HibernateAudit.getLatestTransactionsByLogicalGroup(rootId).getId();
            root.getAs().get(0); // trigger lazy load

            s.getTransaction().commit();
            s.close();

            return new VersionedEntity<Root>(root, changeId);
        }

        /**
         * @throws HibernateAuditException on write conflict.
         */
        private void write(VersionedEntity<Root> versionedRoot) throws Exception
        {
            String tn = Thread.currentThread().getName();
            log.debug(tn + " attempting to write the database in another transaction");

            Root root = versionedRoot.getEntity();
            Long version = versionedRoot.getVersion();

            Session s  = null;

            try
            {
                s = sf.openSession();
                s.beginTransaction();

                Long lastChangeId = HibernateAudit.
                    getLatestTransactionsByLogicalGroup(root.getId()).getId();

                if (lastChangeId.equals(version))
                {
                    // ok to write
                    s.update(versionedRoot.getEntity());
                    s.getTransaction().commit();
                    log.debug(tn + " committed successfully");
                    return;
                }

                // write conflict
                s.getTransaction().rollback();
                log.debug(tn + " rolled back");

                throw new HibernateAuditException(
                    "write collision detected, current chage id is " + lastChangeId +
                    ", original change id is " + version);
            }
            finally
            {
                s.close();
            }
        }
    }
}
