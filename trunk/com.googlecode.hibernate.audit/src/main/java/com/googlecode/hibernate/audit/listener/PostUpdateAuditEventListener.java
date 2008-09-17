package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.type.Type;
import org.hibernate.type.EntityType;
import org.hibernate.Transaction;
import org.hibernate.persister.entity.EntityPersister;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.HibernateAuditException;
import com.googlecode.hibernate.audit.util.Hibernate;

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
        EventContext ctx = createAndLogEventContext(event);

        Object[] state = event.getState();
        Object[] oldState = event.getOldState();
        String[] names = ctx.persister.getPropertyNames();
        Type[] types = ctx.persister.getPropertyTypes();

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
                // let it pass TODO HBA-32
                // throw new RuntimeException("NOT YET IMPLEMENTED");
                continue;
            }

            String name = names[i];
            Object current = state[i];
            Object old = oldState[i];

            if (type.isEntityType())
            {
                // we're dealing with entities here, then the "values" are actually their ids
                current = current == null ? null : ctx.session.getIdentifier(current);
                old = old == null ? null : ctx.session.getIdentifier(old);
            }

            if (current == null && old == null || current != null && current.equals(old))
            {
                // nothing really happened here, nothing changes, exit ...
                continue;
            }

            AuditEventPair pair = new AuditEventPair();
            AuditType fieldType = null;

            if (type.isEntityType())
            {
                EntityType et = (EntityType)type;
                String en = et.getAssociatedEntityName();
                EntityPersister ep = ctx.factory.getEntityPersister(en);
                Class ec = Hibernate.guessEntityClass(et, ep, ctx.mode);
                Class idc = ep.getIdentifierType().getReturnedClass();
                fieldType = ctx.auditTransaction.getAuditType(ec, idc);
            }
            else
            {
                fieldType = ctx.auditTransaction.getAuditType(type.getReturnedClass());
            }

            AuditTypeField f = ctx.auditTransaction.getAuditTypeField(name, fieldType);
            pair.setField(f);
            pair.setValue(current);
            pair.setEvent(ctx.auditEvent);
            ctx.auditTransaction.log(pair);
        }
    }

    // Inner classes -------------------------------------------------------------------------------

}
