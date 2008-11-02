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
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.RootIdProvider;
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
    public void testCoarseWriteCollisionDetection() throws Exception
    {
        log.debug("testCoarseWriteCollisionDetection");

        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(Root.class);
        config.addAnnotatedClass(Shared.class);
        config.addAnnotatedClass(A.class);

        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
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

            Long rootId = root.getId();

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
            assert rootId.equals(txs.get(0).getLogicalGroupId());

            // read data in a separated transaction

            s  = sf.openSession();
            s.beginTransaction();
            root = (Root)s.get(Root.class, rootId);

            AuditTransaction atx = HibernateAudit.getLatestTransactionsByLogicalGroup(rootId);
            Long changeId = atx.getId();

            assert "initial root".equals(root.getS());
            assert "initial shared".equals(root.getShared().getS());

            List<A> as = root.getAs();
            assert as.size() == 1;
            assert "initial a".equals(as.get(0).getS());
            assert root == as.get(0).getRoot();

            s.getTransaction().commit();
            s.close();

            throw new RuntimeException("NOT FINISHED");
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
