package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.AbstractEvent;
import org.hibernate.type.Type;
import org.hibernate.type.EntityType;
import org.hibernate.persister.entity.EntityPersister;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.collision.WriteCollisionDetector;
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

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    /**
     * Convenience cache, to save a method call per event occurence.
     */
    private WriteCollisionDetector writeCollisionDetector;

    // Constructors --------------------------------------------------------------------------------

    public PostUpdateAuditEventListener(Manager m)
    {
        super(m);

        writeCollisionDetector = m.getWriteCollisionDetector();
    }

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostUpdate(PostUpdateEvent event)
    {
        log("onPostUpdate", event);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostUpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // AbstractAuditEventListener overrides --------------------------------------------------------

    @Override
    protected String getListenerType()
    {
        return "post-update";
    }

    @Override
    protected void listenerTypeDependentLog(AbstractEvent event) throws Exception
    {
        PostUpdateEvent pue = (PostUpdateEvent)event;
        
        EventContext ctx = createAndLogEventContext(pue);

        Object[] state = pue.getState();
        Object[] oldState = pue.getOldState();
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
            Object current = state == null ? null : state[i];
            Object old = oldState == null ? null : oldState[i];

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
                fieldType = typeCache.getAuditEntityType(idc, ec);
            }
            else
            {
                fieldType = typeCache.getAuditPrimitiveType(type.getReturnedClass());
            }

            AuditTypeField f = typeCache.getAuditTypeField(name, fieldType);

            // noop if collision detection disabled
            writeCollisionDetector.
                detectCollision(ctx.factory, ctx.entityName, ctx.entityId, f.getName(), old);

            pair.setField(f);
            pair.setValue(current);
            pair.setEvent(ctx.auditEvent);
            ctx.auditTransaction.log(pair);
        }
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
