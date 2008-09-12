package com.googlecode.hibernate.audit.delta_deprecated;

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
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.util.Reflections;
import com.googlecode.hibernate.audit.util.Hibernate;
import com.googlecode.hibernate.audit.util.Entity;
import com.googlecode.hibernate.audit.delta_deprecated.ChangeDeprecated;
import com.googlecode.hibernate.audit.delta_deprecated.DeltaDeprecated;
import com.googlecode.hibernate.audit.delta.ChangeType;

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
@Deprecated
public class DeltaEngine
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DeltaEngine.class);

    // Static --------------------------------------------------------------------------------------

    public static void delta(Object preTransactionState, String entityName, Long tid,
                             SessionFactoryImplementor sf,
                             SessionFactoryImplementor internalSf) throws Exception
    {
        delta(preTransactionState, entityName, null, tid, sf, internalSf);
    }

    /**
     * Applies the transactional delta to the base (preTransactionState).
     *
     * @param preTransactionState - a detached entity instance initialized with the pre-transaction
     *        state. We will use as a base to to apply the forward transaction delta. If id not
     *        specified, it must contain a valid id.
     * @param entityName - the entityName corresponding to the base instance. If null, base's class
     *        will be used.
     * @param id - the entity id. If null, then preTransactionState must contain a valid id.
     * @param txId - the id of the transaction we want to apply to 'preTransactionState'.
     *
     * @throws MappingException - if the object passed as initial state is not a known entity.
     * @throws IllegalArgumentException - if such a transaction does not exist, doesn't have a valid
     *         id, etc.
     */
    public static void delta(Object preTransactionState,
                             String entityName, Serializable id, Long txId,
                             SessionFactoryImplementor sf,
                             SessionFactoryImplementor internalSf) throws Exception
    {
        Session is = null;
        Transaction iTx = null;

        try
        {
            // determine the current events within the current transaction that directly
            // "touched" the object whose initial state is given

            Class c = preTransactionState.getClass();
            String className = c.getName();
            
            if (entityName == null)
            {
                entityName = className;
            }

            EntityPersister persister = sf.getEntityPersister(entityName);

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

            is = internalSf.openSession();
            iTx = is.beginTransaction();

            AuditTransaction aTx = (AuditTransaction)is.get(AuditTransaction.class, txId);

            if (aTx == null)
            {
                throw new IllegalArgumentException("No audit transaction with id " +
                                                   txId + " exists");
            }

            // first query the type
            String qs = "from AuditEntityType as a where a.className = :className";
            Query q = is.createQuery(qs);
            q.setString("className", className);

            AuditType atype = (AuditType)q.uniqueResult();

            if (atype == null)
            {
                throw new IllegalArgumentException(
                    "no audit trace found for an object of type " + className);
            }

            // get all events of that transaction
            qs = "from AuditEvent as a where a.transaction = :transaction order by a.id";
            q = is.createQuery(qs);
            q.setParameter("transaction", aTx);

            List events = q.list();

            if (events.isEmpty())
            {
                throw new IllegalArgumentException(
                    "no audit events found for " + preTransactionState +
                    " in transaction " + txId);
            }

            // "apply" events

            Set<EntityExpectation> entityExpectations = new HashSet<EntityExpectation>();
            Set<CollectionExpectation> collectionExpectations = new HashSet<CollectionExpectation>();

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;
                ChangeType type = ae.getType();

                if (!ChangeType.INSERT.equals(type))
                {
                    // TODO ignore for the time being
                    continue;
                    //throw new RuntimeException("HANDLING " + ae.getType() + " NOT YET IMPLEMENTED");
                }

                Long targetId = ae.getTargetId();
                AuditType targetType = ae.getTargetType();

                if (!targetType.isEntityType())
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                // we're sure it's an entity, so add it to the loading row, while making sure that
                // if the expectation is already there, we use that one.
                EntityExpectation e =
                    new EntityExpectation(sf, targetType.getClassInstance(), targetId);

                boolean found = false;
                for(EntityExpectation alreadyRegistered: entityExpectations)
                {
                    if (alreadyRegistered.equals(e))
                    {
                        alreadyRegistered.initializeDetachedInstanceIfNecessary(sf);
                        e = alreadyRegistered;
                        found = true;
                        break;
                    }
                }

                if (!found)
                {
                    entityExpectations.add(e);
                }

                Object detachedEntity = e.getDetachedInstance();

                // make sure that the target of this even is actually the entity we're applying
                // delta on. For UPDATEs, it's possible that the event refers to a related
                // sub-entity

                // TODO this section was added in a haste and probably must be removed

//                if (ChangeType.UPDATE.equals(type) &&
//                    (!id.equals(targetId) || !atype.equals(targetType)))
//                {
//                    // looks like it's a sub-entity UPDATE, so find the target ...
//                    Object target = Reflections.
//                        find(preTransactionState, targetType.getClassInstance(), targetId);
//
//                    //String targetEntityName = "TROUBLE";
//                    String targetEntityName = null;
//
//                    // ... and apply delta on the target
//                    delta(target, targetEntityName, targetId, txId, sf, internalSf);
//                    return;
//                }

                // insert all pairs of this event into this entity
                q = is.createQuery("from AuditEventPair as p where p.event = :event order by p.id");
                q.setParameter("event", ae);

                List pairs = q.list();

                for(Object o2: pairs)
                {
                    AuditEventPair p = (AuditEventPair)o2;
                    String name = p.getField().getName();
                    AuditType at = p.getField().getType();

                    Object value = null;

                    if (at.isEntityType())
                    {
                        Serializable entityId = at.stringToValue(p.getStringValue());
                        Class entityClass = at.getClassInstance();

                        // the audit framework persisted persisted only the id of this entity,
                        // but we need the entire state, so we check if we find this entity on the
                        // list of those we need state for; if it's there, fine, use it, if not
                        // register it on the list, hopefully the state will come later in a
                        // different event
                        EntityExpectation ee = new EntityExpectation(entityClass, entityId);

                        boolean expectationExists = false;
                        for(EntityExpectation seen : entityExpectations)
                        {
                            if (seen.equals(ee))
                            {
                                expectationExists = true;
                                if (seen.isFulfilled())
                                {
                                    value = seen.getDetachedInstance();
                                    Reflections.mutate(detachedEntity, name, value);
                                    break;
                                }
                                else
                                {
                                    // line this up too
                                    seen.addTargetEntity(detachedEntity, name);
                                }
                            }
                        }

                        if (!expectationExists)
                        {
                            entityExpectations.add(ee);

                            // give the expectation info so it can update the entity that refers the
                            // target of the expectation, when the expectation is eventually
                            // fulfilled.
                            ee.addTargetEntity(detachedEntity, name);
                        }
                    }
                    else if (at.isCollectionType())
                    {
                        AuditEventCollectionPair cp = (AuditEventCollectionPair)p;
                        AuditCollectionType ct = (AuditCollectionType)at;
                        Class collectionClass = ct.getCollectionClassInstance();
                        Class memberClass = ct.getClassInstance();

                        List<Long> ids = cp.getIds();

                        CollectionExpectation ce =
                            new CollectionExpectation(e, name, collectionClass, memberClass);
                        
                        collectionExpectations.add(ce);

                        for(Long cmid: ids)
                        {
                            EntityExpectation cmee = new EntityExpectation(memberClass, cmid);
                            entityExpectations.add(cmee); // noop if the expectation is already there
                            ce.add(cmee);
                        }
                    }
                    else
                    {
                        // primitive
                        value = at.stringToValue(p.getStringValue());
                        Reflections.mutate(detachedEntity, name, value);
                    }
                }
            }

            // loop over entity expectations and make sure that all of them have been fulfilled
            for(EntityExpectation e: entityExpectations)
            {
                if (!e.isFulfilled())
                {
                    // the state of this entity did not change in this transaction, so the
                    // state is whatever the state was previously of this transaction
                    Object o = DeltaEngine.retrieve(e.getClassInstance(), e.getId(), txId, sf);
                    e.fulfill(o);
                }
            }

            // also loop over collections and make sure that the content from all of them are
            // transferred to the rightful owners
            for(CollectionExpectation ce: collectionExpectations)
            {
                ce.transferToOwner();
            }

            iTx.commit();

            Object transactionDelta = null;
            for(EntityExpectation e: entityExpectations)
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

            entityExpectations.clear();

            Reflections.applyDelta(preTransactionState, transactionDelta);
        }
        catch(Exception e)
        {
            if (iTx != null)
            {
                try
                {
                    iTx.rollback();
                }
                catch(Exception e2)
                {
                    log.error("failed to rollback Hibernate transaction", e2);
                }
            }

            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch(Exception e2)
                {
                    log.error("failed to close internal Hibernate session", e2);
                }
            }

            throw e;
        }
    }

    /**
     * @param txId - the id of the transaction that introduced the delta.
     * @param lgId - the id of the logical group. If null, all delta information is returned.
     *
     * @return the delta or null, if no delta information was found for this particular combination
     *         of transaction/logical group.
     */
    public static DeltaDeprecated getDelta(Long txId, Serializable lgId, SessionFactoryImplementor internalSf)
        throws Exception
    {
        Session internalSession = null;
        Transaction iTx = null;

        try
        {
            internalSession = internalSf.openSession();
            iTx = internalSession.beginTransaction();

            AuditTransaction aTx =
                (AuditTransaction)internalSession.get(AuditTransaction.class, txId);

            if (aTx == null)
            {
                return null;
            }


            if (lgId != null && !lgId.equals(aTx.getLogicalGroupId()))
            {
                return null;
            }

            // get all events of that transaction
            String qs = "from AuditEvent as a where a.transaction = :transaction order by a.id";
            Query q = internalSession.createQuery(qs);
            q.setParameter("transaction", aTx);

            List events = q.list();

            if (events.isEmpty())
            {
                return null;
            }

            DeltaDeprecated result = new DeltaDeprecated();

            // "collect" events

            for(Object o: events)
            {
                AuditEvent ae = (AuditEvent)o;
                ChangeType ct = ae.getType();
                AuditType entityType = ae.getTargetType();

                if (!entityType.isEntityType())
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                Entity entity = new Entity(ae.getTargetId(), entityType.getClassInstance());

                qs = "from AuditEventPair as p where p.event = :event order by p.id";
                q = internalSession.createQuery(qs);
                q.setParameter("event", ae);

                List pairs = q.list();

                for(Object o2: pairs)
                {
                    AuditEventPair p = (AuditEventPair)o2;
                    String propertyName = p.getField().getName();
                    AuditType propertyType = p.getField().getType();
                    Object propertyValue = null;

                    if (propertyType.isEntityType())
                    {

                    }
                    else if (propertyType.isCollectionType())
                    {
                        log.warn("ignoring " + propertyType + " for now");
//                        AuditEventCollectionPair cp = (AuditEventCollectionPair)p;
//                        AuditCollectionType ct = (AuditCollectionType)at;
//                        Class collectionClass = ct.getCollectionClassInstance();
//                        Class memberClass = ct.getClassInstance();
//
//                        List<Long> ids = cp.getIds();
//
//                        CollectionExpectation ce =
//                            new CollectionExpectation(e, name, collectionClass, memberClass);
//
//                        collectionExpectations.add(ce);
//
//                        for(Long cmid: ids)
//                        {
//                            EntityExpectation cmee = new EntityExpectation(memberClass, cmid);
//                            entityExpectations.add(cmee); // noop if the expectation is already there
//                            ce.add(cmee);
//                        }
                    }
                    else
                    {
                        // primitive
                        propertyValue = propertyType.stringToValue(p.getStringValue());
                    }

                    ChangeDeprecated c = new ChangeDeprecated(ct, entity, propertyName,
                                          propertyType.getClassInstance(),  propertyValue);

                    result.addChange(c);
                }
            }

            return result;
        }
        catch(Exception e)
        {
            if (iTx != null)
            {
                try
                {
                    iTx.rollback();
                }
                catch(Exception e2)
                {
                    log.error("failed to rollback Hibernate transaction", e2);
                }

                iTx = null;
            }

            throw e;
        }
        finally
        {
            if (iTx != null)
            {
                iTx.commit();
            }

            if (internalSession != null)
            {
                try
                {
                    internalSession.close();
                }
                catch(Exception e2)
                {
                    log.error("failed to close internal Hibernate session", e2);
                }
            }
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


            // entityName
            // TODO this is a hack, see https://jira.novaordis.org/browse/HBA-80
            //org.hibernate.MappingException
            Object o = null;
            try
            {
                o = s.get(c, id);
            }
            catch(MappingException e)
            {
                // TODO this is a horrible kludge
                String entityName = Hibernate.entityNameHeuristics(c);

                if (entityName == null)
                {
                    throw new IllegalStateException(
                        "cannot figure out entityName for " + c.getName());
                }
                
                o = s.get(entityName, id);
            }

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
