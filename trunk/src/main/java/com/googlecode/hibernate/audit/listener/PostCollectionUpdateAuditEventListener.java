package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.event.PostCollectionUpdateEvent;
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
public class PostCollectionUpdateAuditEventListener
    extends AbstractAuditCollectionEventListener implements PostCollectionUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public PostCollectionUpdateAuditEventListener(Manager m)
    {
        super(m);
    }

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostUpdateCollection(PostCollectionUpdateEvent event)
    {
        log("onPostUpdateCollection", event);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostCollectionUpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // AbstractAuditEventListener overrides --------------------------------------------------------

    @Override
    protected String getListenerType()
    {
        return "post-collection-update";
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
