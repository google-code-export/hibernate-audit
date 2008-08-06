package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.SaveOrUpdateEventListener;
import org.hibernate.event.SaveOrUpdateEvent;
import org.hibernate.HibernateException;
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
public class SaveOrUpdateAuditEventListener
    extends AbstractAuditEventListener implements SaveOrUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(SaveOrUpdateAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // SaveOrUpdateEventListener implementation ----------------------------------------------------

    public void onSaveOrUpdate(SaveOrUpdateEvent event) throws HibernateException
    {
        log.debug(this + ".onSaveOrUpdate(...)");

        // this will create an audit transaction and properly register the synchronizations
        createAuditTransaction(event.getSession());
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "SaveOrUpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
