package com.googlecode.hibernate.audit.test.base;

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
 * A base class that manipulates the environment in such a way that give the tested code the
 * illusion that it runs in a managed environment (JNDI, JTA, etc), or a simple non-managed
 * environment.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
abstract class ConfigurableEnvironmentSupport
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ConfigurableEnvironmentSupport.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String mockDataSourceJNDIName;

    // Constructors --------------------------------------------------------------------------------

    ConfigurableEnvironmentSupport()
    {
        mockDataSourceJNDIName = "local:MockMySqlDS";
    }

    // Public --------------------------------------------------------------------------------------

    public abstract void beforeTest() throws Exception;
    public abstract void afterTest() throws Exception;

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected abstract String getHibernateConfigurationFileName();

    protected String getTransactionManagerJNDIName()
    {
        return MockTransactionManager.DEFAULT_TRANSACTION_MANAGER_JNDI_NAME;
    }

    protected String getUserTransactionJNDIName()
    {
        return MockUserTransaction.DEFAULT_USER_TRANSACTION_JNDI_NAME;
    }

    protected void startJTAEnvironment() throws Exception
    {
        log.info("starting JTA environment");

        // configure "JCA"
        MockJTAAwareDataSource ds = new MockJTAAwareDataSource("com.mysql.jdbc.Driver",
                                                               "jdbc:mysql://localhost/playground",
                                                               "playground",
                                                               "playground");
        ds.start();

        // configure "JTA"
        MockTransactionManager tm = MockTransactionManager.getInstance();
        tm.start();

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

    protected void stopJTAEnvironment() throws Exception
    {
        log.info("stopping JTA environment");

        InitialContext ic = new InitialContext();

        MockTransactionManager tm  = (MockTransactionManager)ic.
            lookup(MockTransactionManager.DEFAULT_TRANSACTION_MANAGER_JNDI_NAME);
        tm.stop();

        MockJTAAwareDataSource ds = (MockJTAAwareDataSource)ic.lookup(mockDataSourceJNDIName);
        ds.stop();

        ic.close();

        // unfortunately, can't clean JNDI NamingManager's initialContextFactoryBuilder, API
        // won't allow it
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
