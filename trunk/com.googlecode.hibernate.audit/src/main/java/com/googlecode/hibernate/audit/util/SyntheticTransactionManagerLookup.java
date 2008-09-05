package com.googlecode.hibernate.audit.util;

import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.HibernateException;

import javax.transaction.TransactionManager;
import java.util.Properties;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class SyntheticTransactionManagerLookup implements TransactionManagerLookup
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private TransactionManager tm;
    private String userTransactionName;

    // Constructors --------------------------------------------------------------------------------

    public SyntheticTransactionManagerLookup(TransactionManager tm, String userTransactionName)
    {
        this.tm = tm;
        this.userTransactionName = userTransactionName;
    }

    // TransactionManagerLookup implementation -----------------------------------------------------

    public TransactionManager getTransactionManager(Properties props) throws HibernateException
    {
        return tm;
    }

    public String getUserTransactionName()
    {
        return userTransactionName;
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
