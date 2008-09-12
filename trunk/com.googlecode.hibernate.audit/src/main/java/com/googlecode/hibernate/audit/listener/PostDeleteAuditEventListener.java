package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.Manager;

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
        log.debug(this + ".onPostUpdate(...)");

        createAndLogEventContext(event);

        // no need for an audit pair

//        Object[] ds = event.getDeletedState();
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
