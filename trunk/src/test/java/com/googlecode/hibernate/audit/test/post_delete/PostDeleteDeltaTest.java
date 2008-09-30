package com.googlecode.hibernate.audit.test.post_delete;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_delete.data.CUni;
import com.googlecode.hibernate.audit.test.post_delete.data.DUni;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.CollectionDelta;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.ArrayList;

/**
 * This is a collection of various "post-delete" use cases of leaving an audit trail and then
 * extracting the deltas from that audit trail via the API.
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
public class PostDeleteDeltaTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostDeleteDeltaTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testPostDelete() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(CUni.class);
        config.addAnnotatedClass(DUni.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            CUni c = new CUni();
            s.save(c);
            s.getTransaction().commit();

            s.beginTransaction();

            s.delete(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(c.getId());
            assert txs.size() == 2;
            AuditTransaction tx = txs.get(1);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
            assert td.getEntityDeltas().size() == 1;

            EntityDelta d = td.getEntityDeltas().iterator().next();

            assert c.getId().equals(d.getId());
            assert CUni.class.getName().equals(d.getEntityName());
            assert ChangeType.DELETE.equals(d.getChangeType());

            assert d.getCollectionDeltas().isEmpty();
            assert d.getScalarDeltas().isEmpty();
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
    public void testPostDelete_Collection() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(CUni.class);
        config.addAnnotatedClass(DUni.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            CUni c = new CUni();
            DUni d = new DUni();
            d.setI(8);
            d.setS("se");
            c.setDs(new ArrayList<DUni>());
            c.getDs().add(d);

            s.save(c);
            s.getTransaction().commit();

            s.beginTransaction();

            c.getDs().remove(d);
            s.delete(d);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(c.getId());
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());
            for(EntityDelta ed: td.getEntityDeltas())
            {
                log.debug(">>>>> " + ed);
                // TODO this seem to fail from time to time, see https://jira.novaordis.org/browse/HBA-110
                assert ChangeType.INSERT.equals(ed.getChangeType());
            }

            td = HibernateAudit.getDelta(txs.get(1).getId());
            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(c.getId(), CUni.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            assert ed.getScalarDeltas().isEmpty();
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("ds");
            assert DUni.class.getName().equals(cd.getMemberEntityName());
            assert cd.getIds().isEmpty();

            ed = td.getEntityDelta(d.getId(), DUni.class.getName());
            assert ChangeType.DELETE.equals(ed.getChangeType());
            assert ed.getCollectionDeltas().isEmpty();
            assert ed.getScalarDeltas().isEmpty();
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
