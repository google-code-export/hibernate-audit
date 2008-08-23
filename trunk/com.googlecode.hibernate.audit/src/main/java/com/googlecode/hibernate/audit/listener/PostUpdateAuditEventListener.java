package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.type.Type;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditType;

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

    public PostUpdateAuditEventListener(Manager m)
    {
        super(m);
    }

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostUpdate(PostUpdateEvent event)
    {
        log.debug(this + ".onPostUpdate(...)");

        EntityEventContext ec = createAndLogEntityEventContext(event);

        Object[] state = event.getState();
        Object[] oldState = event.getOldState();
        String[] names = ec.persister.getPropertyNames();
        Type[] types = ec.persister.getPropertyTypes();

        for(int i = 0; i < state.length; i++)
        {
            String name = names[i];
            Type type = types[i];
            Object current = state[i];
            Object old = oldState[i];

            if (current == null && old == null || current != null && current.equals(old))
            {
                // nothing really happened here
                continue;
            }

            AuditEventPair pair = new AuditEventPair();
            AuditType fieldType = ec.auditTransaction.getAuditType(type.getReturnedClass());
            AuditTypeField f = ec.auditTransaction.getAuditTypeField(name, fieldType);
            pair.setField(f);
            pair.setValue(current);
            pair.setEvent(ec.auditEvent);
            ec.auditTransaction.log(pair);
        }
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
