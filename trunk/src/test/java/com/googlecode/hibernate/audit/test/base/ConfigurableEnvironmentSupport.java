package com.googlecode.hibernate.audit.test.base;

import org.apache.log4j.Logger;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import javax.naming.spi.NamingManager;
import javax.naming.InitialContext;

import com.googlecode.hibernate.audit.test.mock.jndi.MockInitialContextFactoryBuilder;
import com.googlecode.hibernate.audit.test.mock.jta.MockUserTransaction;
import com.googlecode.hibernate.audit.test.mock.jta.MockTransactionManager;
import com.googlecode.hibernate.audit.test.mock.jca.MockJTAAwareDataSource;
import com.googlecode.hibernate.audit.HibernateAuditEnvironment;
import com.googlecode.hibernate.audit.util.DDL;
import com.googlecode.hibernate.audit.util.DDLSchema;

import java.util.Map;
import java.util.HashMap;
import java.io.InputStream;
import java.sql.Connection;

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
public abstract class ConfigurableEnvironmentSupport
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ConfigurableEnvironmentSupport.class);

    /**
     * Relative to the classpath.
     */
    public static final String DEFAULT_DDL_FILE_NAME = "test_ddl.sql";
    public static final boolean DEFAULT_USE_EXTERNALLY_CREATED_SCHEMA = true;

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String connectionDriverClassName;
    private String connectionUrl;
    private String connectionUsername;
    private String connectionPassword;
    private String mockDataSourceJNDIName;

    /**
     * The single point of configuration for how schema is created: externally, by reading it from
     * a DDL file, or internally by hibernate ('create-drop').
     */
    private boolean useExternallyCreatedSchema;

    /**
     * The schema DDL file name, relative to the classpath.
     */
    private String schemaDDLFileName;

    private DDLSchema ddlSchema;

    // Constructors --------------------------------------------------------------------------------

    ConfigurableEnvironmentSupport()
    {
        extractConnectionConfiguration();
        setDataSourceJNDIName();
        // the DDL file name will be ignored if useExternallyCreatedSchema is set to false
        setSchemaDDLFileName(DEFAULT_DDL_FILE_NAME);

        // TODO configure this externally
        setUseExternallyCreatedSchema(DEFAULT_USE_EXTERNALLY_CREATED_SCHEMA);
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * TODO seems to me that this is run before suite, not before test
     */
    public void beforeTest() throws Exception
    {
    }

    /**
     * TODO seems to me that this is run after suite, not after test
     */
    public void afterTest() throws Exception
    {
    }

    @BeforeMethod
    public void createAuditTables() throws Exception
    {
        if (!useExternallyCreatedSchema)
        {
            // we rely on Hibernate SchemaExport functionality
            System.setProperty(HibernateAuditEnvironment.HBM2DDL_AUTO, "create-drop");
            return;
        }

        Connection c = null;

        try
        {
            c = DDL.getRawConnection(connectionDriverClassName, connectionUrl,
                                     connectionUsername, connectionPassword);

            // will lazy instantiate if necessary
            getExternalDDLSchema().create(c);
        }
        finally
        {
            if (c != null)
            {
                c.close();
            }
        }

        System.setProperty(HibernateAuditEnvironment.HBM2DDL_AUTO, "validate");
    }

    @AfterMethod
    public void dropAuditTables() throws Exception
    {
        System.clearProperty(HibernateAuditEnvironment.HBM2DDL_AUTO);

        // if using an externally created schema, we drop the tables ourselves, otherwise rely
        // on Hibernate to drop them on session factory close()

        if (useExternallyCreatedSchema)
        {
            Connection c = null;

            try
            {
                c = DDL.getRawConnection(connectionDriverClassName, connectionUrl,
                                         connectionUsername, connectionPassword);
                ddlSchema.drop(c);
            }
            finally
            {
                if (c != null)
                {
                    c.close();
                }
            }
        }
    }
    
    public boolean isUseExternallyCreatedSchema()
    {
        return useExternallyCreatedSchema;
    }

    public void setUseExternallyCreatedSchema(boolean b)
    {
        this.useExternallyCreatedSchema = b;
    }

    public String getSchemaDDLFileName()
    {
        return schemaDDLFileName;
    }

    public void setSchemaDDLFileName(String schemaDDLFileName)
    {
        this.schemaDDLFileName = schemaDDLFileName;
    }

    public String getConnectionDriverClassName()
    {
        return connectionDriverClassName;
    }

    public String getConnectionUrl()
    {
        return connectionUrl;
    }

    public String getConnectionUsername()
    {
        return connectionUsername;
    }

    public String getConnectionPassword()
    {
        return connectionPassword;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected abstract String getHibernateConfigurationFileName();
    protected abstract TransactionType getTransactionType();

    protected String getTransactionManagerJNDIName()
    {
        return MockTransactionManager.DEFAULT_TRANSACTION_MANAGER_JNDI_NAME;
    }

    protected String getUserTransactionJNDIName()
    {
        return MockUserTransaction.DEFAULT_USER_TRANSACTION_JNDI_NAME;
    }

    protected String getDataSourceJNDIName()
    {
        return mockDataSourceJNDIName;
    }

    protected void startJTAEnvironment() throws Exception
    {
        log.info("starting JTA environment");

        //System.getProperties().setProperty("oracle.net.READ_TIMEOUT", "5000");

        // configure "JCA"
        MockJTAAwareDataSource ds = new MockJTAAwareDataSource(connectionDriverClassName,
                                                               connectionUrl,
                                                               connectionUsername,
                                                               connectionPassword);
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
    /**
     * Extracts Data Source configuration (connection URL, username, password, driver) from the
     * environment.
     */
    private void extractConnectionConfiguration()
    {
        connectionDriverClassName = System.getProperty("hibernate.connection.driver_class");

        if (connectionDriverClassName == null)
        {
            connectionDriverClassName = System.getProperty("connection.driver_class");
        }

        if (connectionDriverClassName == null)
        {
            // we cheat for the time being, see https://jira.novaordis.org/browse/HBA-38
            //throw new IllegalStateException("cannot figure out connection's driver class name");
            connectionDriverClassName = "oracle.jdbc.driver.OracleDriver";
        }

        connectionUrl = System.getProperty("hibernate.connection.url");

        if (connectionUrl == null)
        {
            connectionUrl = System.getProperty("connection.url");
        }

        if (connectionUrl == null)
        {
            // we cheat for the time being, see https://jira.novaordis.org/browse/HBA-38
            //throw new IllegalStateException("cannot figure out connection's URL");
            connectionUrl = "jdbc:oracle:thin:@localhost:1521:XE";
        }

        connectionUsername = System.getProperty("hibernate.connection.username");

        if (connectionUsername == null)
        {
            connectionUsername = System.getProperty("connection.username");
        }

        if (connectionUsername == null)
        {
            // we cheat for the time being, see https://jira.novaordis.org/browse/HBA-38
            //throw new IllegalStateException("cannot figure out connection's username");
            connectionUsername = "test";
        }

        if (TransactionType.JTA.equals(getTransactionType()))
        {
            // remove username from the environment to keep hibernate from calling
            // getConnection(username, password), and pass it via the datasource
            System.clearProperty("hibernate.connection.username");
            System.clearProperty("connection.username");
        }

        connectionPassword = System.getProperty("hibernate.connection.password");

        if (connectionPassword == null)
        {
            connectionPassword = System.getProperty("connection.password");
        }

        if (connectionPassword == null)
        {
            // we cheat for the time being, see https://jira.novaordis.org/browse/HBA-38
            //throw new IllegalStateException("cannot figure out connection's password");
            connectionPassword = "test";
        }

        if (TransactionType.JTA.equals(getTransactionType()))
        {
            // remove password from the environment to keep hibernate from calling
            // getConnection(username, password), and pass it via the datasource
            System.clearProperty("hibernate.connection.password");
            System.clearProperty("connection.password");
        }

        // also cheating, see https://jira.novaordis.org/browse/HBA-38
        System.setProperty("hibernate.dialect", "org.hibernate.dialect.Oracle10gDialect");
    }

    private void setDataSourceJNDIName()
    {
        mockDataSourceJNDIName = "local:MockDS";
        System.setProperty("local.test.datasource", mockDataSourceJNDIName);
    }

    private DDLSchema getExternalDDLSchema() throws Exception
    {
        if (ddlSchema == null)
        {
            InputStream is = Thread.
                currentThread().getContextClassLoader().getResourceAsStream(schemaDDLFileName);

            if (is == null)
            {
                throw new Error("could not find DDL file " + schemaDDLFileName +
                                " relative to the current class path");
            }

            ddlSchema = new DDLSchema(is);
        }

        return ddlSchema;
    }

    // Inner classes -------------------------------------------------------------------------------
}
