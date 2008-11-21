package com.googlecode.hibernate.audit.model;

import org.hibernate.cfg.SettingsFactory;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.Environment;
import org.hibernate.dialect.Dialect;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.transaction.TransactionFactory;
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
import com.googlecode.hibernate.audit.util.SyntheticTransactionManagerLookup;

import javax.transaction.TransactionManager;

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
    private String userTransactionNameFromProperties;

    // Constructors --------------------------------------------------------------------------------

    AuditSettingsFactory(Settings sourceSettings)
    {
        this.sourceSettings = sourceSettings;
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public Settings buildSettings(Properties props)
    {
        reset();
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
        for(String hibernatePropertyName: hibernateProperties)
        {
            Object o = copy.remove(hibernatePropertyName);

            if (o != null)
            {
                log.debug("got rid of " + hibernatePropertyName + "=" + o);
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
        copy.setProperty(Environment.TRANSACTION_STRATEGY, tf.getClass().getName());

        // other default settings

        // TODO we force session retention to be 'jta', anaylize and come up with test cases
        copy.setProperty(Environment.CURRENT_SESSION_CONTEXT_CLASS, "jta");
        copy.setProperty(Environment.USE_SECOND_LEVEL_CACHE, "false");
        copy.setProperty(Environment.SHOW_SQL, "true");

        // look for "hba." properties and overwrite correspoding "hibernate." properties (HBA-102)
        // TODO not sure if I am supposed to look within System, and not use the passed copy
        Properties systemProperties = System.getProperties();
        for(Object o: systemProperties.keySet())
        {
            String key = (String)o;

            if (key.startsWith(HibernateAuditEnvironment.HBA_PROPERTY_PREFIX))
            {
                String root = key.substring(HibernateAuditEnvironment.HBA_PROPERTY_PREFIX.length());

                String hibernatePropertyName = root;

                if (!hibernateProperties.contains(hibernatePropertyName))
                {
                    hibernatePropertyName = "hibernate." + root;

                    if (!hibernateProperties.contains(hibernatePropertyName))
                    {
                        hibernatePropertyName = null;
                    }
                }

                if (hibernatePropertyName != null)
                {
                    String hbaPropertyValue = System.getProperty(key);

                    if (hbaPropertyValue != null)
                    {
                        // valid "hba." property, use its value

                        if (HibernateAuditEnvironment.HBM2DDL_AUTO.equals(key))
                        {
                            // extra checks for "hbm2ddl.auto", older code but valid check

                            if (!"validate".equals(hbaPropertyValue) &&
                                !"update".equals(hbaPropertyValue) &&
                                !"create".equals(hbaPropertyValue) &&
                                !"create-drop".equals(hbaPropertyValue))
                            {
                                log.warn(
                                    "'" + hbaPropertyValue + "' is an invalid " +
                                    HibernateAuditEnvironment.HBA_PROPERTY_PREFIX +
                                    root + " value, will be ignored!");

                                continue;
                            }
                        }
                        else if (HibernateAuditEnvironment.USER_TRANSACTION.equals(key))
                        {
                            userTransactionNameFromProperties = hbaPropertyValue;
                        }

                        copy.setProperty(hibernatePropertyName, hbaPropertyValue);
                    }
                }
            }
        }

        return super.buildSettings(copy);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    @Override
    protected TransactionManagerLookup createTransactionManagerLookup(Properties properties)
    {
        TransactionManagerLookup originalTML = sourceSettings.getTransactionManagerLookup();

        if (originalTML == null)
        {
            return null;
        }

        TransactionManager origTM = originalTML.getTransactionManager(properties);

        String userTransactionName = userTransactionNameFromProperties;
        if (userTransactionName == null)
        {
            userTransactionName = originalTML.getUserTransactionName();
        }
        return new SyntheticTransactionManagerLookup(origTM, userTransactionName);
    }

    @Override
    protected TransactionFactory createTransactionFactory(Properties properties)
    {
        TransactionFactory sourceTransactionFactory = sourceSettings.getTransactionFactory();

        if (userTransactionNameFromProperties != null)
        {
            if (properties == null)
            {
                properties = new Properties();
            }

            properties.setProperty(Environment.USER_TRANSACTION, userTransactionNameFromProperties);
            sourceTransactionFactory.configure(properties);
        }
        
        return sourceTransactionFactory; 
    }

    // Private -------------------------------------------------------------------------------------

    private void reset()
    {
        userTransactionNameFromProperties = null;
    }

    // Inner classes -------------------------------------------------------------------------------
}
