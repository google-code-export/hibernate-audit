package com.googlecode.hibernate.audit;

import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditTransaction;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class DeltaEngine
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DeltaEngine.class);

    // Static --------------------------------------------------------------------------------------

    /**
     * @throws IllegalArgumentException - if such a transaction does not exist.
     */
    public static Object applyDelta(SessionFactory sessionFactory,
                                    Object initialState,
                                    Long transactionId)
        throws Exception
    {
        Session s = null;
        Transaction t = null;

        try
        {
            s = sessionFactory.openSession();
            t = s.beginTransaction();

            AuditTransaction at = (AuditTransaction)s.get(AuditTransaction.class, transactionId);

            if (at == null)
            {
                throw new IllegalArgumentException("No audit transaction with id " +
                                                   transactionId + " exists");
            }

            t.commit();

            return null;
        }
        catch(Exception e)
        {
            if (t != null)
            {
                try
                {
                    t.rollback();
                }
                catch(Exception e2)
                {
                    log.error("failed to rollback Hibernate transaction", e2);
                }
            }

            if (s != null)
            {
                try
                {
                    s.close();
                }
                catch(Exception e2)
                {
                    log.error("failed to close Hibernate session", e2);
                }
            }

            throw e;
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
