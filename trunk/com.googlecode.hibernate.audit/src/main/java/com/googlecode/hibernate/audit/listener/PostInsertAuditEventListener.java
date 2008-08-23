package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.type.Type;
import org.hibernate.type.CollectionType;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.util.Hibernate;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

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
        log.debug(this + ".onPostInsert(...)");

        EntityEventContext ec = createAndLogEntityEventContext(event);

        // TODO maybe there's no need to iterate over *all* properties, maybe the state contains
        //      only the new properties, look at how onPostUpdate() was implemented and possibly
        //      refactor this implementation as well
        
        for (String name : ec.persister.getPropertyNames())
        {
            Object value = ec.persister.getPropertyValue(ec.entity, name, ec.mode);

            if (value == null)
            {
                // TODO it is possible that the previous value was not null, and this insert
                // nullifies? Add a test for this
                continue;
            }

            SessionFactoryImpl sf = (SessionFactoryImpl)ec.session.getSessionFactory();
            Type hibernateType = ec.persister.getPropertyType(name);

            AuditType auditType = null;
            AuditEventPair pair = null;

            if (hibernateType.isEntityType())
            {
                if (!hibernateType.isAssociationType())
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                // TODO Refactor this into something more palatable
                String entityName = ec.session.getEntityName(value);
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
                    EntityTuplizer t =
                        assocdEntityPersister.getEntityMetamodel().getTuplizer(ec.mode);
                    entityClass = t.getMappedClass();
                }

                Class idClass = assocdEntityPersister.getIdentifierType().getReturnedClass();
                auditType = ec.auditTransaction.getAuditType(entityClass, idClass);
                value = assocdEntityPersister.getIdentifier(value, ec.mode);
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
                    String s = ec.session.getEntityName(o);

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
                    Long mid  = (Long)memberEntityPersister.getIdentifier(o, ec.mode);
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
                            memberEntityPersister.getEntityMetamodel().getTuplizer(ec.mode);
                        memberClass = t.getMappedClass();
                    }
                    else
                    {
                        // this means the collection was empty and we couldn't determine the
                        // member's persister - this look to me like a hack, review TODO
                        memberClass = Object.class;
                    }
                }

                auditType = ec.auditTransaction.getAuditType(collectionClass, memberClass);
                pair = new AuditEventCollectionPair();
                ((AuditEventCollectionPair)pair).setIds(ids);
                value = null;
            }
            else
            {
                auditType = ec.auditTransaction.getAuditType(hibernateType.getReturnedClass());
                pair = new AuditEventPair();
            }

            AuditTypeField f = ec.auditTransaction.getAuditTypeField(name, auditType);

            pair.setField(f);
            pair.setValue(value);
            pair.setEvent(ec.auditEvent);

            if (hibernateType.isComponentType())
            {
                // createComponent(...);
                // https://jira.novaordis.org/browse/HBA-32

                //throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            ec.auditTransaction.log(pair);
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
