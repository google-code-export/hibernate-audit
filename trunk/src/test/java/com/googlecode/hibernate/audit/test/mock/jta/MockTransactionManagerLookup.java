package com.googlecode.hibernate.audit.test.mock.jta;

import org.hibernate.transaction.TransactionManagerLookup;
import org.hibernate.HibernateException;

import javax.transaction.TransactionManager;
import javax.transaction.Transaction;
import java.util.Properties;

/**
 * A mock Hibernate transaction manager lookup used for JTA tests.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockTransactionManagerLookup implements TransactionManagerLookup
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // TransactionManagerLookup implementation -----------------------------------------------------

    public TransactionManager getTransactionManager(Properties props) throws HibernateException
    {
        return MockTransactionManager.getInstance(); 
    }

    public String getUserTransactionName()
    {
        return MockUserTransaction.DEFAULT_USER_TRANSACTION_JNDI_NAME;
    }

    public Object getTransactionIdentifier(Transaction transaction)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
