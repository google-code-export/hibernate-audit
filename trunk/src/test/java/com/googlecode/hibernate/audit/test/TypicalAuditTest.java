package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.data.A;
import com.googlecode.hibernate.audit.test.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.EntityReferenceDelta;
import com.googlecode.hibernate.audit.delta.CollectionDelta;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.Set;
import java.util.Collection;
import java.io.Serializable;

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
public class TypicalAuditTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(TypicalAuditTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void test() throws Exception
    {
        log.debug("test");

        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            B b = new B();
            a.getBs().add(b);

            s.save(a);

            s.getTransaction().commit();

            s.beginTransaction();

            a.setS("sa");
            b.setS("sb");

            s.update(a);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(a.getId());
            assert txs.size() == 2;


            // first transaction

            AuditTransaction tx = txs.get(0);
            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(a.getId(), A.class.getName());

            assert ChangeType.INSERT.equals(ed.getChangeType());

            Set<PrimitiveDelta> pds = ed.getPrimitiveDeltas();
            assert pds.isEmpty();

            Set<EntityReferenceDelta> erds = ed.getEntityReferenceDeltas();
            assert erds.isEmpty();

            Set<CollectionDelta> cds = ed.getCollectionDeltas();
            assert cds.size() == 1;

            CollectionDelta cd = cds.iterator().next();
            Collection<Serializable> ids = cd.getIds();
            assert ids.size() == 1;
            assert ids.contains(b.getId());

            ed = td.getEntityDelta(b.getId(), B.class.getName());

            assert ChangeType.INSERT.equals(ed.getChangeType());

            pds = ed.getPrimitiveDeltas();
            assert pds.isEmpty();

            erds = ed.getEntityReferenceDeltas();
            assert erds.isEmpty();

            cds = ed.getCollectionDeltas();
            assert cds.isEmpty();

            // second transaction

            tx = txs.get(1);
            td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 2;

            ed = td.getEntityDelta(a.getId(), A.class.getName());

            assert ChangeType.UPDATE.equals(ed.getChangeType());

            pds = ed.getPrimitiveDeltas();
            assert pds.size() == 1;

            PrimitiveDelta pd = pds.iterator().next();
            assert "s".equals(pd.getName());
            assert "sa".equals(pd.getValue());

            erds = ed.getEntityReferenceDeltas();
            assert erds.isEmpty();

            cds = ed.getCollectionDeltas();
            assert cds.isEmpty();

            ed = td.getEntityDelta(b.getId(), B.class.getName());

            assert ChangeType.UPDATE.equals(ed.getChangeType());

            pds = ed.getPrimitiveDeltas();
            assert pds.size() == 1;

            pd = pds.iterator().next();
            assert "s".equals(pd.getName());
            assert "sb".equals(pd.getValue());

            erds = ed.getEntityReferenceDeltas();
            assert erds.isEmpty();

            cds = ed.getCollectionDeltas();
            assert cds.isEmpty();
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
