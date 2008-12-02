package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.AbstractEvent;
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
        log("onPostDelete", event);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostDeleteAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // AbstractAuditEventListener overrides --------------------------------------------------------

    @Override
    protected String getListenerType()
    {
        return "post-delete";
    }

    @Override
    protected void listenerTypeDependentLog(AbstractEvent event) throws Exception
    {
        createAndLogEventContext(event);
    }

    @Override
    protected boolean isDisabledOn(AbstractEvent event)
    {
        PostDeleteEvent pde = (PostDeleteEvent)event;
        Class c = pde.getEntity().getClass();
        return isDisabledOn(c, event.getSession().getFactory());
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
