package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRecreateEvent;
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
        log("onPostRecreateCollection", event);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostCollectionRecreateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // AbstractAuditEventListener overrides --------------------------------------------------------

    @Override
    protected String getListenerType()
    {
        return "post-collection-recreate";
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
