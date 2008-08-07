package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.FlushEntityEventListener;
import org.hibernate.event.FlushEntityEvent;
import org.hibernate.HibernateException;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class FlushEntityAuditEventListener
    extends AbstractAuditEventListener implements FlushEntityEventListener
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // SaveOrUpdateEventListener implementation ----------------------------------------------------

    public void onFlushEntity(FlushEntityEvent event) throws HibernateException
    {
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "FlushEntityAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
