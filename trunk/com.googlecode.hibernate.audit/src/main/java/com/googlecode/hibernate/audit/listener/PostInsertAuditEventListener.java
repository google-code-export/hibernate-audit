package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.EventSource;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.type.Type;
import org.hibernate.type.CollectionType;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.util.Hibernate;

import java.io.Serializable;
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

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostInsert(PostInsertEvent event)
    {
        log.debug(this + ".onPostInsert(...)");

        EventSource session = event.getSession();
        AuditTransaction aTx = createAuditTransaction(session);

        Serializable id = event.getId();
        Object entity = event.getEntity();
        String entityClassName = entity.getClass().getName();

        log.debug(this + " handles " + entityClassName + "[" + id + "]");

        AuditEntityType at = (AuditEntityType)aTx.getAuditType(entity.getClass(), id.getClass());

        // TODO currently we only support Long as ids, we may need to generalize this
        if (!(id instanceof Long))
        {
            throw new IllegalArgumentException(
                "audited entity " + entityClassName + "'s id is not a Long, " +
                "so it is currently not supported");
        }

        AuditEvent ae = new AuditEvent();
        ae.setTransaction(aTx);
        ae.setType(AuditEventType.INSERT);
        ae.setTargetId((Long)id);
        ae.setTargetType(at);

        // even if it may seem redundant, log the event here in case the entity state is empty and
        // no pairs will be logged (see HBA-74).
        aTx.log(ae);

        EntityPersister persister = event.getPersister();
        EntityMode mode = persister.guessEntityMode(entity);

        // log properties

        for (String name : persister.getPropertyNames())
        {
            Object value = persister.getPropertyValue(entity, name, mode);

            log.debug(this + " handles " + name + "=" + value);

            if (value == null)
            {
                // TODO it is possible that the previous value was not null, and this insert
                // nullifies? Add a test for this
                continue;
            }

            SessionFactoryImpl sf = (SessionFactoryImpl)session.getSessionFactory();
            Type hibernateType = persister.getPropertyType(name);

            AuditType auditType = null;
            AuditEventPair pair = null;

            if (hibernateType.isEntityType())
            {
                if (!hibernateType.isAssociationType())
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                if (value == null)
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                Class entityClass = hibernateType.getReturnedClass();
                Class idClass = sf.getIdentifierType(entityClass.getName()).getReturnedClass();

                auditType = aTx.getAuditType(entityClass, idClass);

                // TODO Refactor this into something more palatable
                String entityName = session.getEntityName(value);
                EntityPersister associatedEntityPersister = sf.getEntityPersister(entityName);

                // the entity mode is a session characteristic, so using the previously determined
                // entity mode (TODO: verify this is really true)
                value = associatedEntityPersister.getIdentifier(value, mode);
                pair = new AuditEventPair();
            }
            else if (hibernateType.isCollectionType())
            {
                // figure out the member type and class
                CollectionType collectionType = (CollectionType)hibernateType;
                Type memberType = collectionType.getElementType(sf);
                Class memberClass = memberType.getReturnedClass();
                Class collectionClass = Hibernate.collectionTypeToClass(collectionType);

                auditType = aTx.getAuditType(collectionClass, memberClass);

                String entityName = memberClass.getName();
                EntityPersister memberPersister = sf.getEntityPersister(entityName);
                Collection collection = (Collection)((PersistentCollection)value).getValue();
                List<Long> ids = new ArrayList<Long>();
                for(Object o: collection)
                {
                    // the entity mode is a session characteristic, so using the previously
                    // determined entity mode (TODO: verify this is really true)
                    Long mid  = (Long)memberPersister.getIdentifier(o, mode);
                    ids.add(mid);
                }

                pair = new AuditEventCollectionPair();
                ((AuditEventCollectionPair)pair).setIds(ids);
                value = null;
            }
            else
            {
                auditType = aTx.getAuditType(hibernateType.getReturnedClass());
                pair = new AuditEventPair();
            }

            AuditTypeField f = aTx.getAuditTypeField(name, auditType);

            pair.setField(f);
            pair.setValue(value);
            pair.setEvent(ae);

            if (hibernateType.isComponentType())
            {
                // createComponent(...);
                // https://jira.novaordis.org/browse/HBA-32

                //throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            aTx.log(pair);
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

    // Inner classes -------------------------------------------------------------------------------

}
