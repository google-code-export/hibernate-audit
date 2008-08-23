package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.C;
import com.googlecode.hibernate.audit.test.post_insert.data.D;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEventPair;

import java.util.List;
import java.util.Set;
import java.util.HashSet;

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
public class PostInsertEntityTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertEntityTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testSimpleCascade() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            C c = new C();
            c.setName("charlie");

            D d = new D();
            d.setName("diane");

            c.setD(d);

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(c);

            t.commit();

            Set<Long> expectedTargetIds = new HashSet<Long>();
            expectedTargetIds.add(c.getId());
            expectedTargetIds.add(d.getId());

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;

            AuditTransaction at = (AuditTransaction)transactions.get(0);

            List types = HibernateAudit.query("from AuditType");
            assert types.size() == 3;

            Set<AuditType> expectedTargetTypes = new HashSet<AuditType>();

            for(Object o: types)
            {
                AuditType type = (AuditType)o;

                if (type.isEntityType())
                {
                    expectedTargetTypes.add(type);
                }
                else if (String.class.equals(type.getClassInstance()))
                {
                    // ok
                }
                else
                {
                    throw new Error("unexpected type " + type);
                }
            }

            assert expectedTargetTypes.size() == 2;

            List events = HibernateAudit.query("from AuditEvent");
            assert events.size() == 2;

            for(Object o: events)
            {
                AuditEvent e = (AuditEvent)o;

                assert at.equals(e.getTransaction());
                assert ChangeType.INSERT.equals(e.getType());
                assert expectedTargetTypes.remove(e.getTargetType());
                assert expectedTargetIds.remove(e.getTargetId());
            }

            List pairs = HibernateAudit.query("from AuditEventPair");
            assert pairs.size() == 3;

            Set<String> expectedStringValues = new HashSet<String>();
            expectedStringValues.add(c.getName());
            expectedStringValues.add(d.getName());
            expectedStringValues.add(Long.toString(d.getId()));

            for(Object o: pairs)
            {
                AuditEventPair pair = (AuditEventPair)o;

                // TODO we're not testing pair.getValue() because at this time is not implemented
                assert expectedStringValues.remove(pair.getStringValue());
            }

            HibernateAudit.disableAll();
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testSimpleCascade_Delta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            C c = new C();
            c.setName("charlie");

            D d = new D();
            d.setName("diane");

            c.setD(d);

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(c);

            t.commit();

            Long cId = c.getId();
            Long dId = d.getId();

            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction at = (AuditTransaction)transactions.get(0);

            C cBase = new C();
            HibernateAudit.delta(cBase, cId, at.getId());

            assert cId.equals(cBase.getId());

            assert "charlie".equals(cBase.getName());

            D recreatedD = cBase.getD();
            assert recreatedD != d;
            assert dId.equals(recreatedD.getId());
            assert "diane".equals(recreatedD.getName());

            HibernateAudit.disableAll();
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testCascade_TwoTransactions_Delta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Session s = sf.openSession();

            // transaction one
            s.beginTransaction();

            D d = new D();
            d.setName("diane");

            s.save(d);

            Long dId = d.getId();

            s.getTransaction().commit();
            s.close();

            s = sf.openSession();

            // transaction two
            s.beginTransaction();

            // we do this to modify the order in which post-insert events are sent
            d = (D)s.get(D.class, dId);

            C c = new C();
            c.setName("charlie");
            c.setD(d);

            s.save(c);

            s.getTransaction().commit();

            Long cId = c.getId();

            List transactions = HibernateAudit.query("from AuditTransaction as a order by a.id");
            assert transactions.size() == 2;
            AuditTransaction at = (AuditTransaction)transactions.get(1);

            C cBase = new C();
            HibernateAudit.delta(cBase, cId, at.getId());

            assert cId.equals(cBase.getId());

            assert "charlie".equals(cBase.getName());

            D recreatedD = cBase.getD();
            assert recreatedD != d;
            assert dId.equals(recreatedD.getId());
            assert "diane".equals(recreatedD.getName());

            HibernateAudit.disableAll();
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testCascade_TwoTransactions_MultipleEntities_Delta() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(C.class);
        config.addAnnotatedClass(D.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Session s = sf.openSession();

            // transaction one
            s.beginTransaction();

            D d = new D();
            d.setName("diane");

            s.save(d);

            Long dId = d.getId();

            s.getTransaction().commit();
            s.close();

            s = sf.openSession();

            // transaction two
            s.beginTransaction();

            // we do this to modify the order in which post-insert events are sent
            d = (D)s.get(D.class, dId);

            C c = new C();
            c.setName("charlie");
            c.setD(d);

            C c2 = new C();
            c2.setName("connie");
            c2.setD(d);

            s.save(c);
            s.save(c2);

            s.getTransaction().commit();

            Long cId = c.getId();
            Long cId2 = c2.getId();

            List transactions = HibernateAudit.query("from AuditTransaction as a order by a.id");
            assert transactions.size() == 2;
            AuditTransaction at = (AuditTransaction)transactions.get(1);

            C cBase = new C();
            HibernateAudit.delta(cBase, cId, at.getId());

            assert cId.equals(cBase.getId());

            assert "charlie".equals(cBase.getName());

            D recreatedD = cBase.getD();
            assert recreatedD != d;
            assert dId.equals(recreatedD.getId());
            assert "diane".equals(recreatedD.getName());

            cBase = new C();
            HibernateAudit.delta(cBase, cId2, at.getId());

            assert cId2.equals(cBase.getId());

            assert "connie".equals(cBase.getName());

            recreatedD = cBase.getD();
            assert recreatedD != d;
            assert dId.equals(recreatedD.getId());
            assert "diane".equals(recreatedD.getName());

            HibernateAudit.disableAll();
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
        }
        finally
        {
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
