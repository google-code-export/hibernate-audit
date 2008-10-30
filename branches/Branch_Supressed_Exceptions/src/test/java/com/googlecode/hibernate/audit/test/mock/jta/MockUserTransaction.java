package com.googlecode.hibernate.audit.test.mock.jta;

import org.apache.log4j.Logger;

import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.UserTransaction;
import javax.transaction.TransactionManager;

/**
 * A mock UserTransaction needed for JTA tests.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockUserTransaction implements UserTransaction
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(MockUserTransaction.class);

    public static final String DEFAULT_USER_TRANSACTION_JNDI_NAME = "local:MockUserTransaction";

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // most likely a "mock" one
    private TransactionManager tm;

    // Constructors --------------------------------------------------------------------------------

    public MockUserTransaction(TransactionManager tm)
    {
        this.tm = tm;

        log.debug(this + " created");
    }

    // UserTransaction implementation --------------------------------------------------------------

    public void begin() throws NotSupportedException, SystemException
    {
        tm.begin();
    }

    public void commit()
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
               SecurityException, IllegalStateException, SystemException
    {
        tm.commit();
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException
    {
        tm.rollback();
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        tm.setRollbackOnly();
    }

    public int getStatus() throws SystemException
    {
        return tm.getStatus();
    }

    public void setTransactionTimeout(int i) throws SystemException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "MockUserTransaction[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
