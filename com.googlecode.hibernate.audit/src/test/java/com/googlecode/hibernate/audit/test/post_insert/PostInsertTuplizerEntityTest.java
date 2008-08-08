package com.googlecode.hibernate.audit.test.post_insert;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.post_insert.data.XA;
import com.googlecode.hibernate.audit.test.post_insert.data.XB;
import com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;

import java.util.List;
import java.io.ByteArrayInputStream;

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

    private static final Logger log = Logger.getLogger(PostInsertTuplizerEntityTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testManyToOne_OneIsTuplizer() throws Exception
    {
        Configuration config = new Configuration();
        config.configure(getHibernateConfigurationFileName());

        // we add metadata as XML otherwise we can't simulate the condition (JPA not expressive
        // enough)

        String xaMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" + 
            "<hibernate-mapping>\n" +
            "   <class name='com.googlecode.hibernate.audit.test.post_insert.data.XA' table='XA'>\n" +
            "        <id name='id' type='long'>\n" +
            "            <generator class='native'/>\n" +
            "        </id>\n" +
            "        <property name='name' type='string'/>\n" +
            "        <many-to-one name='xb' column='xb_id' entity-name='XB' cascade='all'/>\n" +
            "    </class>\n" +
            "</hibernate-mapping>";

        String xbMapping =
            "<?xml version='1.0'?>\n" +
            "<!DOCTYPE hibernate-mapping PUBLIC\n" +
            "    \"-//Hibernate/Hibernate Mapping DTD 3.0//EN\"\n" +
            "    \"http://hibernate.sourceforge.net/hibernate-mapping-3.0.dtd\">\n" + 
            "<hibernate-mapping>\n" +
            "   <class entity-name='XB' name='com.googlecode.hibernate.audit.test.post_insert.data.XB' table='XB'>\n" +
            "      <tuplizer entity-mode='pojo' class='com.googlecode.hibernate.audit.test.post_insert.data.XBTuplizer'/>\n" +
            "      <id name='id' type='long'>\n" +
            "         <generator class='native'/>\n" +
            "      </id>\n" +
            "      <property name='name' type='string'/>\n" +
            "   </class>\n" +
            "</hibernate-mapping>";

        config.addInputStream(new ByteArrayInputStream(xaMapping.getBytes()));
        config.addInputStream(new ByteArrayInputStream(xbMapping.getBytes()));

        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            Session s = sf.openSession();
            s.beginTransaction();

            XA xa = new XA();
            XB xb = new XB();

            XBTuplizer tuplizer = new XBTuplizer();
            tuplizer.setPropertyValue(xb, "name", "xbone");

            xa.setXb(xb);

            s.save(xa);

            s.getTransaction().commit();
            s.close();

            List<AuditTransaction> transactions = HibernateAudit.getTransactions(xa.getId());
            assert transactions.size() == 1;

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
