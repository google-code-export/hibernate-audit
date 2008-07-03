package com.googlecode.hibernate.audit.listener;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.event.EventSource;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.HibernateAudit;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
abstract class AbstractAuditEventListener implements AuditEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AbstractAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // AuditEventListener implementation -----------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    /**
     * Make sure we log the transaction the current event occured in scope of.
     */
    protected void logTransaction(EventSource auditedSession, String user)
    {
        Transaction ht = auditedSession.getTransaction();
        AuditTransaction at = HibernateAudit.getCurrentAuditTransaction();

        if (at != null)
        {
            // already logged

            assert ht == at.getTransaction();
            return;
        }

        at = new AuditTransaction(auditedSession, user);

        at.log();

        HibernateAudit.setCurrentAuditTransaction(at);

        log.debug("logged transaction " + ht);
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
