package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostDeleteEvent;
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
public class PostDeleteAuditEventListener
    extends AbstractAuditEventListener implements PostDeleteEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostDeleteAuditEventListener.class);
    private static final boolean traceEnabled = log.isDebugEnabled();

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public PostDeleteAuditEventListener(Manager m)
    {
        super(m);
    }

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostDelete(PostDeleteEvent event)
    {
        try
        {
            if (traceEnabled) { log.debug(this + ".onPostDelete(" + event + ")"); }

            createAndLogEventContext(event);
        }
        catch(Throwable t)
        {
            log.error("failed to log post-delete event", t);

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
            throw new HibernateAuditException("failed to log post-delete event", t);
        }
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostDeleteAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
