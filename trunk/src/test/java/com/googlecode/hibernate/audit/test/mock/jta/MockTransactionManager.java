package com.googlecode.hibernate.audit.test.mock.jta;

import org.apache.log4j.Logger;

import javax.transaction.TransactionManager;
import javax.transaction.NotSupportedException;
import javax.transaction.SystemException;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.Transaction;
import javax.transaction.InvalidTransactionException;
import javax.transaction.Status;

/**
 * A mock transaction manager lookup used for JTA tests.
 *
 * There will always only one TransactionManager instance of this kind in a virtual machine address
 * space, and that instance is accessible only via MockTransactionManager.getInstance();
 *
 * Implements a very simple minded "JTA" transaction strategy, associating a "JTA transaction" with
 * thread local
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockTransactionManager implements TransactionManager
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(MockTransactionManager.class);

    public static final String DEFAULT_TRANSACTION_MANAGER_JNDI_NAME =
        "local:MockTransactionManager";

    // Static --------------------------------------------------------------------------------------

    private static MockTransactionManager instance;

    public static synchronized MockTransactionManager getInstance()
    {
        if (instance == null)
        {
            instance = new MockTransactionManager();
        }

        return instance;
    }

    // Attributes ----------------------------------------------------------------------------------

    private ThreadLocal<MockJTATransaction> currentTransaction;

    // Constructors --------------------------------------------------------------------------------

    private MockTransactionManager()
    {
        currentTransaction = new ThreadLocal<MockJTATransaction>();
    }

    // TransactionManager implementation -----------------------------------------------------------

    public void begin() throws NotSupportedException, SystemException
    {
        MockJTATransaction t = currentTransaction.get();

        if (t != null)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        t = new MockJTATransaction(this);
        currentTransaction.set(t);
    }

    /**
     * From Java Transaction API Specifications, Version 1.1, pag. 38.
     *
     * Complete the transaction associated with the current thread. When this method completes,
     * the thread becomes associated with no transaction.
     *
     * @exception RollbackException Thrown to indicate that the transaction has been rolled back
     *            rather than committed.
     * @exception HeuristicMixedException Thrown to indicate that a heuristic decision was made and
     *            that some relevant updates have been committed while others have been rolled back.
     * @exception HeuristicRollbackException Thrown to indicate that a heuristic decision was made
     *            and that all relevant updates have been rolled back.
     * @exception SecurityException Thrown to indicate that the thread is not allowed to commit the
     *            transaction.
     * @exception IllegalStateException Thrown if the current thread is not associated with a
     *            transaction.
     * @exception SystemException Thrown if the transaction manager encounters an unexpected error
     *            condition.
     */
    public void commit()
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
               SecurityException, IllegalStateException, SystemException
    {
        MockJTATransaction t = currentTransaction.get();

        if (t == null)
        {
            throw new IllegalStateException("current thread not associated with a transaction");
        }

        try
        {
            t.commit();
        }
        catch(RollbackException re)
        {
            // just bubble up
            throw re;
        }
        catch(Exception e)
        {
            log.error("commit failed unexpectedly", e);
            throw new RollbackException("transaction rolled back");
        }
        finally
        {
            currentTransaction.set(null);
        }
    }

    public int getStatus() throws SystemException
    {
        MockJTATransaction t = currentTransaction.get();

        if (t == null)
        {
            return Status.STATUS_NO_TRANSACTION;
        }

        return t.getStatus();
    }

    public Transaction getTransaction() throws SystemException
    {
        return currentTransaction.get();
    }

    public void resume(Transaction transaction)
        throws InvalidTransactionException, IllegalStateException, SystemException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void rollback() throws IllegalStateException, SecurityException, SystemException
    {
        MockJTATransaction current = currentTransaction.get();

        if (current == null)
        {
            throw new IllegalStateException("current thread not associated with a transaction");
        }

        try
        {
            current.rollback();
        }
        catch(Exception e)
        {
            log.error("rollback failed unexpectedly", e);
            throw new IllegalStateException("failed to rollback transaction");
        }
        finally
        {
            currentTransaction.set(null);
        }
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        MockJTATransaction current = currentTransaction.get();

        if (current == null)
        {
            throw new IllegalStateException("current thread not associated with a transaction");
        }

        try
        {
            current.setRollbackOnly();
        }
        catch(Exception e)
        {
            log.error("setRollbackOnly failed unexpectedly", e);
            throw new IllegalStateException("failed to set RollbackOnly transaction");
        }
    }

    public void setTransactionTimeout(int i) throws SystemException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Transaction suspend() throws SystemException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    public void start() throws Exception
    {
        log.debug(this + " started");
    }

    public void stop() throws Exception
    {
        log.debug(this + " stopped");
    }

    // Package protected ---------------------------------------------------------------------------

    /**
     * Must be used only by MockJTATransaction having commit() called upon themselves directly.
     * They invoke this method to clean threadlocal of themselves.
     */
    void clearCurrentTransaction(MockJTATransaction me) throws SystemException
    {
        if (getTransaction() != me)
        {
            throw new IllegalArgumentException(me + " is not the current transaction");
        }

        currentTransaction.set(null);
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
