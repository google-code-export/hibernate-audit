package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.apache.log4j.Logger;
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
    extends AbstractAuditEventListener implements PostCollectionUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostCollectionUpdateAuditEventListener.class);

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
        log.debug(this + ".onPostUpdateCollection(...)");

        EntityEventContext ec = createAndLogEntityEventContext(event);

//        Object[] state = event.getState();
//        Object[] oldState = event.getOldState();
//        String[] names = ec.persister.getPropertyNames();
//        Type[] types = ec.persister.getPropertyTypes();
//
//        for(int i = 0; i < state.length; i++)
//        {
//            Type type = types[i];
//
//            if (type.isEntityType() || type.isCollectionType() || type.isComponentType())
//            {
//                throw new RuntimeException("NOT YET IMPLEMENTED");
//                //continue;
//            }
//
//            String name = names[i];
//            Object current = state[i];
//            Object old = oldState[i];
//
//            if (current == null && old == null || current != null && current.equals(old))
//            {
//                // nothing really happened here
//                continue;
//            }
//
//            AuditEventPair pair = new AuditEventPair();
//            AuditType fieldType = ec.auditTransaction.getAuditType(type.getReturnedClass());
//            AuditTypeField f = ec.auditTransaction.getAuditTypeField(name, fieldType);
//            pair.setField(f);
//            pair.setValue(current);
//            pair.setEvent(ec.auditEvent);
//            ec.auditTransaction.log(pair);
//        }
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostCollectionUpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
