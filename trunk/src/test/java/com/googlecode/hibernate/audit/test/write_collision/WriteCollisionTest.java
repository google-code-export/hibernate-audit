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
import com.googlecode.hibernate.audit.model.AuditTransaction;

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
     *
     * @throws Exception
     */
    @Test(enabled = true)
    public void testCoarseWriteCollisionDetection() throws Throwable
    {
        log.debug("testCoarseWriteCollisionDetection");

        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(Root.class);
        config.addAnnotatedClass(Shared.class);
        config.addAnnotatedClass(A.class);

        final SessionFactoryImplementor sf =
            (SessionFactoryImplementor)config.buildSessionFactory();

        try
        {

            System.setProperty("hba.show_sql", "false");
            HibernateAudit.startRuntime(sf.getSettings());
            RootIdProvider rip = new RootIdProvider(Root.class);
            HibernateAudit.register(sf, rip);

            // write intial data in database

            Session s = sf.openSession();
            s.beginTransaction();

            Root root = new Root();
            rip.setRoot(root);
            root.setS("initial root");
            Shared shared = new Shared();
            shared.setS("initial shared");
            A a = new A();
            a.setS("initial a");
            root.getAs().add(a);
            a.setRoot(root);
            root.setShared(shared);

            s.save(root);

            final Long rootId = root.getId();

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            assert rootId.equals(txs.get(0).getLogicalGroupId());

            // read/write data in two separated transactions, on two separate threads

            final RendezVous rendezVous = new RendezVous("THREAD1", "THREAD2");

            log.debug("\n\nstarting parallel threads\n");

            new Thread(new Runnable()
            {
                public void run()
                {
                    new TwoTransactionsUpdater(rootId, sf, rendezVous).update();
                }
            }, "THREAD1").start();

            new Thread(new Runnable()
            {
                public void run()
                {
                    new TwoTransactionsUpdater(rootId, sf, rendezVous).update();
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

    // Inner classes -------------------------------------------------------------------------------

    private class TwoTransactionsUpdater
    {
        private Long rootId;
        private RendezVous rendezVous;
        private SessionFactory sf;

        TwoTransactionsUpdater(Long rootId, SessionFactory sf, RendezVous rendezVous)
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

                log.debug(Thread.currentThread().getName() + " reading state from database");

                Session s  = sf.openSession();
                s.beginTransaction();

                Root root = (Root)s.get(Root.class, rootId);
                Long changeId = HibernateAudit.getLatestTransactionsByLogicalGroup(rootId).getId();
                root.getAs().get(0); // trigger lazy load

                s.getTransaction().commit();
                s.close();

                rendezVous.swapControl();

                log.debug(Thread.currentThread().getName() + " modifying state in memory");

                root.getAs().get(0).setS("a modified by " + Thread.currentThread().getName());

                rendezVous.swapControl();

                log.debug(Thread.currentThread().getName() +
                          " attempting to write the database in another transaction");

                s  = sf.openSession();
                s.beginTransaction();

                Long crtChangeId = HibernateAudit.
                    getLatestTransactionsByLogicalGroup(rootId).getId();

                if (crtChangeId.equals(changeId))
                {
                    // ok to write
                    s.update(root);
                    s.getTransaction().commit();
                    s.close();
                }
                else
                {
                    s.getTransaction().rollback();
                    s.close();

                    throw new HibernateAuditException(
                        "Write collision detected, current chage id is " + crtChangeId +
                        ", original change id is " + changeId);
                }

                rendezVous.end();
            }
            catch(Throwable t)
            {
                rendezVous.abort(t);
            }
        }
    }

}
