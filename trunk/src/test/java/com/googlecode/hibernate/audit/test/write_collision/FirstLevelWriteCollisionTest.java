package com.googlecode.hibernate.audit.test.write_collision;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.write_collision.data.Root;
import com.googlecode.hibernate.audit.test.write_collision.data.Shared;
import com.googlecode.hibernate.audit.test.write_collision.data.A;
import com.googlecode.hibernate.audit.test.write_collision.data.B;
import com.googlecode.hibernate.audit.test.util.RendezVous;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.RootIdProvider;
import com.googlecode.hibernate.audit.HibernateAuditException;

/**
 * TODO this test doesn't exercise HBA functionality, is part of an experiment, break out in a
 *      different module!
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class FirstLevelWriteCollisionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(FirstLevelWriteCollisionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Primitive Updates ---------------------------------------------------------------------------

    @Test(enabled = true)
    public void test() throws Throwable
    {
        AnnotationConfiguration conf = new AnnotationConfiguration();
        conf.configure(getHibernateConfigurationFileName());
        conf.getProperties().setProperty("hibernate.show_sql", "false");
        conf.addAnnotatedClass(Root.class);
        conf.addAnnotatedClass(Shared.class);
        conf.addAnnotatedClass(A.class);
        conf.addAnnotatedClass(B.class);

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
                    new TwoPhaseVersionedUpdater(rootId, sf, rendezVous).update();
                }
            }, "THREAD1").start();

            new Thread(new Runnable()
            {
                public void run()
                {
                    new TwoPhaseVersionedUpdater(rootId, sf, rendezVous).update();
                }
            }, "THREAD2").start();

            rendezVous.awaitEnd();

            assert null == rendezVous.getThrowable("THREAD1");
            assert null == rendezVous.getThrowable("THREAD2");
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
        B b = new B();
        b.setS("b");
        root.getBs().add(b);
        a.setRoot(root);
        b.setRoot(root);
        root.setShared(shared);

        s.save(root);

        s.getTransaction().commit();
        s.close();

        return root.getId();
    }

    // Inner classes -------------------------------------------------------------------------------

    private class TwoPhaseVersionedUpdater
    {
        private Long rootId;
        private RendezVous rendezVous;
        private SessionFactory sf;

        TwoPhaseVersionedUpdater(Long rootId, SessionFactory sf, RendezVous rendezVous)
        {
            this.rootId = rootId;
            this.sf = sf;
            this.rendezVous = rendezVous;
        }

        public void update()
        {
            String tn = Thread.currentThread().getName();

            try
            {
                rendezVous.begin();

                Versions v = read(rootId);

                rendezVous.swapControl();

                Root root = (Root)v.getEntity(Root.class, rootId);

                if ("THREAD1".equals(tn))
                {
                    // modifying As

                    root.getAs().get(0).setS("a2");
                    log.debug(tn + " modified state in memory, root.a.s = " + root.getAs().get(0).getS());
                }
                else
                {
                    // modifying Bs
                    root.getBs().get(0).setS("b2");
                    log.debug(tn + " modified state in memory, root.b.s = " + root.getBs().get(0).getS());
                }

                rendezVous.swapControl();

                //write(ve);

                rendezVous.end();
            }
            catch(Throwable t)
            {
                log.debug("thread failure", t);
                rendezVous.abort(t);
            }
        }

        private Versions read(Long rootId) throws Exception
        {
            String tn = Thread.currentThread().getName();
            log.debug(tn + " reading root from database");

            Versions v = new Versions();

            Session s  = sf.openSession();
            s.beginTransaction();

            Root root = (Root)s.get(Root.class, rootId);
            Long changeId = HibernateAudit.getLatestTransactionByLogicalGroup(rootId).getId();

            v.put(Root.class, rootId, changeId);
//
//            Long sharedChangeId = HibernateAudit.get
//
//            root.getAs().get(0); // trigger lazy load
//            root.getBs().get(0); // trigger lazy load
//
//            s.getTransaction().commit();
//            s.close();
//
//            return new VersionedEntity<Root>(root, changeId);

            return v;
        }

        /**
         * @throws com.googlecode.hibernate.audit.HibernateAuditException on write conflict.
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
                    getLatestTransactionByLogicalGroup(root.getId()).getId();

                if (lastChangeId.equals(version))
                {
                    // nothing changed anywhere under root, ok to write; this is an optimization
                    s.update(versionedRoot.getEntity());
                    s.getTransaction().commit();
                    log.debug(tn + " committed successfully");
                    return;
                }

                // something changed, let's see if changes conflict

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
