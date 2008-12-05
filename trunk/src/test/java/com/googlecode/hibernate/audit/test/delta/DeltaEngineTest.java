package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.RandomType;
import com.googlecode.hibernate.audit.test.delta.data.A;
import com.googlecode.hibernate.audit.test.delta.data.B;
import com.googlecode.hibernate.audit.test.delta.data.D;
import com.googlecode.hibernate.audit.test.delta.data.C;
import com.googlecode.hibernate.audit.delta_deprecated.DeltaEngine;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.RootProvider;
import com.googlecode.hibernate.audit.LogicalGroupImpl;
import com.googlecode.hibernate.audit.LogicalGroup;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDelta;
import com.googlecode.hibernate.audit.delta.CollectionDelta;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.util.ArrayList;

/**
 * Tests the runtime API
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
public class DeltaEngineTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DeltaEngineTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testNoSuchEntity() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            RandomType o = new RandomType();

            try
            {
                DeltaEngine.delta(o, null, null, (SessionFactoryImplementor)sf,
                                  HibernateAudit.getManager().getSessionFactory());
                throw new Error("should've failed");
            }
            catch(MappingException e)
            {
                log.info(e.getMessage());
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

    @Test(enabled = true)
    public void testInvalidId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            A a = new A();

            try
            {
                DeltaEngine.delta(a, null, null, (SessionFactoryImplementor)sf,
                                  HibernateAudit.getManager().getSessionFactory());
                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
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

    @Test(enabled = true)
    public void testNoSuchAuditTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            A a = new A();
            a.setId(new Long(0));

            try
            {
                DeltaEngine.delta(a, null, new Long(23843431223l),
                                  (SessionFactoryImplementor)sf,
                                  HibernateAudit.getManager().getSessionFactory());

                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
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

    @Test(enabled = true)
    public void testNoSuchTypeAudited() throws Exception
    {
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

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();
            s.close();

            // entity is in the database and it's 'audited' as well

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            B toFillUp = new B();
            toFillUp.setId(new Long(0));

            try
            {
                DeltaEngine.delta(toFillUp, null, at.getId(),
                                  (SessionFactoryImplementor)sf,
                                  HibernateAudit.getManager().getSessionFactory());

                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
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

    @Test(enabled = true)
    public void testNoSuchEntityIdInDatabase() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();
            s.close();

            // entity is in the database and it's 'audited' as well

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            A toFillUp = new A();
            toFillUp.setId(a.getId().longValue() + 157);

            try
            {
                DeltaEngine.delta(toFillUp, null, at.getId(), (SessionFactoryImplementor)sf,
                                  HibernateAudit.getManager().getSessionFactory());
                throw new Error("should've failed");
            }
            catch(IllegalArgumentException e)
            {
                log.info(e.getMessage());
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

    @Test(enabled = true)
    public void testForwardDelta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            A a = new A();
            a.setName("alice");
            a.setAge(33);

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();
            s.close();

            Long id = a.getId();

            // entity is in the database and it's 'audited' as well

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            A base = new A();

            DeltaEngine.delta(base, null, id, at.getId(),(SessionFactoryImplementor)sf,
                              HibernateAudit.getManager().getSessionFactory());

            assert id.equals(base.getId());

            assert "alice".equals(base.getName());

            assert 33 == base.getAge();
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
    public void testDelta_ProtectedConstructor() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(ProtectedConstructorC.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            ProtectedConstructorC c = ProtectedConstructorC.getInstance();
            c.setName("cami");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(c);

            s.getTransaction().commit();
            s.close();

            Long id = c.getId();

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            ProtectedConstructorC base = ProtectedConstructorC.getInstance();

            DeltaEngine.delta(base, null, id, at.getId(), (SessionFactoryImplementor)sf,
                              HibernateAudit.getManager().getSessionFactory());

            assert id.equals(base.getId());

            assert "cami".equals(base.getName());

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

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-107
//    public void testGetDelta_NoSuchAuditTransaction() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            A a = new A();
//
//            s.save(a);
//            s.getTransaction().commit();
//
//            assert null == DeltaEngine.getDelta(new Long(23843431223l), null,
//                                                HibernateAudit.getManager().getSessionFactory());
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
//
//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-107
//    public void testGetDelta_NoMatchingLogicalGroupId() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf);
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            A a = new A();
//
//            s.save(a);
//            s.getTransaction().commit();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);
//
//            assert transactions.size() == 1;
//
//            assert null == DeltaEngine.getDelta(transactions.get(0).getId(), new Long(5),
//                                                HibernateAudit.getManager().getSessionFactory());
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
//
//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-107
//    public void testGetDelta_NoMatchingLogicalGroupId2() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf, new LogicalGroupProvider()
//            {
//                public Serializable getLogicalGroup(EventSource es,
//                                                      Serializable id,
//                                                      Object entity)
//                {
//                    return new Long(2);
//                }
//            });
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            A a = new A();
//
//            s.save(a);
//            s.getTransaction().commit();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);
//
//            assert transactions.size() == 1;
//
//            assert null == DeltaEngine.getDelta(transactions.get(0).getId(), new Long(5),
//                                                HibernateAudit.getManager().getSessionFactory());
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
//
//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-107
//    public void testGetDelta_MatchingLogicalGroupId() throws Exception
//    {
//        AnnotationConfiguration config = new AnnotationConfiguration();
//        config.configure(getHibernateConfigurationFileName());
//        config.addAnnotatedClass(A.class);
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//
//            HibernateAudit.startRuntime(sf.getSettings());
//            HibernateAudit.register(sf, new LogicalGroupProvider()
//            {
//                public Serializable getLogicalGroup(EventSource es,
//                                                      Serializable id,
//                                                      Object entity)
//                {
//                    return new Long(44);
//                }
//            });
//
//            Session s = sf.openSession();
//            s.beginTransaction();
//
//            A a = new A();
//
//            s.save(a);
//            s.getTransaction().commit();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);
//
//            assert transactions.size() == 1;
//
//            DeltaDeprecated d = DeltaEngine.getDelta(transactions.get(0).getId(), new Long(44),
//                                           HibernateAudit.getManager().getSessionFactory());
//
//            assert d != null;
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

    @Test(enabled = true)
    public void testGetDelta_Complex() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            C c = new C();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf, new RootProvider(c));

            Session s = sf.openSession();
            s.beginTransaction();

            c.setS("cex");
            c.setI(7);

            D done = new D();
            done.setS("done");
            done.setI(8);
            D dtwo = new D();
            dtwo.setS("dtwo");
            dtwo.setI(9);

            List<D> ds = new ArrayList<D>();
            ds.add(done);
            ds.add(dtwo);
            c.setDs(ds);

            s.save(c);
            s.getTransaction().commit();

            LogicalGroup clg = new LogicalGroupImpl(c.getId(), C.class.getName());
            List<AuditTransaction> txs = HibernateAudit.getTransactionsByLogicalGroup(clg);
            assert txs.size() == 1;
            AuditTransaction tx = txs.get(0);

            TransactionDelta td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 3;

            EntityDelta ed = null;
            ed = td.getEntityDelta(c.getId(), C.class.getName());
            assert ed.getScalarDeltas().size() == 2;
            assert "cex".equals(ed.getPrimitiveDelta("s").getValue());
            assert new Integer(7).equals(ed.getPrimitiveDelta("i").getValue());
            assert ed.getCollectionDeltas().size() == 1;
            CollectionDelta cd = ed.getCollectionDelta("ds");
            assert D.class.getName().equals(cd.getMemberEntityName());
            assert cd.getIds().size() == 2;
            assert cd.getIds().contains(done.getId());
            assert cd.getIds().contains(dtwo.getId());

            ed = td.getEntityDelta(done.getId(), D.class.getName());
            assert ed.getScalarDeltas().size() == 2;
            assert "done".equals(ed.getPrimitiveDelta("s").getValue());
            assert new Integer(8).equals(ed.getPrimitiveDelta("i").getValue());
            assert ed.getCollectionDeltas().isEmpty();

            ed = td.getEntityDelta(dtwo.getId(), D.class.getName());
            assert ed.getScalarDeltas().size() == 2;
            assert "dtwo".equals(ed.getPrimitiveDelta("s").getValue());
            assert new Integer(9).equals(ed.getPrimitiveDelta("i").getValue());
            assert ed.getCollectionDeltas().isEmpty();

            s.beginTransaction();

            c.setS("cex2");
            c.setI(17);

            done.setS("done2");
            done.setI(18);
            dtwo.setS("dtwo2");
            dtwo.setI(19);

            s.update(c);

            s.getTransaction().commit();

            txs = HibernateAudit.getTransactionsByLogicalGroup(clg);
            assert txs.size() == 2;
            tx = txs.get(1);

            td = HibernateAudit.getDelta(tx.getId());

            assert td.getEntityDeltas().size() == 3;

            ed = td.getEntityDelta(c.getId(), C.class.getName());
            assert ed.getScalarDeltas().size() == 2;
            assert "cex2".equals(ed.getPrimitiveDelta("s").getValue());
            assert new Integer(17).equals(ed.getPrimitiveDelta("i").getValue());
            assert ed.getCollectionDeltas().isEmpty();

            ed = td.getEntityDelta(done.getId(), D.class.getName());
            assert ed.getScalarDeltas().size() == 2;
            assert "done2".equals(ed.getPrimitiveDelta("s").getValue());
            assert new Integer(18).equals(ed.getPrimitiveDelta("i").getValue());
            assert ed.getCollectionDeltas().isEmpty();

            ed = td.getEntityDelta(dtwo.getId(), D.class.getName());
            assert ed.getScalarDeltas().size() == 2;
            assert "dtwo2".equals(ed.getPrimitiveDelta("s").getValue());
            assert new Integer(19).equals(ed.getPrimitiveDelta("i").getValue());
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

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
