package com.googlecode.hibernate.audit.test.logical_group_id;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.event.EventSource;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.HibernateAuditTest;
import com.googlecode.hibernate.audit.test.logical_group_id.data.A;
import com.googlecode.hibernate.audit.test.logical_group_id.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.LogicalGroupProvider;
import com.googlecode.hibernate.audit.LogicalGroup;
import com.googlecode.hibernate.audit.LogicalGroupImpl;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.io.Serializable;
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
public class MultipleLogicalGroupsPerTransactionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAuditTest.class);

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

            LogicalGroupProvider lgip = new LogicalGroupProvider()
            {
                public LogicalGroup getLogicalGroup(EventSource es,
                                                    Serializable id,
                                                    Object entity)
                {
                    if (entity instanceof A)
                    {
                        return new LogicalGroupImpl(new Long(77), A.class.getName());
                    }
                    else if (entity instanceof B)
                    {
                        return new LogicalGroupImpl(new Long(88), B.class.getName());
                    }

                    throw new IllegalStateException("don't know the entity " + entity);
                }
            };
            HibernateAudit.register(sf, lgip);

            Session s = sf.openSession();
            s.beginTransaction();
            A a = new A();
            a.setName("something");
            s.save(a);

            B b = new B();
            b.setName("something else");
            s.save(b);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;

            TransactionDelta txd = HibernateAudit.getDelta(txs.get(0).getId());

            Set<EntityDelta> eds = txd.getEntityDeltas();
            assert eds.size() == 2;

            for(EntityDelta d: eds)
            {
                if (d.getId().equals(a.getId()))
                {
                    LogicalGroup lg = d.getLogicalGroup();
                    assert new Long(77).equals(lg.getId());
                    assert A.class.getName().equals(lg.getType());
                }
                else if (d.getId().equals(b.getId()))
                {
                    LogicalGroup lg = d.getLogicalGroup();
                    assert new Long(88).equals(lg.getId());
                    assert B.class.getName().equals(lg.getType());
                }
                else
                {
                    throw new Error("didn't expect this entity delta");
                }
            }

            txs = HibernateAudit.getTransactionsByLogicalGroup(new Long(23482347239l));
            assert txs.isEmpty();

            txs = HibernateAudit.getTransactionsByLogicalGroup(new Long(77));
            assert txs.size() == 1;

            txs = HibernateAudit.getTransactionsByLogicalGroup(new Long(88));
            assert txs.size() == 1;
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
