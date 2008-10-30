package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.F;
import com.googlecode.hibernate.audit.test.post_insert.data.G;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
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
public class PostInsertComponentTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertComponentTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

//    TODO https://jira.novaordis.org/browse/HBA-32
//    @Test(enabled = true)
//    public void testSimpleEmbedded() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(F.class);
//        config.addAnnotatedClass(G.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            F f = new F();
//            f.setI(10);
//            f.setS("ferdinand");
//
//            G g = new G();
//            g.setI(11);
//            g.setS("gia");
//
//            f.setG(g);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            s.save(f);
//
//            s.getTransaction().commit();
//
//            log.debug(f);
//
//            List<AuditTransaction> txs = HibernateAudit.getTransactions();
//            assert txs.size() == 1;
//            AuditTransaction tx = txs.get(0);
//
//            TransactionDelta td = HibernateAudit.getDelta(tx.getId());
//            assert td.getEntityDeltas().size() == 1; // TODO https://jira.novaordis.org/browse/HBA-32
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
}
