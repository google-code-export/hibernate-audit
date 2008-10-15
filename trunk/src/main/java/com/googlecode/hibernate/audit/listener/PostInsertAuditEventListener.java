package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.type.Type;
import org.hibernate.type.CollectionType;
import org.hibernate.type.EntityType;
import org.hibernate.Transaction;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.util.Hibernate;
import com.googlecode.hibernate.audit.HibernateAuditException;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 * 
 * $Id$
 */
public class PostInsertAuditEventListener
    extends AbstractAuditEventListener implements PostInsertEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostInsertAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public PostInsertAuditEventListener(Manager m)
    {
        super(m);
    }

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostInsert(PostInsertEvent event)
    {
        try
        {
            log.debug(this + ".onPostInsert(" + event + ")");
            log(event);
        }
        catch(Throwable t)
        {
            log.error("failed to log post-insert event", t);

            try
            {
                Transaction tx = event.getSession().getTransaction();
                tx.rollback();
            }
            catch(Throwable t2)
            {
                log.error("could not rollback current transaction", t2);
            }

            throw new HibernateAuditException("failed to log post-insert event", t);
        }
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostInsertAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private void log(PostInsertEvent event) throws Exception
    {
        EventContext ctx = createAndLogEventContext(event);

        // TODO maybe there's no need to iterate over *all* properties, maybe the state contains
        //      only the new properties, look at how onPostUpdate() was implemented and possibly
        //      refactor this implementation as well

        for (String name : ctx.persister.getPropertyNames())
        {
            Object value = ctx.persister.getPropertyValue(ctx.entity, name, ctx.mode);

            if (value == null)
            {
                // TODO it is possible that the previous value was not null, and this insert
                // nullifies? Add a test for this
                continue;
            }

            Type hibernateType = ctx.persister.getPropertyType(name);

            AuditType auditType = null;
            AuditEventPair pair = null;

            if (hibernateType.isEntityType())
            {
                EntityType et = (EntityType)hibernateType;
                String en = et.getAssociatedEntityName();
                EntityPersister ep = ctx.factory.getEntityPersister(en);
                Class ec = Hibernate.guessEntityClass(et, ep, ctx.mode);
                Class idc = ep.getIdentifierType().getReturnedClass();
                auditType = typeCache.getAuditEntityType(idc, ec);
                value = ep.getIdentifier(value, ctx.mode);
                pair = new AuditEventPair();
            }
            else if (hibernateType.isCollectionType())
            {
                // figure out collection type
                CollectionType ct = (CollectionType)hibernateType;
                Class cc = Hibernate.collectionTypeToClass(ct);

                // figure out element type
                Type et = ct.getElementType(ctx.factory);

                if (!(et instanceof EntityType))
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                EntityType eet = (EntityType)et;
                String een = eet.getAssociatedEntityName();
                EntityPersister eep = ctx.factory.getEntityPersister(een);
                Class eec = Hibernate.guessEntityClass(eet, eep, ctx.mode);

                Collection collection = (Collection)((PersistentCollection)value).getValue();
                List<Long> ids = new ArrayList<Long>();
                for(Object o: collection)
                {
                    Long mid  = (Long)eep.getIdentifier(o, ctx.mode);
                    ids.add(mid);
                }

                if (ids.isEmpty())
                {
                    // this is an insert event, inserting an empty collection is a noop, don't
                    // record in audit trail
                    continue;
                }

                auditType = typeCache.getAuditCollectionType(cc, eec);
                pair = new AuditEventCollectionPair();
                ((AuditEventCollectionPair)pair).setIds(ids);
                value = null;
            }
            else
            {
                auditType = typeCache.getAuditPrimitiveType(hibernateType.getReturnedClass());
                pair = new AuditEventPair();
            }

            AuditTypeField f = typeCache.getAuditTypeField(name, auditType);

            pair.setField(f);
            pair.setValue(value);
            pair.setEvent(ctx.auditEvent);

            if (hibernateType.isComponentType())
            {
                // createComponent(...);
                // https://jira.novaordis.org/browse/HBA-32

                //throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            ctx.auditTransaction.log(pair);
        }
    }

    // Inner classes -------------------------------------------------------------------------------

}
