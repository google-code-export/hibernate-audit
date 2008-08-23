package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.MappingException;
import org.hibernate.Session;
import org.hibernate.event.EventSource;
import org.hibernate.engine.SessionFactoryImplementor;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.RandomType;
import com.googlecode.hibernate.audit.delta.DeltaEngine;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.Delta;
import com.googlecode.hibernate.audit.delta.Change;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.LogicalGroupIdProvider;

import java.util.List;
import java.io.Serializable;

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

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
            HibernateAudit.disableAll();
            
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testGetDelta_NoSuchAuditTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();

            s.save(a);
            s.getTransaction().commit();

            assert null == DeltaEngine.getDelta(new Long(23843431223l), null,
                                                HibernateAudit.getManager().getSessionFactory());
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testGetDelta_NoMatchingLogicalGroupId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();

            s.save(a);
            s.getTransaction().commit();

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);
            
            assert transactions.size() == 1;

            assert null == DeltaEngine.getDelta(transactions.get(0).getId(), new Long(5),
                                                HibernateAudit.getManager().getSessionFactory());
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testGetDelta_NoMatchingLogicalGroupId2() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf, new LogicalGroupIdProvider()
            {
                public Serializable getLogicalGroupId(EventSource es, 
                                                      Serializable id,
                                                      Object entity)
                {
                    return new Long(2);
                }
            });

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();

            s.save(a);
            s.getTransaction().commit();

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);

            assert transactions.size() == 1;

            assert null == DeltaEngine.getDelta(transactions.get(0).getId(), new Long(5),
                                                HibernateAudit.getManager().getSessionFactory());
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testGetDelta_MatchingLogicalGroupId() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf, new LogicalGroupIdProvider()
            {
                public Serializable getLogicalGroupId(EventSource es,
                                                      Serializable id,
                                                      Object entity)
                {
                    return new Long(44);
                }
            });

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();

            s.save(a);
            s.getTransaction().commit();

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);

            assert transactions.size() == 1;

            Delta d = DeltaEngine.getDelta(transactions.get(0).getId(), new Long(44),
                                           HibernateAudit.getManager().getSessionFactory());

            assert d != null;
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = false) // TEST_OFF
    public void testGetDelta_InsertSimpleEntity() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();

            s.save(a);
            s.getTransaction().commit();

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);

            assert transactions.size() == 1;

            Delta d = DeltaEngine.getDelta(transactions.get(0).getId(), null,
                                           HibernateAudit.getManager().getSessionFactory());

            assert d != null;

            throw new Exception("INCOMPLETE TEST");
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testGetDelta_UpdateSimpleEntity() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            A a = new A();

            s.save(a);
            s.getTransaction().commit();

            s.beginTransaction();

            a.setName("alice");
            a.setAge(30);

            s.update(a);

            s.getTransaction().commit();

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(null);

            assert transactions.size() == 2;

            Delta d = DeltaEngine.getDelta(transactions.get(1).getId(), null,
                                           HibernateAudit.getManager().getSessionFactory());

            assert d != null;

            List<Change> chages = d.getChanges();

            log.debug(chages);
        }
        finally
        {
            HibernateAudit.disableAll();

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
