package com.googlecode.hibernate.audit.listener;

import org.apache.log4j.Logger;
import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.EntityMode;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.event.EventSource;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostDeleteEvent;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.delta.ChangeType;

import java.security.Principal;
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
abstract class AbstractAuditEventListener implements AuditEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AbstractAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    protected Manager manager;
    protected TypeCache typeCache;

    // Constructors --------------------------------------------------------------------------------

    protected AbstractAuditEventListener(Manager manager)
    {
        this.manager = manager;
        this.typeCache = manager.getTypeCache();
    }

    // AuditEventListener implementation -----------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    /**
     * Extracts all sorts of useful information out of the event and wrap them together as an
     * EventContext instance, and also log the audit transaction and the audit event into the
     * audit log.
     *
     * @param e - this method only gets entity events (PostInsertEvent, PostUpdateEvent, etc).
     *        Anything else is a programming error and it will be signaled with an
     *        IllegalArgumentException. Interesting how PostInsertEvent, PostUpdateEvent don't have
     *        a common base class that would allow access to entity ...
     */
    protected EventContext createAndLogEventContext(AbstractEvent e) throws Exception
    {
        EventContext c = new EventContext();

        populateEventContextWithEventSpecificInfo(c, e);

        c.factory = c.session.getFactory();
        c.entityIdClass = c.entityId.getClass();
        c.entityClass = c.entity.getClass();
        c.entityClassName = c.entityClass.getName();

        c.auditTransaction = createAuditTransaction(c.session);

        // update LogicalGroupId

        Serializable newLGId = manager.getLogicalGroupId(c.session, c.entityId, c.entity);
        Serializable currentLGId  = c.auditTransaction.getLogicalGroupId();

        if (currentLGId == null && newLGId != null)
        {
            c.auditTransaction.setLogicalGroupId(newLGId);
        }
        else if (currentLGId != null && !currentLGId.equals(newLGId))
        {
            throw new IllegalStateException(
                "NOT YET IMPLEMENTED: inconsistent logical groups, current: " + currentLGId +
                ", new: " + newLGId);
        }

        c.auditEntityType = typeCache.getAuditEntityType(c.entityIdClass, c.entityClass);

        // TODO currently we only support Long as ids, we may need to generalize this
        if (!(c.entityId instanceof Long))
        {
            throw new IllegalArgumentException(
                "audited entity " + c.entityClassName + "'s id is not a Long, " +
                "so it is currently not supported");
        }

        c.auditEvent = new AuditEvent();
        c.auditEvent.setTransaction(c.auditTransaction);
        c.auditEvent.setType(c.changeType);
        c.auditEvent.setTargetId((Long)c.entityId);
        c.auditEvent.setTargetType(c.auditEntityType);

        // even if it may seem redundant, log the event here in case the entity state is empty and
        // no pairs will be logged (see HBA-74).
        c.auditTransaction.log(c.auditEvent);

        c.mode = c.persister.guessEntityMode(c.entity);
        return c;
    }

    protected void populateEventContextWithEventSpecificInfo(EventContext c, AbstractEvent e)
    {
        if (e instanceof PostInsertEvent)
        {
            PostInsertEvent pie = (PostInsertEvent)e;
            c.changeType = ChangeType.INSERT;
            c.session = pie.getSession();
            c.entityId = pie.getId();
            c.entity = pie.getEntity();
            c.persister = pie.getPersister();
        }
        else if (e instanceof PostUpdateEvent)
        {
            PostUpdateEvent pue = (PostUpdateEvent)e;
            c.changeType = ChangeType.UPDATE;
            c.session = pue.getSession();
            c.entityId = pue.getId();
            c.entity = pue.getEntity();
            c.persister = pue.getPersister();
        }
        else if (e instanceof PostDeleteEvent)
        {
            PostDeleteEvent pde = (PostDeleteEvent)e;
            c.changeType = ChangeType.DELETE;
            c.session = pde.getSession();
            c.entityId = pde.getId();
            c.entity = pde.getEntity();
            c.persister = pde.getPersister();
        }
        else
        {
            throw new IllegalArgumentException("unsupported event type " + e);
        }
    }

    /**
     * Creates in-memory instance of the audit transaction the current event occured in scope of
     * and perform all necessary associations (with the thread, register synchronizations, etc.).
     * No unnecessary creation occurs if the audit transaction instance already exists.
     */
    protected AuditTransaction createAuditTransaction(EventSource auditedSession)
    {
        Transaction ht = auditedSession.getTransaction();
        AuditTransaction at = Manager.getCurrentAuditTransaction();

        if (at != null)
        {
            // already logged
            if (ht != at.getTransaction())
            {
                throw new IllegalStateException("other transaction");
            }
            
            return at;
        }

        Manager m = HibernateAudit.getManager();
        Principal p = m.getPrincipal();
        SessionFactory isf = m.getSessionFactory();
        at = new AuditTransaction(ht, p, isf);
        Manager.setCurrentAuditTransaction(at);

        log.debug(this + " created");

        return at;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    /**
     * Visible only to AbstractAuditEventListener subclasses.
     */
    protected class EventContext
    {
        SessionFactoryImplementor factory;
        EventSource session;

        Serializable entityId;
        Class entityIdClass;
        String entityName;

        Object entity;
        Class entityClass;
        String entityClassName;

        EntityPersister persister;
        EntityMode mode;

        ChangeType changeType;

        AuditTransaction auditTransaction;
        AuditEntityType auditEntityType;
        AuditEvent auditEvent;
    }
}
