package com.googlecode.hibernate.audit.test.post_delete;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_delete.data.CUni;
import com.googlecode.hibernate.audit.test.post_delete.data.DUni;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditCollectionType;

import java.util.List;
import java.util.ArrayList;

/**
 * A set of post-delete tests that look directly into the database and check raw deltas.
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
public class PostDeleteRawTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

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

            Long id = c.getId();

            s.beginTransaction();

            s.delete(c);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(c.getId());
            assert txs.size() == 2;

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);

            assert tx.equals(ae.getTransaction());
            assert ChangeType.DELETE.equals(ae.getType());
            assert id.equals(ae.getTargetId());

            AuditEntityType aet = (AuditEntityType)ae.getTargetType();
            assert Long.class.equals(aet.getIdClassInstance());
            assert CUni.class.equals(aet.getClassInstance());

            List pairs =
                HibernateAudit.query("from AuditEventPair as p where p.event = :event", ae);

            assert pairs.isEmpty();
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

            Long id = d.getId();

            s.beginTransaction();

            s.delete(d);
            c.getDs().remove(d);

            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions(c.getId());
            assert txs.size() == 2;

            AuditTransaction tx = txs.get(1);

            List events =
                HibernateAudit.query("from AuditEvent as e where e.transaction = :tx", tx);

            assert events.size() == 2;

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;
                assert tx.equals(ae.getTransaction());
                ChangeType ct = ae.getType();
                List pairs =
                    HibernateAudit.query("from AuditEventPair as p where p.event = :event", ae);
                
                if (ChangeType.DELETE.equals(ct))
                {
                    assert id.equals(ae.getTargetId());
                    AuditEntityType aet = (AuditEntityType)ae.getTargetType();
                    assert Long.class.equals(aet.getIdClassInstance());
                    assert DUni.class.equals(aet.getClassInstance());
                    assert pairs.isEmpty();
                }
                else if (ChangeType.UPDATE.equals(ct))
                {
                    assert c.getId().equals(ae.getTargetId());
                    AuditEntityType aet = (AuditEntityType)ae.getTargetType();
                    assert Long.class.equals(aet.getIdClassInstance());
                    assert CUni.class.equals(aet.getClassInstance());
                    assert pairs.size() == 1;

                    AuditEventCollectionPair p = (AuditEventCollectionPair)pairs.get(0);

                    assert ae.equals(p.getEvent());
                    AuditTypeField f = p.getField();
                    assert "ds".equals(f.getName());
                    AuditCollectionType act = (AuditCollectionType)f.getType();
                    assert DUni.class.equals(act.getClassInstance());
                    assert List.class.equals(act.getCollectionClassInstance());

                    List ids = (List)p.getValue();
                    assert ids.isEmpty();
                }
                else
                {
                    throw new Error("invalid state");
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
