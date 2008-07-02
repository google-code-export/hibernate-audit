package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostInsertEvent;
import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 * 
 * $Id$
 */
public class PostInsertAuditEventListener
    extends AbstractAuditEventListener implements PostInsertEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostInsert(PostInsertEvent event)
    {
        log.debug("onPostInsert(" + event + ")");

        String user = null; // TODO properly determine the user
        logTransaction(event.getSession(), user);

    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
