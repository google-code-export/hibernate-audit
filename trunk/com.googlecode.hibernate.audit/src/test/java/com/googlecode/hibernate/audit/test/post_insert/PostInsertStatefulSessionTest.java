package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.engine.SessionFactoryImplementor;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.A;
import com.googlecode.hibernate.audit.test.post_insert.data.WB;
import com.googlecode.hibernate.audit.test.post_insert.data.WA;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditCollectionType;

import java.util.List;

/**
 * This is a set of extra post-insert tests, devised while refactoring from a stateless to a
 * stateful session (https://jira.novaordis.org/browse/HBA-72)
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
public class PostInsertStatefulSessionTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertStatefulSessionTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testSingleInsert() throws Exception
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

            Long aId = a.getId();
            
            List transactions = HibernateAudit.query("from AuditTransaction");
            assert transactions.size() == 1;
            AuditTransaction aTx = (AuditTransaction)transactions.get(0);

            List pairs = HibernateAudit.query("from AuditEventPair");
            assert pairs.size() == 1;
            AuditEventPair p = (AuditEventPair)pairs.get(0);

            assert "alice".equals(p.getValue());

            AuditTypeField f = p.getField();
            assert "name".equals(f.getName());
            AuditType t = f.getType();
            assert t.isPrimitiveType();
            assert String.class.equals(t.getClassInstance());

            AuditEvent e = p.getEvent();
            assert aTx.equals(e.getTransaction());
            assert aId.equals(e.getTargetId());
            assert ChangeType.INSERT.equals(e.getType());

            t = e.getTargetType();
            assert t.isEntityType();
            AuditEntityType et = (AuditEntityType)t;
            assert A.class.equals(et.getClassInstance());
            assert Long.class.equals(et.getIdClassInstance());
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testInsert_AlreadyExistingEntityType() throws Exception
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

            // insert the entity type
            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            Session is = isf.openSession();
            is.beginTransaction();

            AuditEntityType et = new AuditEntityType(Long.class, A.class);
            is.save(et);

            is.getTransaction().commit();
            is.close();

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();

            List events = HibernateAudit.query("from AuditEvent");
            assert events.size() == 1;
            AuditEvent ae = (AuditEvent)events.get(0);
            AuditEntityType persistedEt = (AuditEntityType)ae.getTargetType();

            assert et.equals(persistedEt);

            List types = HibernateAudit.query("from AuditType");
            assert 2 == types.size();

            assert types.remove(et);

            AuditType t = (AuditType)types.get(0);
            assert t.isPrimitiveType();
            assert String.class.equals(t.getClassInstance());
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testInsert_AlreadyExistingPrimitiveType() throws Exception
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

            // insert the primitive type
            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            Session is = isf.openSession();
            is.beginTransaction();

            AuditType t = new AuditType();
            t.setClassName(String.class.getName());
            is.save(t);

            is.getTransaction().commit();
            is.close();

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();

            List fields = HibernateAudit.query("from AuditTypeField");
            assert fields.size() == 1;
            AuditTypeField f = (AuditTypeField)fields.get(0);
            AuditType persistedT = f.getType();

            assert t.equals(persistedT);

            List types = HibernateAudit.query("from AuditType");
            assert 2 == types.size();

            assert types.remove(t);

            AuditEntityType et = (AuditEntityType)types.get(0);
            assert A.class.equals(et.getClassInstance());
            assert Long.class.equals(et.getIdClassInstance());
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testInsert_AlreadyExistingCollectionType() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(WA.class);
        config.addAnnotatedClass(WB.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // insert the entity type
            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            Session is = isf.openSession();
            is.beginTransaction();

            AuditCollectionType ct = new AuditCollectionType(List.class, WB.class);
            is.save(ct);

            is.getTransaction().commit();
            is.close();

            WA wa = new WA();
            WB wb = new WB();
            wb.setName("wbong");
            wa.getWbs().add(wb);

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(wa);

            s.getTransaction().commit();

            List events =
                HibernateAudit.query("from AuditEvent as ae where ae.targetId = ?", wa.getId());

            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);

            List pairs = HibernateAudit.query("from AuditEventPair as p where p.event = ?", ae);

            assert pairs.size() == 1;

            AuditEventPair p = (AuditEventPair)pairs.get(0);

            AuditType t = p.getField().getType();

            assert ct.equals(t);

            List types = HibernateAudit.query("from AuditType");
            assert types.size() == 4;

            assert types.remove(ct);

            for(Object o: types)
            {
                AuditType ts = (AuditType)o;
                assert ts.isEntityType() || ts.isPrimitiveType();
            }
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
