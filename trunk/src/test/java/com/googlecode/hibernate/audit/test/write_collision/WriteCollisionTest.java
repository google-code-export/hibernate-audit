package com.googlecode.hibernate.audit.test.write_collision;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
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

            final Long changeId = txs.get(0).getId();

            // read data in two separated transactions

            String threadOneName = "THREAD1";
            String threadTwoName = "THREAD2";

            final RendezVous rendezVous = new RendezVous(threadOneName, threadTwoName);


            log.debug("\n\nstarting parallel threads\n");

            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        rendezVous.begin();

                        log.debug("\n\nreading state from database\n");

                        Root rootInTxOne = null;

                        Session s  = sf.openSession();
                        s.beginTransaction();
                        rootInTxOne = (Root)s.get(Root.class, rootId);

                        AuditTransaction atx =
                            HibernateAudit.getLatestTransactionsByLogicalGroup(rootId);
                        assert changeId.equals(atx.getId());

                        assert "initial root".equals(rootInTxOne.getS());
                        assert "initial shared".equals(rootInTxOne.getShared().getS());

                        List<A> as = rootInTxOne.getAs();
                        assert as.size() == 1;
                        assert "initial a".equals(as.get(0).getS());
                        assert rootInTxOne == as.get(0).getRoot();

                        s.getTransaction().commit();
                        s.close();

                        rendezVous.swapControl();

                        log.debug("\n\nmodifying state in memory\n");

                        rootInTxOne.getAs().get(0).
                            setS("a modified by " + Thread.currentThread().getName());

                        rendezVous.swapControl();

                        log.debug("\n\nattempting to write the database in another transaction\n");

                        s  = sf.openSession();
                        s.beginTransaction();

                        atx = HibernateAudit.getLatestTransactionsByLogicalGroup(rootId);

                        if (atx.getId().equals(changeId))
                        {
                            // ok to write
                            s.update(rootInTxOne);
                        }
                        else
                        {
                            s.getTransaction().rollback();

                            throw new HibernateAuditException(
                                "Write collision detected, my chage id is " + atx.getId() +
                                ", database change id is " + changeId);
                        }

                        s.getTransaction().commit();
                        s.close();

                        rendezVous.end();
                    }
                    catch(Throwable t)
                    {
                        rendezVous.abort(t);
                    }
                }
            }, threadOneName).start();

            new Thread(new Runnable()
            {
                public void run()
                {
                    try
                    {
                        rendezVous.begin();

                        log.debug("\n\nreading state from database\n");

                        Root rootInTxOne = null;

                        Session s  = sf.openSession();
                        s.beginTransaction();
                        rootInTxOne = (Root)s.get(Root.class, rootId);

                        AuditTransaction atx =
                            HibernateAudit.getLatestTransactionsByLogicalGroup(rootId);
                        assert changeId.equals(atx.getId());

                        assert "initial root".equals(rootInTxOne.getS());
                        assert "initial shared".equals(rootInTxOne.getShared().getS());

                        List<A> as = rootInTxOne.getAs();
                        assert as.size() == 1;
                        assert "initial a".equals(as.get(0).getS());
                        assert rootInTxOne == as.get(0).getRoot();

                        s.getTransaction().commit();
                        s.close();

                        rendezVous.swapControl();

                        log.debug("\n\nmodifying state in memory\n");

                        rootInTxOne.getAs().get(0).
                            setS("a modified by " + Thread.currentThread().getName());

                        rendezVous.swapControl();

                        log.debug("\n\nattempting to write the database in another transaction\n");

                        s  = sf.openSession();
                        s.beginTransaction();

                        atx = HibernateAudit.getLatestTransactionsByLogicalGroup(rootId);

                        if (atx.getId() == changeId)
                        {
                            // ok to write
                            s.update(rootInTxOne);
                        }
                        else
                        {
                            s.getTransaction().rollback();

                            throw new HibernateAuditException(
                                "Write collision detected, my chage id is " + atx.getId() +
                                ", database change id is " + changeId);
                        }

                        s.getTransaction().commit();
                        s.close();

                        rendezVous.end();
                    }
                    catch(Throwable t)
                    {
                        rendezVous.abort(t);
                    }
                }
            }, threadTwoName).start();

            rendezVous.awaitEnd();

            assert null == rendezVous.getThrowable(threadOneName);

            Throwable t = rendezVous.getThrowable(threadTwoName);
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

}
