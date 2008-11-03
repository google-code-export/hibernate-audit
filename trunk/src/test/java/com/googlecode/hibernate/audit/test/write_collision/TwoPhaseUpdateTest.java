package com.googlecode.hibernate.audit.test.write_collision;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.EntityMode;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.write_collision.data.Root;
import com.googlecode.hibernate.audit.test.write_collision.data.Shared;
import com.googlecode.hibernate.audit.test.write_collision.data.A;
import com.googlecode.hibernate.audit.test.write_collision.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.RootIdProvider;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.util.reflections.HibernateReflections;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.Set;

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
public class TwoPhaseUpdateTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(TwoPhaseUpdateTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Primitive Updates ---------------------------------------------------------------------------

    @Test(enabled = true)
    public void testTwoPhaseUpdate() throws Throwable
    {
        log.debug("test");

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

            Long rootId = root.getId();

            // read in memory

            s  = sf.openSession();
            s.beginTransaction();

            root = (Root)s.get(Root.class, rootId);
            root.getShared();
            root.getAs().get(0);
            root.getBs().get(0);

            s.getTransaction().commit();
            s.close();

            // modify in memory

            root.getAs().get(0).setS("a2");
            root.getBs().get(0).setS("b2");
            root.getShared().setS("s2");
            root.setS("r2");

            // read again in memory

            s  = sf.openSession();
            s.beginTransaction();

            Root dbRoot = (Root)s.get(Root.class, rootId);
            dbRoot.getShared();
            dbRoot.getAs().get(0);
            dbRoot.getBs().get(0);

            HibernateReflections.applyChanges(sf, EntityMode.POJO, dbRoot, root);

            s.update(dbRoot);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(1).getId());
            Set<EntityDelta> eds = td.getEntityDeltas();

            assert eds.size() == 1;

            EntityDelta ed = eds.iterator().next();

            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert dbRoot.getId().equals(ed.getId());

            assert ed.getEntityReferenceDeltas().isEmpty();
            assert ed.getCollectionDeltas().isEmpty();

            Set<PrimitiveDelta> pds = ed.getPrimitiveDeltas();
            assert pds.size() == 1;

            PrimitiveDelta pd = pds.iterator().next();
            assert "s".equals(pd.getName());
            assert "r2".equals(pd.getValue());
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
