package com.googlecode.hibernate.audit.test.base;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.AfterSuite;
import org.apache.log4j.Logger;

import javax.naming.spi.NamingManager;
import javax.naming.InitialContext;

import com.googlecode.hibernate.audit.test.mock.jndi.MockInitialContextFactoryBuilder;
import com.googlecode.hibernate.audit.test.mock.jta.MockUserTransaction;
import com.googlecode.hibernate.audit.test.mock.jta.MockTransactionManager;
import com.googlecode.hibernate.audit.test.mock.jca.MockJTAAwareDataSource;

import java.util.Map;
import java.util.HashMap;

/**
 * A test base that manipulates the environment in such a way that give the tested code the illusion
 * that it runs in a managed environment (JNDI, JTA, etc), or a simple non-managed environment.
 *
 * All tests that need this should inherit from it.
 * 
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public abstract class ConfigurableEnvironmentTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ConfigurableEnvironmentTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private TransactionType transactionType;
    private String mockDataSourceJNDIName;

    // Constructors --------------------------------------------------------------------------------

    public ConfigurableEnvironmentTest()
    {
        transactionType = TransactionType.JTA;
        mockDataSourceJNDIName = "local:MockMySqlDS";
    }

    // Public --------------------------------------------------------------------------------------

    @BeforeSuite
    public void configure() throws Exception
    {
        log.info("configuring environment");

        if (TransactionType.JTA.equals(transactionType))
        {
            // configure "JCA"
            MockJTAAwareDataSource ds =
                new MockJTAAwareDataSource("com.mysql.jdbc.Driver",
                                           "jdbc:mysql://localhost/playground",
                                           "playground",
                                           "playground");
            ds.start();

            // configure "JTA"
            MockTransactionManager tm = MockTransactionManager.getInstance();
            MockUserTransaction ut = new MockUserTransaction(tm);

            // configure "JNDI"
            Map<String, Object> jndi = new HashMap<String, Object>();

            jndi.put(MockUserTransaction.DEFAULT_USER_TRANSACTION_JNDI_NAME, ut);
            jndi.put(MockTransactionManager.DEFAULT_TRANSACTION_MANAGER_JNDI_NAME, tm);
            jndi.put(mockDataSourceJNDIName, ds);

            MockInitialContextFactoryBuilder icfb = new MockInitialContextFactoryBuilder(jndi);
            NamingManager.setInitialContextFactoryBuilder(icfb);

            InitialContext ic = new InitialContext();
            assert ut == ic.lookup(MockUserTransaction.DEFAULT_USER_TRANSACTION_JNDI_NAME);
            assert tm == ic.lookup(MockTransactionManager.DEFAULT_TRANSACTION_MANAGER_JNDI_NAME);
            assert ds == ic.lookup(mockDataSourceJNDIName);
        }
        else if (TransactionType.LOCAL.equals(transactionType))
        {
            // not much to do here
        }
        else
        {
            throw new IllegalStateException("unsupported transaction type " + transactionType);
        }
    }

    @AfterSuite
    public void clean() throws Exception
    {
        log.info("cleaning environment");

        if (TransactionType.JTA.equals(transactionType))
        {
            InitialContext ic = new InitialContext();

            MockJTAAwareDataSource ds = (MockJTAAwareDataSource)ic.lookup(mockDataSourceJNDIName);
            ds.stop();

            ic.close();
            
            // unfortunately, can't clean JNDI NamingManager's initialContextFactoryBuilder, API
            // won't allow it
        }
        else if (TransactionType.LOCAL.equals(transactionType))
        {
            // not much to do here
        }
        else
        {
            throw new IllegalStateException("unsupported transaction type " + transactionType);
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected String getHibernateConfigurationFileName()
    {
        if (TransactionType.JTA.equals(transactionType))
        {
            return "/hibernate-jta.cfg.xml";
        }
        else if (TransactionType.LOCAL.equals(transactionType))
        {
            return "/hibernate-thread.cfg.xml";
        }
        else
        {
            throw new IllegalStateException("unknow transaction environment " + transactionType);
        }
    }

    /**
     * TODO some databases "floor" the time stamp to second. Need to investigate why this happen,
     * fix it and get rid of all floorTime() call throughout the code base.
     */
    protected long floorTime(long timestamp)
    {
        timestamp = timestamp / 1000;
        return timestamp * 1000;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
