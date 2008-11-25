package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.AbstractCollectionEvent;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.hibernate.event.PostCollectionRecreateEvent;
import org.hibernate.event.AbstractEvent;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.CollectionEntry;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.hibernate.type.EntityType;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.util.Hibernate;
import com.googlecode.hibernate.audit.delta.ChangeType;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Collection event handling is very similar whether is a post update collection event, post
 * recreate collection event, etc, so we're encapsulating common code in a base superclass.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
abstract class AbstractAuditCollectionEventListener extends AbstractAuditEventListener
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    protected AbstractAuditCollectionEventListener(Manager m)
    {
        super(m);
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // AbstractAuditEventListener overrides --------------------------------------------------------

    @Override
    protected void listenerTypeDependentLog(AbstractEvent e) throws Exception
    {
        AbstractCollectionEvent ace = (AbstractCollectionEvent)e;

        EventContext ctx = createAndLogEventContext(ace);

        // figure out collection type
        PersistentCollection c = ace.getCollection();
        CollectionEntry ce = ctx.session.getPersistenceContext().getCollectionEntry(c);
        CollectionPersister cp = ce.getLoadedPersister();
        CollectionType ct = cp.getCollectionType();
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

        AuditCollectionType ft = typeCache.getAuditCollectionType(cc, eec);
        String role = cp.getRole();
        String fn = Hibernate.roleToVariableName(role);
        AuditTypeField f = typeCache.getAuditTypeField(fn, ft);

        AuditEventCollectionPair pair = new AuditEventCollectionPair();
        pair.setEvent(ctx.auditEvent);
        pair.setField(f);

        List<Long> ids = new ArrayList<Long>();
        for(Iterator i = c.entries(cp); i.hasNext(); )
        {
            Object entry = i.next();
            Long id = (Long)ctx.session.getIdentifier(entry);
            ids.add(id);
        }

        pair.setIds(ids);

        ctx.auditTransaction.log(pair);
    }

    // Protected -----------------------------------------------------------------------------------

    @Override
    protected void populateEventContextWithEventSpecificInfo(EventContext c, AbstractEvent e)
    {
        if (e instanceof PostCollectionUpdateEvent)
        {
            PostCollectionUpdateEvent pcue = (PostCollectionUpdateEvent)e;
            c.changeType = ChangeType.UPDATE;
            c.session = pcue.getSession();
            c.entityId = pcue.getAffectedOwnerIdOrNull();
            c.entity = pcue.getAffectedOwnerOrNull();
            c.entityName = pcue.getAffectedOwnerEntityName();
            c.persister = c.session.getEntityPersister(c.entityName, c.entity);
        }
        else if (e instanceof PostCollectionRecreateEvent)
        {
            PostCollectionRecreateEvent pcre = (PostCollectionRecreateEvent)e;
            c.changeType = ChangeType.UPDATE;
            c.session = pcre.getSession();
            c.entityId = pcre.getAffectedOwnerIdOrNull();
            c.entity = pcre.getAffectedOwnerOrNull();
            c.entityName = pcre.getAffectedOwnerEntityName();
            c.persister = c.session.getEntityPersister(c.entityName, c.entity);
        }
        else
        {
             throw new IllegalArgumentException("unsupported event type " + e);
        }
    }

    @Override
    protected boolean isDisabledOn(AbstractEvent event)
    {
        AbstractCollectionEvent ace = (AbstractCollectionEvent)event;
        Object o = ace.getAffectedOwnerOrNull();

        if (o == null)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        return isDisabledOn(o.getClass());
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
