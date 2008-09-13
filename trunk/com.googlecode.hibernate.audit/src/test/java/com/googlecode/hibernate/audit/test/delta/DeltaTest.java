package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.delta.data.A;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.ScalarDelta;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.Set;
import java.util.Date;
import java.util.Collection;

/**
 * Micellaneous delta function tests, that do not fit under PostInsertDeltaTest,
 * PostUpdateDeltaTest, etc.
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
public class DeltaTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testGetDelta_NoSuchTransaction_WithoutAppTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            assert null == HibernateAudit.getDelta(new Long(323478628));
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
    public void testGetDelta_NoSuchTransaction_WithAppTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Session s = sf.openSession();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);
            s.beginTransaction();

            assert null == HibernateAudit.getDelta(new Long(323478629));

            s.getTransaction().commit();
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
    public void testGetDelta_NoPrimitives() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Session s = sf.openSession();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);
            Date t1 = new Date();
            s.beginTransaction();

            A a = new A();
            s.save(a);

            s.getTransaction().commit();

            Date t2 = new Date();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 1;

            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert tx.getId().equals(td.getId());
            assert null == td.getLogicalGroupId();
            assert null == td.getUser();

            Date timestamp = td.getTimestamp();
            assert timestamp.getTime() >= floorTime(t1.getTime());
            assert timestamp.getTime() <= t2.getTime();

            Set<EntityDelta> eds = td.getEntityDeltas();

            assert eds.size() == 1;

            EntityDelta ed = eds.iterator().next();

            assert a.getId().equals(ed.getId());
            assert ed.getPrimitiveDeltas().isEmpty();
            assert ed.getCollectionDeltas().isEmpty();
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
    public void testGetDelta_SomePrimitives() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Session s = sf.openSession();
            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);
            s.beginTransaction();

            A a = new A();
            a.setName("alice");
            a.setAge(30);
            a.setDate(new Date(1000)); // anything less than a second is lost
            s.save(a);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(null);
            assert txs.size() == 1;

            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert tx.getId().equals(td.getId());
            assert null == td.getLogicalGroupId();
            assert null != td.getTimestamp();
            assert null == td.getUser();

            Set<EntityDelta> eds = td.getEntityDeltas();

            assert eds.size() == 1;

            EntityDelta ed = eds.iterator().next();

            assert a.getId().equals(ed.getId());
            assert ed.getCollectionDeltas().isEmpty();

            Collection<ScalarDelta> pds = ed.getPrimitiveDeltas();
            assert pds.size() == 3;

            for(ScalarDelta pd: pds)
            {
                String name = pd.getName();
                if ("name".equals(name))
                {
                    assert String.class.equals(pd.getType());
                    assert "alice".equals(pd.getValue());
                }
                else if ("age".equals(name))
                {
                    assert Integer.class.equals(pd.getType());
                    assert new Integer(30).equals(pd.getValue());
                }
                else if ("date".equals(name))
                {
                    assert Date.class.equals(pd.getType());
                    assert new Date(1000).equals(pd.getValue());
                }
                else
                {
                    throw new Error("unexpected primitive name " + name);
                }
            }
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
