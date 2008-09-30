package com.googlecode.hibernate.audit;

import org.hibernate.connection.ConnectionProvider;
import org.hibernate.HibernateException;

import java.util.Properties;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * A ConnectionProvider that delegates to an already existing ConnectionProvider instance. The
 * DelegateConnectionProvider expects to find the instance it delegates to in the Properties
 * instance passed as argument to the configure() method, keyed on
 * AuditEnvironment.CONNECTION_PROVIDER_DELEGATE.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class DelegateConnectionProvider implements ConnectionProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private ConnectionProvider delegate;

    // Constructors --------------------------------------------------------------------------------

    // ConnectionProvider implementation -----------------------------------------------------------

    public void configure(Properties props) throws HibernateException
    {
        Object o = props.get(AuditEnvironment.CONNECTION_PROVIDER_DELEGATE);

        if (o == null)
        {
            throw new HibernateException(
                "no delegate instance found in configuration properties while configuring " + this);
        }

        if (!(o instanceof ConnectionProvider))
        {
            throw new HibernateException(
                "delegate instance is not a ConnectionProvider, but a " + o);
        }

        delegate = (ConnectionProvider)o;

        // cleanup
        props.remove(AuditEnvironment.CONNECTION_PROVIDER_DELEGATE);
    }

    public Connection getConnection() throws SQLException
    {
        if (delegate == null)
        {
            throw new IllegalStateException("no delegate instance configured");
        }

        return delegate.getConnection();
    }

    public void closeConnection(Connection conn) throws SQLException
    {
        if (delegate == null)
        {
            throw new IllegalStateException("no delegate instance configured");
        }

        delegate.closeConnection(conn);
    }

    public void close() throws HibernateException
    {
        if (delegate == null)
        {
            throw new IllegalStateException("no delegate instance configured");
        }

        delegate.close();
    }

    public boolean supportsAggressiveRelease()
    {
        if (delegate == null)
        {
            throw new IllegalStateException("no delegate instance configured");
        }

        return delegate.supportsAggressiveRelease();
    }

    // Public --------------------------------------------------------------------------------------

    public ConnectionProvider getDelegate()
    {
        return delegate;
    }

    @Override
    public String toString()
    {
        return "DelegateConnectionProvider[" +
               (delegate == null ?
                Integer.toHexString(System.identityHashCode(this)) :
                delegate) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
