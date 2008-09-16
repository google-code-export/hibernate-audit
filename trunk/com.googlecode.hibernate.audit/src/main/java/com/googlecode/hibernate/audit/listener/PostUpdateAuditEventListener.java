package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.type.Type;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.HibernateAuditException;

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
        try
        {
            log.debug(this + ".onPostUpdate(" + event + ")");
            log(event);
        }
        catch(Throwable t)
        {
            try
            {
                Transaction tx = event.getSession().getTransaction();
                tx.rollback();
            }
            catch(Throwable t2)
            {
                log.error("could not rollback current transaction", t2);
            }

            throw new HibernateAuditException("failed to log post-update event", t);
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

    private void log(PostUpdateEvent event)
    {
        EventContext ec = createAndLogEventContext(event);

        Object[] state = event.getState();
        Object[] oldState = event.getOldState();
        String[] names = ec.persister.getPropertyNames();
        Type[] types = ec.persister.getPropertyTypes();

        for(int i = 0; i < state.length; i++)
        {
            Type type = types[i];

            if (type.isCollectionType())
            {
                // ignore, will be handled by PostCollectionUpdate listener
                continue;
            }

            if (type.isComponentType())
            {
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            String name = names[i];
            Object current = state[i];
            Object old = oldState[i];

            if (current == null && old == null || current != null && current.equals(old))
            {
                // nothing really happened here, nothing changes, exit ...
                continue;
            }

            if (type.isEntityType())
            {
                throw new RuntimeException("ENTITY MEMBER CHANGED, NOT YET IMPLEMENTED");
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

    // Inner classes -------------------------------------------------------------------------------

}
