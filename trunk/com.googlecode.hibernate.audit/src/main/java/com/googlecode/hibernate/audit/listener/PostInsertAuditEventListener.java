package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.EventSource;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.type.Type;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditCollectionType;

import java.io.Serializable;

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

        String user = null; // TODO properly determine the user
        AuditTransaction auditTransaction = logTransaction(session, user);

        Serializable id = event.getId();
        Object entity = event.getEntity();
        String entityClassName = entity.getClass().getName();

        log.debug(this + " handles " + entityClassName + "[" + id + "]");

        AuditType at = new AuditType();
        at.setClassName(entityClassName);

        // TODO currently we only support Long as ids, we may need to generalize this
        if (!(id instanceof Long))
        {
            throw new IllegalArgumentException(
                "audited entity " + entityClassName + "'s id is not a Long, " +
                "so it is currently not supported");
        }

        AuditEvent ae = new AuditEvent();

        ae.setType(AuditEventType.INSERT);
        ae.setTargetId((Long)id);
        ae.setTargetType(at);

        auditTransaction.logEvent(ae);

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

            Type hibernateType = persister.getPropertyType(name);
            Class javaType = hibernateType.getReturnedClass();
            AuditType auditType = null;

            if (hibernateType.isEntityType())
            {
                if (!hibernateType.isAssociationType())
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                SessionFactoryImpl sf = (SessionFactoryImpl)session.getSessionFactory();
                Class idClass = sf.getIdentifierType(javaType.getName()).getReturnedClass();

//                String entityName = session.getEntityName(value);
//                EntityPersister associatedEntityPersister = sf.getEntityPersister(entityName);
//                Class idClass = associatedEntityPersister.getIdentifierType().getReturnedClass();

                auditType = new AuditEntityType(idClass);

                if (value == null)
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                // TODO Refactor this into something more palatable
                String entityName = session.getEntityName(value);
                EntityPersister associatedEntityPersister = sf.getEntityPersister(entityName);

                // the entity mode is a session characteristic, so using the previously determined
                // entity mode (TODO: verify this is really true)
                value = associatedEntityPersister.getIdentifier(value, mode);
            }
            else if (hibernateType.isCollectionType())
            {
                // See https://jira.novaordis.org/browse/HBA-30

                // TODO review this:
                // this is the "one" side of a one-to-many relationship, and the collection
                // contains the associated elements. Currently, we only handle the case when the
                // relationship is implemented as a foreign key in the "many"-side of the
                // relationship table, so recording this even doesn't give us much. Will record
                // though, and we'll remove later if needed
                auditType = new AuditCollectionType();
            }
            else
            {
                auditType = new AuditType();
            }

            auditType.setClassName(javaType.getName());

            AuditTypeField f = new AuditTypeField();
            f.setType(auditType);
            f.setName(name);

            AuditEventPair pair = new AuditEventPair();
            pair.setField(f);
            pair.setValue(value);
            pair.setEvent(ae);

            if (hibernateType.isComponentType())
            {
                // createComponent(...);
                // https://jira.novaordis.org/browse/HBA-32

                //throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            auditTransaction.logPair(pair);
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
