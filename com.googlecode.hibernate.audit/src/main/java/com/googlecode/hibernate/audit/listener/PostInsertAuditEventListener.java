package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.type.Type;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditPair;
import com.googlecode.hibernate.audit.model.AuditEntity;

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

        String user = null; // TODO properly determine the user
        AuditTransaction auditTransaction = logTransaction(event.getSession(), user);

        Serializable id = event.getId();
        Object entity = event.getEntity();
        String entityClassName = entity.getClass().getName();

        AuditEntity aent = new AuditEntity();
        aent.setClassName(entityClassName);

        // TODO currently we only support Long as ids, we may need to generalize this
        if (!(id instanceof Long))
        {
            throw new IllegalArgumentException(
                "audited entity " + entityClassName + "'s id is not a Long, " +
                "so it is currently not supported");
        }

        AuditEvent ae = new AuditEvent();

        ae.setType(AuditEventType.INSERT);
        ae.setEntityId((Long)id);
        ae.setEntity(aent);

        auditTransaction.logEvent(ae);

        EntityPersister persister = event.getPersister();
        EntityMode mode = persister.guessEntityMode(entity);

        // log properties

        for (String name : persister.getPropertyNames())
        {
            Object value = persister.getPropertyValue(entity, name, mode);
            if (value != null)
            {
                Type type = persister.getPropertyType(name);
                if (type.isEntityType())
                {
                    // createEntityRef(...);
                    // https://jira.novaordis.org/browse/HBA-31
                    throw new RuntimeException("entity type handling not yet implemented");
                }
                else if (type.isCollectionType())
                {
                    // collection event listener will be triggered by and process this.
                    // See https://jira.novaordis.org/browse/HBA-30
                    
                }
                else if (type.isComponentType())
                {
                    // createComponent(...);
                    // https://jira.novaordis.org/browse/HBA-32
                    throw new RuntimeException("component type handling not yet implemented");
                }
                else
                {
                    AuditPair nvp = new AuditPair();
                    nvp.setEvent(ae);
                    nvp.setName(name);
                    nvp.setValue(value);
                    auditTransaction.logNameValuePair(nvp);
                }
            }
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
