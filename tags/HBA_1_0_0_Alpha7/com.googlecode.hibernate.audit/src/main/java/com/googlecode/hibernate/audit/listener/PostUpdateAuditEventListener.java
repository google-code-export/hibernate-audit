package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.EventSource;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.type.Type;
import org.hibernate.type.CollectionType;
import org.hibernate.impl.SessionFactoryImpl;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.util.Hibernate;

import java.io.Serializable;
import java.util.Map;
import java.util.Collection;
import java.util.ArrayList;
import java.util.List;

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

    /**
     * TODO lots of duplicate code with PostInsertAuditEventListener. Factor out in superclass.
     */
    public void onPostUpdate(PostUpdateEvent event)
    {
        EventSource session = event.getSession();
        AuditTransaction aTx = createAuditTransaction(session);

        Serializable id = event.getId();
        Object entity = event.getEntity();
        String entityClassName = entity.getClass().getName();

        Manager manager = getManager();
        Serializable newLGId = manager.getLogicalGroupId(session, id, entity);
        Serializable currentLGId  = aTx.getLogicalGroupId();

        if (currentLGId == null)
        {
            if (newLGId != null)
            {
                aTx.setLogicalGroupId(newLGId);
            }
        }
        else if (!currentLGId.equals(newLGId))
        {
            throw new IllegalStateException(
                "NOT YET IMPLEMENTED: inconsistent logical groups, current: " + currentLGId +
                ", new: " + newLGId);
        }

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
        ae.setType(AuditEventType.UPDATE);
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

                // TODO Refactor this into something more palatable
                String entityName = session.getEntityName(value);
                EntityPersister assocdEntityPersister = sf.getEntityPersister(entityName);

                // TODO verify if the following assumption is true:
                // the entity mode is a session characteristic, so using the previously determined
                // entity mode everywhere associated entity mode is needed

                Class entityClass = hibernateType.getReturnedClass();

                if (Map.class.equals(entityClass))
                {
                    // this is what Hibernate returns when it cannot figure out the class,
                    // most likley due to the fact that audited application uses entity names and
                    // custom tuplizers
                    EntityTuplizer t = assocdEntityPersister.getEntityMetamodel().getTuplizer(mode);
                    entityClass = t.getMappedClass();
                }

                Class idClass = assocdEntityPersister.getIdentifierType().getReturnedClass();
                auditType = aTx.getAuditType(entityClass, idClass);
                value = assocdEntityPersister.getIdentifier(value, mode);
                pair = new AuditEventPair();
            }
            else if (hibernateType.isCollectionType())
            {
                // figure out the member type and class
                CollectionType collectionType = (CollectionType)hibernateType;
                Class collectionClass = Hibernate.collectionTypeToClass(collectionType);

                Collection collection = (Collection)((PersistentCollection)value).getValue();

                List<Long> ids = new ArrayList<Long>();
                EntityPersister memberEntityPersister = null;
                String memberEntityName = null;

                // TODO iterating over the members of the collection to figure out the type is not
                //      a good idea. The mechanism breaks when faced with empty collections.
                for(Object o: collection)
                {
                    String s = session.getEntityName(o);

                    if (memberEntityName == null)
                    {
                        memberEntityName = s;
                    }
                    else if (!memberEntityName.equals(s))
                    {
                        throw new IllegalStateException("Heterogeneous collection: " +
                                                        memberEntityName + ", " + s);
                    }

                    if (memberEntityPersister == null)
                    {
                        memberEntityPersister = sf.getEntityPersister(memberEntityName);
                    }

                    // the entity mode is a session characteristic, so using the previously
                    // determined entity mode (TODO: verify this is really true)
                    Long mid  = (Long)memberEntityPersister.getIdentifier(o, mode);
                    ids.add(mid);
                }

                Type memberType = collectionType.getElementType(sf);
                Class memberClass = memberType.getReturnedClass();
                if (Map.class.equals(memberClass))
                {
                    // this is what Hibernate returns when it cannot figure out the class,
                    // most likley due to the fact that audited application uses entity names and
                    // custom tuplizers
                    if (memberEntityPersister != null)
                    {
                        EntityTuplizer t =
                            memberEntityPersister.getEntityMetamodel().getTuplizer(mode);
                        memberClass = t.getMappedClass();
                    }
                    else
                    {
                        // this means the collection was empty and we couldn't determine the
                        // member's persister - this look to me like a hack, review TODO
                        memberClass = Object.class;
                    }
                }

                auditType = aTx.getAuditType(collectionClass, memberClass);
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
        return "PostUpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
