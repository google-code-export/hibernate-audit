package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;

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
public class PostInsertTuplizerEntityTest extends JTATransactionTest 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

//    @Test(enabled = true) TODO https://jira.novaordis.org/browse/HBA-107
//    public void testManyToOne_OneIsTuplizer() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xaMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA' table='XA'>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <property name='name' type='string'/>\n" +
//            "        <many-to-one name='xb' column='xb_id' entity-name='XB' cascade='all'/>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xbMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xaMapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
//
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
//            XA xa = new XA();
//            XB xb = new XB();
//
//            XBTuplizer tuplizer = new XBTuplizer();
//            tuplizer.setPropertyValue(xb, "name", "xbone");
//
//            xa.setXb(xb);
//
//            s.save(xa);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa.getId());
//            assert transactions.size() == 1;
//
//            XA base = new XA();
//            HibernateAudit.delta(base, xa.getId(), transactions.get(0).getId());
//
//            XB restored = base.getXb();
//            assert xb.getId().equals(restored.getId());
//            assert "xbone".equals(restored.getName());
//
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
//    @Test(enabled = true) // TODO https://jira.novaordis.org/browse/HBA-81
//    public void testMissingMutatorThatMayBeSalvagedByTuplizer() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xaMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA' table='XA'>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <property name='name' type='string'/>\n" +
//            "        <many-to-one name='xc' column='xc_id' entity-name='XC' cascade='all'/>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xcMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XC' name='com.googlecode.hibernate.audit.test.post_insert.data.XC' table='XC'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XCTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xaMapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xcMapping.getBytes()));
//
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
//            XA xa = new XA();
//            XC xc = new XC();
//
//            XCTuplizer tuplizer = new XCTuplizer();
//            tuplizer.setPropertyValue(xc, "name", "xcone");
//
//            xa.setXc(xc);
//
//            s.save(xa);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa.getId());
//            assert transactions.size() == 1;
//
//            XA base = new XA();
//            HibernateAudit.delta(base, xa.getId(), transactions.get(0).getId());
//
//            XC restored = base.getXc();
//            assert xc.getId().equals(restored.getId());
//            assert "xcone".equals(restored.getName());
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
//    public void testManyToOne_OneIsTuplizer_Collection() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xa2Mapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA2' table='XA2'>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <set name='xbs' cascade='all'>\n" +
//            "            <key column='xa_id'/>\n" +
//            "            <one-to-many entity-name='XB'/>\n" +
//            "        </set>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xbMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xa2Mapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
//
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
//            XA2 xa2 = new XA2();
//            XB xbone = new XB();
//            XB xbtwo = new XB();
//
//            XBTuplizer tuplizer = new XBTuplizer();
//            tuplizer.setPropertyValue(xbone, "name", "xbone");
//            tuplizer.setPropertyValue(xbtwo, "name", "xbtwo");
//
//            Set<XB> xbs = xa2.getXbs();
//            xbs.add(xbone);
//            xbs.add(xbtwo);
//
//            s.save(xa2);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa2.getId());
//            assert transactions.size() == 1;
//
//            XA2 base = new XA2();
//            HibernateAudit.delta(base, xa2.getId(), transactions.get(0).getId());
//
//            Set<XB> restored = base.getXbs();
//            assert restored.size() == 2;
//
//            for(XB xb: restored)
//            {
//                if (xbone.getId().equals(xb.getId()))
//                {
//                    assert "xbone".equals(xb.getName());
//                }
//                else if (xbtwo.getId().equals(xb.getId()))
//                {
//                    assert "xbtwo".equals(xb.getName());
//                }
//                else
//                {
//                    throw new Error("unexpected " + xb);
//                }
//            }
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
//    @Test(enabled = true) // TODO 1.1 https://jira.novaordis.org/browse/HBA-107
//    public void testManyToOne_BothAreTuplizers_Collection() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xa3Mapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XA3' name='com.googlecode.hibernate.audit.test.post_insert.data.XA3' table='XA3'>\n" +
//            "        <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XA3Tuplizer'/>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <set name='xbs' cascade='all'>\n" +
//            "            <key column='xa_id'/>\n" +
//            "            <one-to-many entity-name='XB'/>\n" +
//            "        </set>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xbMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xa3Mapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
//
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
//            XA3 xa3 = new XA3();
//            XB xbone = new XB();
//            XB xbtwo = new XB();
//
//            XBTuplizer xbTuplizer = new XBTuplizer();
//            xbTuplizer.setPropertyValue(xbone, "name", "xbone");
//            xbTuplizer.setPropertyValue(xbtwo, "name", "xbtwo");
//
//            Set<XB> xbs = new HashSet<XB>();
//            xbs.add(xbone);
//            xbs.add(xbtwo);
//            XA3Tuplizer xa3Tuplizer = new XA3Tuplizer();
//            xa3Tuplizer.setPropertyValue(xa3, "xbs", xbs);
//
//            s.save("XA3", xa3);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa3.getId());
//            assert transactions.size() == 1;
//
//            XA3 base = new XA3();
//            HibernateAudit.delta(base, "XA3", xa3.getId(), transactions.get(0).getId());
//
//            assert xa3 != base;
//            assert xa3.getId().equals(base.getId());
//
//            Set<XB> xbsREstored = base.getXbs();
//            assert xbsREstored != xbs;
//
//            assert xbsREstored.size() == 2;
//
//            for(XB xb: xbsREstored)
//            {
//                assert xb != xbone;
//                assert xb != xbtwo;
//
//                if (xbone.getId().equals(xb.getId()))
//                {
//                    assert "xbone".equals(xb.getName());
//                }
//                else if (xbtwo.getId().equals(xb.getId()))
//                {
//                    assert "xbtwo".equals(xb.getName());
//                }
//                else
//                {
//                    throw new Error("unexpected " + xb);
//                }
//            }
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
//    public void testManyToOne_OneIsTuplizer_EmptyCollection() throws Exception
//    {
//        Configuration config = new Configuration();
//        config.configure(getHibernateConfigurationFileName());
//
//        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
//        // enough)
//
//        String xa2Mapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA2' table='XA2'>\n" +
//            "        <id name='id' type='long'>\n" +
//            "            <generator class='native'/>\n" +
//            "        </id>\n" +
//            "        <set name='xbs' cascade='all'>\n" +
//            "            <key column='xa_id'/>\n" +
//            "            <one-to-many entity-name='XB'/>\n" +
//            "        </set>\n" +
//            "    </class>\n" +
//            "</hibernate-mapping>";
//
//        String xbMapping =
//            "<?xml version='1.0'?>\n" +
//            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
//            "    \"-//Hibernate/HibernateReflections Mapping DTD 3.0//EN\"\n" +
//            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" +
//            "<hibernate-mapping>\n" +
//            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
//            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
//            "      <id name='id' type='long'>\n" +
//            "         <generator class='native'/>\n" +
//            "      </id>\n" +
//            "      <property name='name' type='string'/>\n" +
//            "   </class>\n" +
//            "</hibernate-mapping>";
//
//        config.addInputStream(new ByteArrayInputStream(xa2Mapping.getBytes()));
//        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));
//
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
//            XA2 xa2 = new XA2();
//
//            // empty collection
//
//            s.save(xa2);
//
//            s.getTransaction().commit();
//            s.close();
//
//            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa2.getId());
//            assert transactions.size() == 1;
//
//            XA2 base = new XA2();
//            HibernateAudit.delta(base, xa2.getId(), transactions.get(0).getId());
//
//            Set<XB> restored = base.getXbs();
//            assert restored.isEmpty();
//
//        }
//        catch(Exception e)
//        {
//            log.error("test failed unexpectedly", e);
//            throw e;
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
