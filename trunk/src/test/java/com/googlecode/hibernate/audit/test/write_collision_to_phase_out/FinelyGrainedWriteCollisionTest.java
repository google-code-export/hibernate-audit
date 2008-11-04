package com.googlecode.hibernate.audit.test.write_collision_to_phase_out;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.write_collision_to_phase_out.data.Root;
import com.googlecode.hibernate.audit.test.write_collision_to_phase_out.data.Shared;
import com.googlecode.hibernate.audit.test.write_collision_to_phase_out.data.A;
import com.googlecode.hibernate.audit.test.write_collision_to_phase_out.data.B;
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
public class FinelyGrainedWriteCollisionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(FinelyGrainedWriteCollisionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

//    @Test(enabled = true) // TODO temporarily commented out, failing
//    public void testIndependentUpdatesNoCollision() throws Throwable
//    {
//        AnnotationConfiguration conf = new AnnotationConfiguration();
//        conf.configure(getHibernateConfigurationFileName());
//        conf.getProperties().setProperty("hibernate.show_sql", "false");
//        conf.addAnnotatedClass(Root.class);
//        conf.addAnnotatedClass(Shared.class);
//        conf.addAnnotatedClass(A.class);
//        conf.addAnnotatedClass(B.class);
//
//        final SessionFactoryImplementor sf = (SessionFactoryImplementor)conf.buildSessionFactory();
//
//        try
//        {
//            System.setProperty("hba.show_sql", "false");
//            HibernateAudit.startRuntime(sf.getSettings());
//            RootIdProvider rip = new RootIdProvider(Root.class);
//            HibernateAudit.register(sf, rip);
//
//            // write intial data in database
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//            Root root = new Root();
//            rip.setRoot(root);
//            root.setS("r");
//            Shared shared = new Shared();
//            shared.setS("s");
//            A a = new A();
//            a.setS("a");
//            root.getAs().add(a);
//            B b = new B();
//            b.setS("b");
//            root.getBs().add(b);
//            a.setRoot(root);
//            b.setRoot(root);
//            root.setShared(shared);
//            s.save(root);
//            s.getTransaction().commit();
//            s.close();
//
//            final Long rootId = root.getId();
//
//            // read/write data in two separated transactions, on two separate threads
//
//            final RendezVous rendezVous = new RendezVous("THREAD1", "THREAD2");
//
//            final ModificationStrategy strategy = new ModificationStrategy()
//            {
//                public void modify(Root root) throws Exception
//                {
//                    String tn = Thread.currentThread().getName();
//
//                    if ("THREAD1".equals(tn))
//                    {
//                        // modifying As
//                        root.getAs().get(0).setS("a2");
//                        log.debug(tn + " modified state in memory, root.a.s = " +
//                                  root.getAs().get(0).getS());
//                    }
//                    else
//                    {
//                        // modifying Bs
//                        root.getBs().get(0).setS("b2");
//                        log.debug(tn + " modified state in memory, root.b.s = " +
//                                  root.getBs().get(0).getS());
//                    }
//                }
//            };
//
//            log.debug("");
//            log.debug("  starting parallel threads");
//            log.debug("");
//
//            new Thread(new Runnable()
//            {
//                public void run()
//                {
//                    new TwoPhaseVersionedUpdater(rootId, sf, rendezVous, strategy).update();
//                }
//            }, "THREAD1").start();
//
//            new Thread(new Runnable()
//            {
//                public void run()
//                {
//                    new TwoPhaseVersionedUpdater(rootId, sf, rendezVous, strategy).update();
//                }
//            }, "THREAD2").start();
//
//            rendezVous.awaitEnd();
//
//            assert null == rendezVous.getThrowable("THREAD1");
//            assert null == rendezVous.getThrowable("THREAD2");
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


//    @Test(enabled = true) // TODO temporary disabled, failing
//    public void testCollision() throws Throwable
//    {
//        AnnotationConfiguration conf = new AnnotationConfiguration();
//        conf.configure(getHibernateConfigurationFileName());
//        conf.getProperties().setProperty("hibernate.show_sql", "false");
//        conf.addAnnotatedClass(Root.class);
//        conf.addAnnotatedClass(Shared.class);
//        conf.addAnnotatedClass(A.class);
//        conf.addAnnotatedClass(B.class);
//
//        final SessionFactoryImplementor sf = (SessionFactoryImplementor)conf.buildSessionFactory();
//
//        try
//        {
//            System.setProperty("hba.show_sql", "false");
//            HibernateAudit.startRuntime(sf.getSettings());
//            RootIdProvider rip = new RootIdProvider(Root.class);
//            HibernateAudit.register(sf, rip);
//
//            // write intial data in database
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//            Root root = new Root();
//            rip.setRoot(root);
//            root.setS("r");
//            Shared shared = new Shared();
//            shared.setS("s");
//            A a = new A();
//            a.setS("a");
//            root.getAs().add(a);
//            B b = new B();
//            b.setS("b");
//            root.getBs().add(b);
//            a.setRoot(root);
//            b.setRoot(root);
//            root.setShared(shared);
//            s.save(root);
//            s.getTransaction().commit();
//            s.close();
//
//            final Long rootId = root.getId();
//
//            // read/write data in two separated transactions, on two separate threads
//
//            //final RendezVous rendezVous = new RendezVous("THREAD1", "THREAD2");
//            final RendezVous rendezVous = new RendezVous("THREAD1");
//
//            final ModificationStrategy strategy = new ModificationStrategy()
//            {
//                public void modify(Root root) throws Exception
//                {
//                    String tn = Thread.currentThread().getName();
//
//                    // both threads modifying As
//                    root.getAs().get(0).setS("a" + tn.charAt(tn.length() - 1));
//
//                    log.debug(tn + " has modified state in memory, root.a.s = " +
//                              root.getAs().get(0).getS());
//                }
//            };
//
//            log.debug("");
//            log.debug("  starting parallel threads");
//            log.debug("");
//
//            new Thread(new Runnable()
//            {
//                public void run()
//                {
//                    new TwoPhaseVersionedUpdater(rootId, sf, rendezVous, strategy).update();
//                }
//            }, "THREAD1").start();
//
////            new Thread(new Runnable()
////            {
////                public void run()
////                {
////                    new TwoPhaseVersionedUpdater(rootId, sf, rendezVous, strategy).update();
////                }
////            }, "THREAD2").start();
//
//            rendezVous.awaitEnd();
//
//            assert null == rendezVous.getThrowable("THREAD1");
//
//            Throwable t = rendezVous.getThrowable("THREAD2");
//            assert t instanceof HibernateAuditException;
//            log.debug(">>> " + t.getMessage());
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

    private class TwoPhaseVersionedUpdater
    {
        private Long rootId;
        private RendezVous rendezVous;
        private SessionFactoryImplementor sf;
        private ModificationStrategy strategy;

        TwoPhaseVersionedUpdater(Long rootId, SessionFactoryImplementor sf,
                                 RendezVous rendezVous, ModificationStrategy strategy)
        {
            this.sf = sf;
            this.rootId = rootId;
            this.rendezVous = rendezVous;
            this.strategy = strategy;
        }

        public void update()
        {
            String tn = Thread.currentThread().getName();

            try
            {
                rendezVous.begin();

                // read data in memory

                Session s = sf.openSession();
                s.beginTransaction();

                Root root = (Root)s.get(Root.class, rootId);
                // kick in lazy loading
                root.getShared();
                root.getAs().get(0);
                root.getBs().get(0);

                Long initVersion = HibernateAudit.getLatestTransactionForLogicalGroup(rootId).getId();

                s.getTransaction().commit();
                s.close();

                log.debug(tn + " has read root in memory, current version " + initVersion);

                rendezVous.swapControl();

                // modify data in memory

                strategy.modify(root);

                rendezVous.swapControl();

                // read again and compare

                s = sf.openSession();
                s.beginTransaction();
                
                Root dbroot = (Root)s.get(Root.class, rootId);
                // kick in lazy loading
                dbroot.getShared();
                dbroot.getAs().get(0);
                dbroot.getBs().get(0);

                Long dbVersion = HibernateAudit.getLatestTransactionForLogicalGroup(rootId).getId();

                log.debug(tn + " has read root in memory again, version " + dbVersion);

                Root.applyChanges(dbroot, root);

                // write in the database

                s.save(dbroot);
                s.getTransaction().commit();

                log.debug(tn + " has committed");

                rendezVous.end();
            }
            catch(Throwable t)
            {
                log.debug("thread failure", t);
                rendezVous.abort(t);
            }
        }
    }
}
