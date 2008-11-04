package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRecreateEvent;
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
public class PostCollectionRecreateAuditEventListener
    extends AbstractAuditCollectionEventListener implements PostCollectionRecreateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostCollectionRecreateAuditEventListener.class);
    private static final boolean traceEnabled = log.isTraceEnabled();

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public PostCollectionRecreateAuditEventListener(Manager m)
    {
        super(m);
    }

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostRecreateCollection(PostCollectionRecreateEvent event)
    {
        try
        {
            if (traceEnabled) { log.trace(this + ".onPostRecreateCollection(" + event + ")"); }

            logCollectionEvent(event);
        }
        catch(Throwable t)
        {
            log.error("failed to log post-collection-recreate event", t);

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
            throw new HibernateAuditException("failed to log post-collection-recreate event", t);
        }
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostCollectionRecreateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
