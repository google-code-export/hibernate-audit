package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRecreateEvent;
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
public class PostCollectionRecreateAuditEventListener
    extends AbstractAuditCollectionEventListener implements PostCollectionRecreateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostCollectionRecreateAuditEventListener.class);

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
        log.debug(this + ".onPostRecreateCollection(...)");
        handleCollectionEvent(event);
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
