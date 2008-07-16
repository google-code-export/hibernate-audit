package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.FlushEventListener;
import org.hibernate.event.FlushEvent;
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
public class FlushAuditEventListener
    extends AbstractAuditEventListener implements FlushEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(FlushAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // SaveOrUpdateEventListener implementation ----------------------------------------------------

    public void onFlush(FlushEvent event) throws HibernateException
    {
        log.debug(this + ".onFlush(...)");
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "FlushAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
