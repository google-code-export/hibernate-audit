package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.event.AbstractEvent;
import org.hibernate.HibernateException;
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
public class SaveAuditEventListener
    extends AbstractAuditEventListener implements SaveOrUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

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
        log("onSaveOrUpdate", event);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "SaveAuditEventListener[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // AbstractAuditEventListener overrides --------------------------------------------------------

    @Override
    protected String getListenerType()
    {
        return "save";
    }

    @Override
    protected void listenerTypeDependentLog(AbstractEvent event) throws Exception
    {
        // this will create an audit transaction and properly register the synchronizations
        createAuditTransaction(event.getSession());
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
