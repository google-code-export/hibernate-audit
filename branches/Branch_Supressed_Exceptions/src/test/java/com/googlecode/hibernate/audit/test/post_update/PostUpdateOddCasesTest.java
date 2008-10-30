package com.googlecode.hibernate.audit.test.post_update;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_update.data.CBi;
import com.googlecode.hibernate.audit.test.post_update.data.DBi;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityReferenceDelta;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.CollectionDelta;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.Set;

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
public class PostUpdateOddCasesTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostUpdateOddCasesTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Primitive Updates ---------------------------------------------------------------------------

    /**
     * https://jira.novaordis.org/browse/HBA-155
     */
    @Test(enabled = true)
    public void testPostUpdate_ManyToOneRelationshipCleared() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(CBi.class);
        config.addAnnotatedClass(DBi.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            CBi c = new CBi();
            DBi d = new DBi();
            d.setC(c);

            s.save(d);

            s.getTransaction().commit();

            s.beginTransaction();

            d.setC(null);

            s.update(d);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(d.getId());
            assert txs.size() == 2;

            TransactionDelta td = HibernateAudit.getDelta(txs.get(0).getId());
            assert td.getEntityDeltas().size() == 2;

            EntityDelta ed = td.getEntityDelta(c.getId(), CBi.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            Set<PrimitiveDelta> pds = ed.getPrimitiveDeltas();
            assert pds.isEmpty();
            Set<EntityReferenceDelta> erds = ed.getEntityReferenceDeltas();
            assert erds.isEmpty();
            Set<CollectionDelta> cds = ed.getCollectionDeltas();
            assert cds.isEmpty();

            ed = td.getEntityDelta(d.getId(), DBi.class.getName());
            assert ChangeType.INSERT.equals(ed.getChangeType());
            pds = ed.getPrimitiveDeltas();
            assert pds.isEmpty();
            erds = ed.getEntityReferenceDeltas();
            assert erds.size() == 1;
            EntityReferenceDelta erd = erds.iterator().next();
            assert c.getId().equals(erd.getId());
            assert CBi.class.equals(erd.getEntityClass());
            cds = ed.getCollectionDeltas();
            assert cds.isEmpty();

            td = HibernateAudit.getDelta(txs.get(1).getId());
            assert td.getEntityDeltas().size() == 1;

            ed = td.getEntityDelta(d.getId(), DBi.class.getName());
            assert ChangeType.UPDATE.equals(ed.getChangeType());
            pds = ed.getPrimitiveDeltas();
            assert pds.isEmpty();
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
