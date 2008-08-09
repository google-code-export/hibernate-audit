package com.googlecode.hibernate.audit.model;

import org.hibernate.cfg.SettingsFactory;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.transaction.TransactionFactory;
import org.hibernate.transaction.JTATransactionFactory;
import org.hibernate.transaction.TransactionManagerLookup;
import org.apache.log4j.Logger;

import java.util.Properties;
import java.util.Set;
import java.util.HashSet;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.googlecode.hibernate.audit.DelegateConnectionProvider;
import com.googlecode.hibernate.audit.AuditEnvironment;
import com.googlecode.hibernate.audit.HibernateAuditEnvironment;

/**
 * A SettingsFactory that knows how to extract certain "interesting" properties from audited session
 * factories' settings and provide them to the internal audit configuration. The configuration built
 * based on those settings will be used to create the internal audit session factory.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
class AuditSettingsFactory extends SettingsFactory
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditSettingsFactory.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Settings sourceSettings;

    // Constructors --------------------------------------------------------------------------------

    AuditSettingsFactory(Settings sourceSettings)
    {
        this.sourceSettings = sourceSettings;
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public Settings buildSettings(Properties props)
    {
        Properties copy = new Properties();
        copy.putAll(props);

        // filter properties, replacing the values of "interesting" properties with those inferred
        // from sourceSettings

        Set<String> hibernateProperties = new HashSet<String>();
        Field[] fields = Environment.class.getFields();
        for(Field f: fields)
        {
            int mod = f.getModifiers();
            if (!Modifier.isPublic(mod) || !Modifier.isStatic(mod) || !Modifier.isFinal(mod))
            {
                continue;
            }

            String value = null;
            try
            {
                Object o = f.get(null);

                if (!(o instanceof String))
                {
                    continue;
                }

                value = (String)o;
            }
            catch(Exception e)
            {
                // ignore, we're not interested in this field
                continue;
            }

            hibernateProperties.add(value);
        }

        // get rid of all hibernate properties ...
        for(String hibernateProperty: hibernateProperties)
        {
            Object o = copy.remove(hibernateProperty);

            if (o != null)
            {
                log.debug("got rid of " + hibernateProperty + "=" + o);
            }
        }

        // and only add those we need ...

        Dialect dialect = sourceSettings.getDialect();
        copy.setProperty(Environment.DIALECT, dialect.getClass().getName());

        ConnectionProvider cp = sourceSettings.getConnectionProvider();

        // because a DatasourceConnectionProvider does not maintain the JNDI name of the datasource
        // we cannot set Environment.DATASOURCE property to the original value, so we use a delegate
        // connection provider instead
        copy.setProperty(Environment.CONNECTION_PROVIDER,
                         DelegateConnectionProvider.class.getName());
        copy.put(AuditEnvironment.CONNECTION_PROVIDER_DELEGATE, cp);

        TransactionFactory tf = sourceSettings.getTransactionFactory();
        if (!(tf instanceof JTATransactionFactory))
        {
            throw new RuntimeException("NOT YET IMPLEMENTED: " +
                                       (tf == null ?
                                        "null TransactionFactory" :
                                        tf.getClass().getName() + " not yet supported!"));
        }

        copy.setProperty(Environment.TRANSACTION_STRATEGY, JTATransactionFactory.class.getName());

        TransactionManagerLookup tml = sourceSettings.getTransactionManagerLookup();
        if (tml == null)
        {
            throw new RuntimeException(
                "NOT YET IMPLEMENTED: null TransactionManagerLookup not supported!");
        }

        copy.setProperty(Environment.TRANSACTION_MANAGER_STRATEGY, tml.getClass().getName());

        // other settings

        // TODO we force session retention to be 'jta', anaylize and come up with test cases
        copy.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");

        copy.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
        copy.setProperty(Environment.SHOW_SQL, "true");

        // look for external definitions of 'hbm2ddl.auto'
        // TODO not sure if I am supposed to look within System, and not use the passed copy
        String s = System.getProperty(HibernateAuditEnvironment.HBM2DDL_AUTO);
        if (s != null)
        {
            // insure consistency
            if (!"validate".equals(s) &&
                !"update".equals(s) &&
                !"create".equals(s) &&
                !"create-drop".equals(s))
            {
                log.warn("'" + s + "' is an invalid " + HibernateAuditEnvironment.HBM2DDL_AUTO +
                         " value, will be ignored!");
            }
            else
            {
                copy.setProperty(Environment.HBM2DDL_AUTO, s);
            }
        }

        return super.buildSettings(copy);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
