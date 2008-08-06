package com.googlecode.hibernate.audit.listener;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.event.EventSource;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.HibernateAudit;

import java.security.Principal;

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
     * Creates in-memory instance of the audit transaction the current event occured in scope of
     * and perform all necessary associations (with the thread, register synchronizations, etc.).
     * No unnecessary creation occurs if the audit transaction instance already exists.
     */
    protected AuditTransaction createAuditTransaction(EventSource auditedSession)
    {
        Transaction ht = auditedSession.getTransaction();
        AuditTransaction at = HibernateAudit.getCurrentAuditTransaction();

        if (at != null)
        {
            // already logged
            assert ht == at.getTransaction();
            return at;
        }

        Principal p = HibernateAudit.getPrincipal();
        at = new AuditTransaction(auditedSession, p);
        HibernateAudit.setCurrentAuditTransaction(at);

        log.debug(this + " created");

        return at;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
