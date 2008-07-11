package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PostUpdateEvent;
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
public class PostUpdateAuditEventListener
    extends AbstractAuditEventListener implements PostUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostUpdateAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostUpdate(PostUpdateEvent event)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }
    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostUpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
