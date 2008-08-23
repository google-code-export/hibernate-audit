package com.googlecode.hibernate.audit.test.post_update;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.event.EventSource;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_update.data.A;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.LogicalGroupIdProvider;

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
public class PostUpdateTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostUpdateTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testPostUpdate_UsingLogicalGroupId() throws Exception
    {
        throw new Exception("NOT YET IMPLEMENTED");
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        config.addAnnotatedClass(B.class);
//        SessionFactory sf = null;
//
//        try
//        {
//            sf = config.buildSessionFactory();
//            LogicalGroupIdProviderImpl lgip = new LogicalGroupIdProviderImpl();
//            HibernateAudit.enable(sf, lgip);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            A a = new A();
//            lgip.setGroupLead(a);
//
//            B b = new B();
//            b.setS("ben");
//
//            a.getBs().add(b);
//            b.setA(a);
//
//            s.save(a);
//
//            s.getTransaction().commit();
//
//            Long aId = a.getId();
//
//            s.beginTransaction();
//
//            b.setS("bob");
//            s.update(a);
//
//            s.getTransaction().commit();
//
//            // our custom LGIP does uses aId as group id.
//            List<AuditTransaction> txs = HibernateAudit.getTransactionsByLogicalGroup(aId);
//
//            assert txs.size() == 2;
//
//            // recreate the current state applying 'insert' and 'update' deltas
//
//            A copy = new A();
//
//            HibernateAudit.delta(copy, aId, txs.get(0).getId());
//
//            List<Temp> deltas = HibernateAudit.getDelta(txs.get(1).getId());
//
//            log.debug(copy);
//        }
//        finally
//        {
//            HibernateAudit.disableAll();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class LogicalGroupIdProviderImpl implements LogicalGroupIdProvider
    {
        private A groupLead;

        public Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity)
        {
            return groupLead.getId();
        }

        void setGroupLead(A a)
        {
            groupLead = a;
        }
    }
}
