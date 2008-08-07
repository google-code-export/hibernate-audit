package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.Formats;
import com.googlecode.hibernate.audit.test.post_insert.data.A;
import com.googlecode.hibernate.audit.test.post_insert.data.B;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditTypeField;

import java.util.List;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

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
public class PostInsertTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testInsert_NullProperty() throws Exception
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

            // 'age' is null

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(a);

            t.commit();

            // make sure information was logged

            List transactions = HibernateAudit.query("from AuditTransaction");

            assert transactions.size() == 1;

            List events = HibernateAudit.query("from AuditEvent");

            assert transactions.size() == 1;

            AuditEvent ae = (AuditEvent)events.get(0);

            assert AuditEventType.INSERT.equals(ae.getType());
            assert transactions.remove(ae.getTransaction());

            AuditType type = ae.getTargetType();
            assert A.class.getName().equals(type.getClassName());
            assert a.getId().equals(ae.getTargetId());

            List pairs = HibernateAudit.query("from AuditEventPair as ap where ap.event = :event", ae);

            assert pairs.size() == 1;

            AuditEventPair pair = (AuditEventPair)pairs.get(0);
            assert "alice".equals(pair.getValue());

            AuditTypeField field = pair.getField();
            assert "name".equals(field.getName());

            type = field.getType();
            assert String.class.getName().equals(type.getClassName());
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testSingleInsert() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Date t1 = new Date();

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(a);

            t.commit();

            Date t2 = new Date();

            // make sure information was logged. Since we wipe out tables for each test, only one
            // audit transaction is expected to be found

            List ts = HibernateAudit.query("from AuditTransaction");

            assert ts.size() == 1;

            AuditTransaction at = (AuditTransaction)ts.get(0);

            assert at.getTimestamp().getTime() >= floorTime(t1.getTime());
            assert at.getTimestamp().getTime() <= t2.getTime();

            List es = HibernateAudit.query("from AuditEvent");

            assert ts.size() == 1;

            AuditEvent ae = (AuditEvent)es.get(0);

            assert AuditEventType.INSERT.equals(ae.getType());
            assert ts.remove(ae.getTransaction());

            AuditType aent = ae.getTargetType();
            assert A.class.getName().equals(aent.getClassName());
            assert a.getId().equals(ae.getTargetId());

            List pairs = HibernateAudit.query("from AuditEventPair as ap where ap.event = :event", ae);

            assert pairs.size() == 1;

            AuditEventPair pair = (AuditEventPair)pairs.get(0);
            assert "alice".equals(pair.getValue());

            AuditTypeField field = pair.getField();
            assert "name".equals(field.getName());

            AuditType type = field.getType();
            assert String.class.getName().equals(type.getClassName());
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testSuccesiveInserts() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Set<Long> expectedEntityIds = new HashSet<Long>();
            Set<String> expectedPairValues = new HashSet<String>();

            Date t1 = new Date();

            A a = new A();
            a.setName("alice");
            expectedPairValues.add(a.getName());

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(a);

            t.commit();

            expectedEntityIds.add(a.getId());

            Date t2 = new Date();

            a = new A();
            a.setName("alex");
            expectedPairValues.add(a.getName());

            s = sf.openSession();
            t = s.beginTransaction();

            s.save(a);

            t.commit();

            expectedEntityIds.add(a.getId());

            Date t3 = new Date();

            List ts = HibernateAudit.query("from AuditTransaction as a order by a.timestamp asc");

            assert ts.size() == 2;

            AuditTransaction at = (AuditTransaction)ts.get(0);
            assert at.getTimestamp().getTime() >= floorTime(t1.getTime());
            assert at.getTimestamp().getTime() <= t2.getTime();

            at = (AuditTransaction)ts.get(1);
            assert at.getTimestamp().getTime() >= floorTime(t2.getTime());
            assert at.getTimestamp().getTime() <= t3.getTime();

            List es = HibernateAudit.query("from AuditEvent");

            assert ts.size() == 2;

            AuditEvent ae = (AuditEvent)es.get(0);
            assert AuditEventType.INSERT.equals(ae.getType());
            assert ts.remove(ae.getTransaction());
            AuditType aent = ae.getTargetType();
            assert A.class.getName().equals(aent.getClassName());
            assert expectedEntityIds.remove(ae.getTargetId());

            List pairs = HibernateAudit.query("from AuditEventPair as ap where ap.event = :event", ae);

            assert pairs.size() == 1;

            AuditEventPair pair = (AuditEventPair) pairs.get(0);
            assert "name".equals(pair.getField().getName());
            assert expectedPairValues.remove((String)pair.getValue());

            ae = (AuditEvent)es.get(1);
            assert AuditEventType.INSERT.equals(ae.getType());
            assert ts.remove(ae.getTransaction());
            aent = ae.getTargetType();
            assert A.class.getName().equals(aent.getClassName());
            assert expectedEntityIds.remove(ae.getTargetId());

            pairs = HibernateAudit.query("from AuditEventPair as ap where ap.event = :event", ae);

            assert pairs.size() == 1;

            pair = (AuditEventPair)pairs.get(0);
            assert "name".equals(pair.getField().getName());
            assert expectedPairValues.remove((String)pair.getValue());
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
    public void testAuditType() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Set<String> expectedTypes = new HashSet<String>();
            expectedTypes.add(A.class.getName());
            expectedTypes.add(String.class.getName());

            A a = new A();
            a.setName("alice");

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            s.save(a);
            t.commit();

            List rs = HibernateAudit.query("from AuditType");

            // we inserted two types, the entity and the field type
            assert rs.size() == 2;
            AuditType entityAuditType = null;
            for(Object o: rs)
            {
                AuditType at = (AuditType)o;
                assert expectedTypes.remove(at.getClassName());
                if (A.class.getName().equals(at.getClassName()))
                {
                    entityAuditType = at;
                }
            }

            rs = HibernateAudit.query("from AuditEvent");

            assert rs.size() == 1;

            AuditEvent ae = (AuditEvent)rs.get(0);
            AuditType at2 = ae.getTargetType();
            assert A.class.getName().equals(at2.getClassName());

            assert entityAuditType.getId().equals(at2.getId());
            assert entityAuditType.equals(at2);

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
    public void testAuditType_TwoInsertsSameEntity_OneTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Set<String> expectedTypes = new HashSet<String>();
            expectedTypes.add(A.class.getName());
            expectedTypes.add(String.class.getName());

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            A a = new A();
            a.setName("alice");
            s.save(a);

            a = new A();
            a.setName("alex");
            s.save(a);

            t.commit();

            List rs = HibernateAudit.query("from AuditType");

            assert rs.size() == 2;
            AuditType entityType = null;

            for(Object o: rs)
            {
                AuditType at = (AuditType)o;
                assert expectedTypes.remove((at.getClassName()));
                if (A.class.getName().equals(at.getClassName()))
                {
                    entityType = at;
                }
            }

            rs = HibernateAudit.query("from AuditEvent");

            assert rs.size() == 2;

            AuditEvent ae = (AuditEvent)rs.get(0);
            AuditType at2 = ae.getTargetType();
            assert A.class.getName().equals(at2.getClassName());
            assert entityType.getId().equals(at2.getId());
            assert entityType.equals(at2);

            ae = (AuditEvent)rs.get(1);
            AuditType at3 = ae.getTargetType();
            assert A.class.getName().equals(at3.getClassName());
            assert entityType.getId().equals(at3.getId());
            assert entityType.equals(at3);

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
    public void testAuditType_TwoInsertsSameEntity_TwoTransactions() throws Exception
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
            Transaction t = s.beginTransaction();

            Set<String> expectedTypes = new HashSet<String>();
            expectedTypes.add(String.class.getName());
            expectedTypes.add(A.class.getName());

            A a = new A();
            a.setName("alice");
            s.save(a);

            t.commit();
            s.close();

            s = sf.openSession();
            t = s.beginTransaction();

            a = new A();
            a.setName("alex");
            s.save(a);

            t.commit();
            s.close();

            List rs = HibernateAudit.query("from AuditType");

            assert rs.size() == 2;

            AuditType entityType = null;

            for(Object o: rs)
            {
                AuditType at = (AuditType)o;
                assert expectedTypes.remove(at.getClassName());

                if (A.class.getName().equals(at.getClassName()))
                {
                    entityType = at;
                }
            }

            rs = HibernateAudit.query("from AuditEvent");

            assert rs.size() == 2;

            AuditEvent ae = (AuditEvent)rs.get(0);
            AuditType at2 = ae.getTargetType();
            assert A.class.getName().equals(at2.getClassName());
            assert entityType.getId().equals(at2.getId());
            assert entityType.equals(at2);

            ae = (AuditEvent)rs.get(1);
            AuditType at3 = ae.getTargetType();
            assert A.class.getName().equals(at3.getClassName());
            assert entityType.getId().equals(at3.getId());
            assert entityType.equals(at3);

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
    public void testAuditType_TwoEntities() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactory sf = null;

        Set<String> expectedClassNames = new HashSet<String>();
        expectedClassNames.add(A.class.getName());
        expectedClassNames.add(B.class.getName());
        expectedClassNames.add(String.class.getName());

        try
        {
            sf = config.buildSessionFactory();
            HibernateAudit.enable(sf);

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            A a = new A();
            a.setName("alice");
            s.save(a);

            B b = new B();
            b.setName("bob");
            s.save(b);

            b = new B();
            b.setName("ben");
            s.save(b);

            a = new A();
            a.setName("alex");
            s.save(a);

            t.commit();
            s.close();

            List rs = HibernateAudit.query("from AuditType");

            assert rs.size() == 3;

            assert expectedClassNames.remove(((AuditType)rs.get(0)).getClassName());
            assert expectedClassNames.remove(((AuditType)rs.get(1)).getClassName());
            assert expectedClassNames.remove(((AuditType)rs.get(2)).getClassName());

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
    public void testAuditField_TwoEntities_TwoTransactions() throws Exception
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

            Session s = sf.openSession();
            Transaction t = s.beginTransaction();

            Set<String> expectedTypeNames = new HashSet<String>();
            expectedTypeNames.add(String.class.getName());
            expectedTypeNames.add(Integer.class.getName());
            expectedTypeNames.add(Date.class.getName());
            expectedTypeNames.add(A.class.getName());
            expectedTypeNames.add(B.class.getName());

            A a = new A();
            a.setName("alice");
            a.setAge(30);

            s.save(a);

            B b = new B();
            b.setName("bob");
            b.setBirthDate((Date)Formats.testDateFormat.parseObject("01/01/1971"));

            s.save(b);

            t.commit();
            s.close();

            s = sf.openSession();
            t = s.beginTransaction();

            a = new A();
            a.setName("anna");
            a.setAge(31);

            s.save(a);

            b = new B();
            b.setName("ben");
            b.setBirthDate((Date)Formats.testDateFormat.parseObject("02/02/1972"));

            s.save(b);

            t.commit();
            s.close();

            List types = HibernateAudit.query("from AuditType");

            assert types.size() == 5;

            Set<AuditType> expectedTypes = new HashSet<AuditType>();

            for(Object o: types)
            {
                AuditType at = (AuditType)o;
                assert expectedTypeNames.remove(at.getClassName());
                expectedTypes.add(at);
            }

            List fields = HibernateAudit.query("from AuditTypeField");

            assert fields.size() == 3;

            for(Object o: fields)
            {
                AuditTypeField field = (AuditTypeField)o;
                String name = field.getName();
                AuditType at = field.getType();
                String className = at.getClassName();

                assert expectedTypes.contains(at);

                if ("name".equals(name))
                {
                    assert String.class.getName().equals(className);
                }
                else if ("age".equals(name))
                {
                    assert Integer.class.getName().equals(className);
                }
                else if ("birthDate".equals(name))
                {
                    assert Date.class.getName().equals(className);
                }
                else
                {
                    throw new Error("unexpected field name " + name);
                }
            }

            assert HibernateAudit.query("from AuditEvent").size() == 4;

            List pairs = HibernateAudit.query("from AuditEventPair");

            assert pairs.size() == 8;

            List<String> expectedNames = new ArrayList<String>();
            expectedNames.add("name");
            expectedNames.add("age");
            expectedNames.add("name");
            expectedNames.add("birthDate");
            expectedNames.add("name");
            expectedNames.add("age");
            expectedNames.add("name");
            expectedNames.add("birthDate");

            List<Object> expectedValues = new ArrayList<Object>();
            expectedValues.add("alice");
            expectedValues.add(30);
            expectedValues.add("bob");
            expectedValues.add(Formats.testDateFormat.parseObject("01/01/1971"));
            expectedValues.add("anna");
            expectedValues.add(31);
            expectedValues.add("ben");
            expectedValues.add(Formats.testDateFormat.parseObject("02/02/1972"));

            for(Object o: pairs)
            {
                AuditEventPair p = (AuditEventPair)o;

                String name = p.getField().getName();
                Object value = p.getValue();

                assert expectedNames.remove(name);
                assert expectedValues.remove(value);
            }
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
    public void testInsert_EmptyState() throws Exception
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

            Session s = sf.openSession();
            s.beginTransaction();

            s.save(a);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(a.getId());

            assert transactions.size() == 1;

            A base = new A();
            HibernateAudit.delta(base, a.getId(), transactions.get(0).getId());

            assert a.getId().equals(base.getId());
            assert base.getName() == null;
            assert base.getAge() == null;
        }
        catch(Exception e)
        {
            log.error("test failed unexpectedly", e);
            throw e;
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
