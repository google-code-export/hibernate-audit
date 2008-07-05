package com.googlecode.hibernate.audit.test.mock.jca;

import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;
import java.sql.PreparedStatement;
import java.sql.CallableStatement;
import java.sql.DatabaseMetaData;
import java.sql.SQLWarning;
import java.sql.Savepoint;
import java.util.Map;

/**
 * A wrapper around a raw JDBC Connection, allowing us to intercept invocations and manage pooling.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class InterceptedConnection implements Connection
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(InterceptedConnection.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private MockJTAAwareDataSource manager;
    private Connection delegate;


    // Constructors --------------------------------------------------------------------------------

    public InterceptedConnection(MockJTAAwareDataSource manager, Connection delegate)
    {
        this.manager = manager;
        this.delegate = delegate;
    }

    // Connection implementation -------------------------------------------------------------------

    public Statement createStatement() throws SQLException
    {
        return delegate.createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException
    {
        return delegate.prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException
    {
        return delegate.prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException
    {
        return delegate.nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException
    {
        delegate.setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException
    {
        return delegate.getAutoCommit();
    }

    public void commit() throws SQLException
    {
        delegate.commit();
    }

    public void rollback() throws SQLException
    {
        delegate.rollback();
    }

    public void close() throws SQLException
    {
        // the manager will do it when the time comes
        log.debug("intercepted and deflected " + this + ".close()");
    }

    public boolean isClosed() throws SQLException
    {
        return delegate.isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException
    {
        return delegate.getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException
    {
        delegate.setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException
    {
        return delegate.isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException
    {
        delegate.setCatalog(catalog);
    }

    public String getCatalog() throws SQLException
    {
        return delegate.getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException
    {
        delegate.setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException
    {
        return delegate.getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException
    {
        return delegate.getWarnings();
    }

    public void clearWarnings() throws SQLException
    {
        delegate.clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
        return delegate.createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency) throws SQLException
    {
        return delegate.prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency)
        throws SQLException
    {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException
    {
        return delegate.getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException
    {
        delegate.setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException
    {
        delegate.setHoldability(holdability);
    }

    public int getHoldability() throws SQLException
    {
        return delegate.getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException
    {
        return delegate.setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException
    {
        return delegate.setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException
    {
        delegate.rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException
    {
        delegate.releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency,
                                     int resultSetHoldability) throws SQLException
    {
        return delegate.createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType,
                                              int resultSetConcurrency, int resultSetHoldability)
        throws SQLException
    {
        return delegate.
            prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency,
                                         int resultSetHoldability) throws SQLException
    {
        return delegate.prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys)
        throws SQLException
    {
        return delegate.prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException
    {
        return delegate.prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException
    {
        return delegate.prepareStatement(sql, columnNames);
    }

    // Public --------------------------------------------------------------------------------------

    public MockJTAAwareDataSource getManager()
    {
        return manager;
    }

    public Connection getDelegate()
    {
        return delegate;
    }

    public String toString()
    {
        return "InterceptedConnection[" + delegate + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
