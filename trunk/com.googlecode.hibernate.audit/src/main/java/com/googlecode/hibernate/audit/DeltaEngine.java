package com.googlecode.hibernate.audit;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.MappingException;
import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.engine.SessionFactoryImplementor;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventType;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.util.Reflections;

import java.util.List;
import java.util.HashSet;
import java.util.Set;
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
public class DeltaEngine
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DeltaEngine.class);

    // Static --------------------------------------------------------------------------------------

    public static Object delta(SessionFactoryImplementor sf, Object preTransactionState, Long tid)
        throws Exception
    {
        return delta(sf, preTransactionState, null, tid);
    }

    /**
     * @param preTransactionState - a detached entity instance initialized with the pre-transaction
     *        state. We will use as a base to to apply the forward transaction delta. If id not
     *        specified, it must contain a valid id.
     *
     * @param id - the entity id. If null, then preTransactionState must contain a valid id.
     *
     * @param tid - the id of the transaction we want to apply to 'preTransactionState'.
     *
     * @throws MappingException - if the object passed as initial state is not a known entity.
     * @throws IllegalArgumentException - if such a transaction does not exist, doesn't have a valid
     *         id, etc.
     *
     * @return a detached instance reflecting the post-transaction state of the object
     */
    public static Object delta(SessionFactoryImplementor sf,
                               Object preTransactionState,
                               Serializable id, Long tid)
        throws Exception
    {
        Session s = null;
        Transaction t = null;

        try
        {
            // determine the current events within the current transaction that directly
            // "touched" the object whose initial state is given

            // for that, first determine the entity's id
            Class c = preTransactionState.getClass();
            String className = c.getName();

            EntityPersister persister = sf.getEntityPersister(className);

            if (id == null)
            {
                // we try to get it from the instance
                // TODO. For the time being we only suport pojos
                id = (Serializable)persister.getIdentifier(preTransactionState, EntityMode.POJO);
            }

            if (id == null)
            {
                throw new IllegalArgumentException("initial state must have a non-null id");
            }

            s = sf.openSession();
            t = s.beginTransaction();

            AuditTransaction at = (AuditTransaction)s.get(AuditTransaction.class, tid);

            if (at == null)
            {
                throw new IllegalArgumentException("No audit transaction with id " +
                                                   tid + " exists");
            }

            // first query the type
            String qs = "from AuditType as a where a.className = :className";
            Query q = s.createQuery(qs);
            q.setString("className", className);

            AuditType atype = (AuditType)q.uniqueResult();

            if (atype == null)
            {
                throw new IllegalArgumentException(
                    "no audit trace found for an object of type " + className);
            }

            // get all events of that transaction
            qs = "from AuditEvent as a where a.transaction = :transaction order by a.id";
            q = s.createQuery(qs);
            q.setParameter("transaction", at);

            List events = q.list();

            if (events.isEmpty())
            {
                throw new IllegalArgumentException(
                    "no audit events found for " + preTransactionState +
                    " in transaction " + tid);
            }

            // "apply" events

            Set<EntityExpectation> entityLoadingRow = new HashSet<EntityExpectation>();

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;

                if (!AuditEventType.INSERT.equals(ae.getType()))
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                Long tId = ae.getTargetId();
                AuditType tt = HibernateAudit.enhance(sf, ae.getTargetType());

                if (!tt.isEntityType())
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                // we're sure it's an entity, so add it to the loading row
                EntityExpectation e = new EntityExpectation(sf, tt.getClassInstance(), tId);
                Object detachedEntity = e.getDetachedInstance();
                entityLoadingRow.add(e);

                // insert all pairs of this event into this entity
                q = s.createQuery("from AuditEventPair as p where p.event = :event order by p.id");
                q.setParameter("event", ae);

                List pairs = q.list();

                for(Object o2: pairs)
                {
                    AuditEventPair p = (AuditEventPair)o2;
                    String name = p.getField().getName();
                    AuditType type = HibernateAudit.enhance(sf, p.getField().getType());

                    Object value = null;

                    if (type.isEntityType())
                    {
                        Serializable entityId = type.stringToValue(p.getStringValue());
                        Class entityClass = type.getClassInstance();

                        // the audit framework persisted persisted only the id of this entity,
                        // but we need the entire state, so we check if we find this entity on the
                        // list of those we need state for; if it's there, fine, use it, if not
                        // register it on the list, hopefully the state will come later in a
                        // different event
                        EntityExpectation ee = new EntityExpectation(entityClass, entityId);

                        boolean expectationExists = false;
                        for(EntityExpectation seen : entityLoadingRow)
                        {
                            if (seen.equals(ee))
                            {
                                expectationExists = true;
                                if (ee.isFulfilled())
                                {
                                    value = seen.getDetachedInstance();
                                    Reflections.mutate(detachedEntity, name, value);
                                    break;
                                }
                                else
                                {
                                    // line this up too
                                    ee.addTargetEntity(detachedEntity, name);
                                }
                            }
                        }

                        if (!expectationExists)
                        {
                            entityLoadingRow.add(ee);

                            // give the expectation info so it can update the entity that refers the
                            // target of the expectation, when the expectation is eventually
                            // fulfilled.
                            ee.addTargetEntity(detachedEntity, name);
                        }
                    }
                    else if (type.isCollectionType())
                    {
                        log.warn(">>>>>>>>>>>>>>>>>>");
                        log.warn(">>>>>>>>>>>>>>>>>>");
                        log.warn(">>>>>>>>>>>>>>>>>>");
                        log.warn(">>>>>>>>>>>>>>>>>> Collection Handling NOT YET IMPLEMENTED");
                        log.warn(">>>>>>>>>>>>>>>>>>");
                        log.warn(">>>>>>>>>>>>>>>>>>");
                        log.warn(">>>>>>>>>>>>>>>>>>");
                    }
                    else
                    {
                        // primitive
                        value = type.stringToValue(p.getStringValue());
                        Reflections.mutate(detachedEntity, name, value);
                    }
                }
            }

            // loop over expectations and make sure that all of them have been fulfilled
            for(EntityExpectation e: entityLoadingRow)
            {
                if (!e.isFulfilled())
                {
                    // the state of this entity did not change in this transaction, so the
                    // state is whatever the state was previously of this transaction
                    Object o = DeltaEngine.retrieve(e.getClassInstance(), e.getId(), tid, sf);
                    e.fulfill(o);
                }
            }

            t.commit();

            Object transactionDelta = null;
            for(EntityExpectation e: entityLoadingRow)
            {
                if (e.getId().equals(id) && e.getClassInstance().equals(c))
                {
                    transactionDelta = e.getDetachedInstance();
                }
            }

            if (transactionDelta == null)
            {
                throw new IllegalArgumentException(
                    "no audit trace for " + c.getName() + "[" + id + "]" );
            }

            entityLoadingRow.clear();

            return Reflections.applyDelta(preTransactionState, transactionDelta);
        }
        catch(Exception e)
        {
            if (t != null)
            {
                try
                {
                    t.rollback();
                }
                catch(Exception e2)
                {
                    log.error("failed to rollback Hibernate transaction", e2);
                }
            }

            if (s != null)
            {
                try
                {
                    s.close();
                }
                catch(Exception e2)
                {
                    log.error("failed to close Hibernate session", e2);
                }
            }

            throw e;
        }
    }

    /**
     * Returns a (c, id) detached instance, as stored in the database at the <b>beginning</b> of
     * the transaction tid.
     */
    public static Object retrieve(Class c, Serializable id, Long tid, SessionFactoryImplementor sf)
        throws Exception
    {

        Session s = sf.getCurrentSession();
        Transaction t = null;

        try
        {
            t = s.beginTransaction();

            // we currently cheat and return the current instance. In reality, this is a lot
            // more expensive operation
            // TODO https://jira.novaordis.org/browse/HBA-52
            log.warn("disregarding transaction id " + tid);
            Object o = s.get(c, id);

            t.commit();

            return o;
        }
        catch(Exception e)
        {
            if (t != null)
            {
                try
                {
                    t.rollback();
                }
                catch(Exception e2)
                {
                    log.error("failed to rollback Hibernate transaction", e2);
                }
            }

            if (s != null)
            {
                try
                {
                    s.close();
                }
                catch(Exception e2)
                {
                    log.error("failed to close Hibernate session", e2);
                }
            }

            throw e;
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
