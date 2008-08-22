package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.FlushEventListener;
import org.hibernate.event.FlushEvent;
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
public class FlushAuditEventListener
    extends AbstractAuditEventListener implements FlushEventListener
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public FlushAuditEventListener(Manager m)
    {
        super(m);
    }

    // SaveOrUpdateEventListener implementation ----------------------------------------------------

    public void onFlush(FlushEvent event) throws HibernateException
    {
        //log.debug(this + ".onFlush(...)");
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
