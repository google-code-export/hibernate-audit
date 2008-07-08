package com.googlecode.hibernate.audit.test.mock.jca;

import org.apache.log4j.Logger;

import javax.sql.DataSource;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.transaction.SystemException;
import javax.transaction.Status;
import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.Synchronization;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.DriverManager;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;

import com.googlecode.hibernate.audit.test.mock.jta.MockTransactionManager;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockJTAAwareDataSource implements DataSource
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(MockJTAAwareDataSource.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String driverClassName;
    private String connectionUrl;
    private String connectionUsername;
    private String connectionPassword;

    private Map<Transaction, InterceptedConnection> connections;

    private boolean broken;

    // Constructors --------------------------------------------------------------------------------

    public MockJTAAwareDataSource(String driverClassName,
                                  String connectionUrl,
                                  String connectionUsername,
                                  String connectionPassword) throws Exception
    {
        this.driverClassName = driverClassName;
        this.connectionUrl = connectionUrl;
        this.connectionUsername = connectionUsername;
        this.connectionPassword = connectionPassword;
    }

    // DataSource implementation -------------------------------------------------------------------

    public Connection getConnection() throws SQLException
    {
        InitialContext ic = null;
        try
        {
            ic = new InitialContext();
            TransactionManager tm = (TransactionManager)ic.
                lookup(MockTransactionManager.DEFAULT_TRANSACTION_MANAGER_JNDI_NAME);

            if (Status.STATUS_NO_TRANSACTION == tm.getStatus())
            {
                // no active JTA transaction, Hibernate will use local JDBC transactions, return
                // a new raw database connection. This is very inneficient, but we rely on
                // Hibernate connection pooling too
                Connection raw = DriverManager.getConnection(getConnectionUrl(),
                                                             getConnectionUsername(),
                                                             getConnectionPassword());
                raw.setAutoCommit(false);
                return new InterceptedConnection(this, raw);
            }

            Transaction t = tm.getTransaction();

            // the current implementation of this mock fixture tracks connections by transactions
            Connection c = connections.get(t);

            if (c != null)
            {
                return c;
            }

            // this transaction has no associated connection yet

            Connection raw =  DriverManager.getConnection(getConnectionUrl(),
                                                          getConnectionUsername(),
                                                          getConnectionPassword());

            raw.setAutoCommit(false);
            InterceptedConnection inc = new InterceptedConnection(this, raw);
            JDBCConnectionXAResource xar = new JDBCConnectionXAResource(inc);

            if (!t.enlistResource(xar))
            {
                throw new IllegalStateException("enlisting should have succeeded");
            }

            t.registerSynchronization(new InterceptedConnectionCleaner(t));
            connections.put(t, inc);

            return inc;
        }
        catch(NamingException e)
        {
            throw new IllegalStateException("JNDI failure", e);
        }
        catch(SystemException e)
        {
            throw new IllegalStateException("JTA failure", e);
        }
        catch(RollbackException e)
        {
            throw new IllegalStateException("Current JTA transaction marked for rollback", e);
        }
        finally
        {
            if (ic != null)
            {
                try
                {
                    ic.close();
                }
                catch(Exception e)
                {
                    throw new IllegalStateException("failed to close InitialContext instance", e);
                }
            }
        }
    }

    public Connection getConnection(String username, String password) throws SQLException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public PrintWriter getLogWriter() throws SQLException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void setLogWriter(PrintWriter out) throws SQLException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void setLoginTimeout(int seconds) throws SQLException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public int getLoginTimeout() throws SQLException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    public synchronized void start() throws Exception
    {
        // load the driver
        Class.forName(getConnectionDriverClassName());

        if (connections != null)
        {
            log.warn(this + " already started");
            return;
        }

        connections =
            Collections.synchronizedMap(new HashMap<Transaction, InterceptedConnection>());

        log.info(this + " url=" + getConnectionUrl()+ ", username=" + getConnectionUsername() +
                 ", password=***, driver=" + getConnectionDriverClassName() + " started");
    }

    public synchronized void stop() throws Exception
    {
        if (connections == null)
        {
            log.warn(this + " already stopped");
            return;
        }

        if (!connections.isEmpty())
        {
            throw new Exception(
                "There are " + connections.size() + " active connections, cannot stop");
        }

        connections = null;
    }

    public String getConnectionDriverClassName()
    {
        return driverClassName;
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

    public boolean isBroken()
    {
        return broken;
    }

    public void setBroken(boolean b)
    {
        this.broken = b;
    }

    @Override
    public String toString()
    {
        return "MockDataSource[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    /**
     * The job of this synchronization is to take the connection out of the Transaction-Connection
     * map as soon as the transaction becomes irrelevant.
     */
    private class InterceptedConnectionCleaner implements Synchronization
    {
        private Transaction t;

        InterceptedConnectionCleaner(Transaction t)
        {
            this.t = t;
        }

        public void beforeCompletion()
        {
            // noop
        }

        public void afterCompletion(int i)
        {
            InterceptedConnection c = connections.remove(t);

            // discard the delegate
            Connection delegate = c.getDelegate();

            try
            {
                delegate.close();
            }
            catch(Exception e)
            {
                log.error("failed to close " + delegate);
            }
        }

        @Override
        public String toString()
        {
            return "InterceptedConnectionCleaner[" +
                   Integer.toHexString(System.identityHashCode(this)) + "]";
        }
    }

}
