package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.HibernateAuditException;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class SaveAuditEventListener
    extends AbstractAuditEventListener implements SaveOrUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(SaveAuditEventListener.class);
    private static final boolean traceEnabled = log.isDebugEnabled();

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public SaveAuditEventListener(Manager m)
    {
        super(m);
    }

    // SaveOrUpdateEventListener implementation ----------------------------------------------------

    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
    {
        try
        {
            if (traceEnabled) { log.debug(this + ".onSaveOrUpdate(" + event + ")"); }

            // this will create an audit transaction and properly register the synchronizations
            createAuditTransaction(event.getSession());
        }
        catch(Throwable t)
        {
            log.error("failed to log save event", t);

            if (suppressed)
            {
                log.warn("Exception propagation and automatic transaction rollback is suppressed! " +
                         "DO NOT USE THIS OPTION IN PRODUCTION!");
                return;
            }

            try
            {
                Transaction tx = event.getSession().getTransaction();
                tx.rollback();
            }
            catch(Throwable t2)
            {
                log.error("could not rollback current transaction", t2);
            }


            // TODO bubble WriteCollisionException up https://jira.novaordis.org/browse/HBA-174
            throw new HibernateAuditException("failed to start audit transaction on save event", t);
        }
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "SaveAuditEventListener[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
