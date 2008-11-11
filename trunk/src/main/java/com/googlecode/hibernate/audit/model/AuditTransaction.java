package com.googlecode.hibernate.audit.model;

import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.LockMode;
import org.hibernate.EntityMode;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.impl.SessionImpl;
import org.hibernate.engine.EntityKey;
import org.apache.log4j.Logger;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;
import javax.transaction.Synchronization;
import java.util.Date;
import java.util.List;
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
@Entity
@Table(name = "AUDIT_TRANSACTION")
@SequenceGenerator(name = "audit-transaction-seq-generator", sequenceName = "AUDIT_TRANSACTION_SEQ")
public class AuditTransaction implements Synchronization
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditTransaction.class);
    private static final boolean traceEnabled = log.isDebugEnabled();

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_TRANSACTION_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, 
                    generator = "audit-transaction-seq-generator")
    private Long id;

    @Column(name = "TRANSACTION_TMSTP", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "TRANSACTION_USER")
    private String user;

    // The id of the application-level logical group modified by this transaction. For more about
    // logical groups see https://jira.novaordis.org/browse/HBA-100.
    @Column(name = "LOGICAL_GROUP_ID", columnDefinition="NUMBER(30, 0)")
    private Long logicalGroupId;

    // the events are stored in the order they were initially logged in the database. TODO implement this
    @OneToMany(mappedBy = "transaction", fetch = FetchType.LAZY)
    private List<AuditEvent> events;

    /**
     * The originating Hibernate transaction. Can be a JDBCTransaction or a JTATransaction.
     */
    @Transient
    private Transaction hibernateTransaction;

    /**
     * The session used to persist this transaction and all related audit elements.
     */
    @Transient
    private SessionImpl internalSession;

    // Constructors --------------------------------------------------------------------------------

    AuditTransaction()
    {
        timestamp = new Date();
    }

    /**
     * @param principal - could be null if it couldn't be determined by the upper layers.
     * @param hibernateTransaction - the Hibernate transaction this audit transaction instance
     *        corresponds to.
     */
    public AuditTransaction(Transaction hibernateTransaction,
                            Principal principal,
                            SessionFactory internalSessionFactory)
    {
        this();
        this.hibernateTransaction = hibernateTransaction;

        if (principal != null)
        {
            this.user = principal.getName();
        }

        internalSession = (SessionImpl)internalSessionFactory.openSession();

        // if we're in a JTA environment and there's an active JTA transaction, we'll just enroll
        internalSession.beginTransaction();

        if (traceEnabled) { log.debug(this + " registering itself as synchronization on " + this.hibernateTransaction); }

        this.hibernateTransaction.registerSynchronization(this);
    }

    // Synchronization implementation --------------------------------------------------------------

    public void beforeCompletion()
    {
        // most likely, this won't be called for a JTA transaction, because AuditTransaction
        // synchronization is registered during the JTA transaction "beforeCompletion()" call.
        // see https://jira.novaordis.org/browse/HBA-37
        try
        {
            internalSession.getTransaction().commit();
            
            if (traceEnabled) { log.debug(this + " committed"); }

            internalSession.close();
            internalSession = null;
        }
        finally
        {
            // no matter what happens, disassociate myself from the thread
            Manager.setCurrentAuditTransaction(null);
        }
    }

    public void afterCompletion(int i)
    {
        if (traceEnabled) { log.debug("after completion, commit status " + i); }

        // no matter what happens, disassociate myself from the thread
        Manager.setCurrentAuditTransaction(null);
    }

    // Public --------------------------------------------------------------------------------------

    public Long getId()
    {
        return id;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(Date timestamp)
    {
        this.timestamp = timestamp;
    }

    public String getUser()
    {
        return user;
    }

    public void setUser(String user)
    {
        this.user = user;
    }

    public Transaction getTransaction()
    {
        return hibernateTransaction;
    }

    public Serializable getLogicalGroupId()
    {
        return logicalGroupId;
    }

    public void setLogicalGroupId(Serializable logicalGroupId)
    {
        // TODO The API supports generic Serializables, internally we only support longs. Review that.
        this.logicalGroupId = (Long)logicalGroupId;
    }

    /**
     * The events are returned in the order they were initially logged in the database.
     * TODO: ordering is not implemented yet.
     */
    public List<AuditEvent> getEvents()
    {
        return events;
    }

    /**
     * Write an event on persistent storage, in the context of this transaction. This method may
     * seem redundant, as log(AuditEventPair) will also write the parent event, via cascade. However
     * there are cases when events do not generate any pairs, so we need this method. See HBA-74.
     *
     * Hence, it doesn't make sense to recursively descend into the event to find
     */
    public void log(AuditEvent event)
    {
        AuditType type = event.getTargetType();
        AuditType lockedType = lockTypeToPersistenceContext(type);
        event.setTargetType(lockedType);

        internalSession.save(event);

        if (traceEnabled) { log.debug(this + " logged " + event); }
    }

    /**
     * Write a name/value pair on persistent storage, in the context of this transaction.
     */
    public void log(AuditEventPair pair)
    {
        if (pair.getEvent() == null)
        {
            throw new IllegalArgumentException("orphan name/value pair " + pair);
        }

        // the field is already in the write-once cache, so lock it and its type
        AuditTypeField field = pair.getField();
        internalSession.lock(field, LockMode.NONE);

        AuditType type = field.getType();
        AuditType lockedType = lockTypeToPersistenceContext(type);
        field.setType(lockedType);

        internalSession.save(pair);

        if (traceEnabled) { log.debug(this + " logged " + pair); }
    }

    /**
     * Falls back to database identity.
     */
    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (!(o instanceof AuditTransaction))
        {
            return false;
        }

        AuditTransaction that = (AuditTransaction)o;

        return id != null && id.equals(that.id);
    }

    /**
     * Falls back to database identity.
     */
    @Override
    public int hashCode()
    {
        if (id == null)
        {
            return 0;
        }

        return id.hashCode();
    }

    @Override
    public String toString()
    {
        return "AuditTransaction[" + (id == null ? "TRANSIENT" : id) + "]@" +
               Integer.toHexString(System.identityHashCode(this));
    }

    // Package protected ---------------------------------------------------------------------------

    void setEvents(List<AuditEvent> events)
    {
        this.events = events;
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    /**
     * @return the 'locked' instance, that must be used from now on. If the calling context
     *         continues to use 'at', it may run into failures such as
     *         https://jira.novaordis.org/browse/HBA-164 (non unique exceptions).
     */
    private AuditType lockTypeToPersistenceContext(AuditType at)
    {
        // check whether the type hasn't been locked yet, it happens in case of entities with
        // multiple fields of same type or other similar cases

        // TODO both these this can be optimized, save look-ups, as mode, persister don't change
        EntityPersister pers = internalSession.getEntityPersister(AuditType.class.getName(), at);
        EntityMode mode = internalSession.getEntityMode();
        EntityKey key = new EntityKey(at.getId(), pers, mode);

        Object persistenceCtxTypeInstance = internalSession.getPersistenceContext().getEntity(key);

        if (persistenceCtxTypeInstance == null)
        {
            // entity not in persistence context, safe to lock

            try
            {
                internalSession.lock(at, LockMode.NONE);
                return at;
            }
            catch(Exception e)
            {
                throw new IllegalStateException("could not lock type " + at, e);
            }
        }

        if (persistenceCtxTypeInstance != at)
        {
            // see https://jira.novaordis.org/browse/HBA-164
            log.warn("type " + at + " already managed by the internal persistence context as " +
                     persistenceCtxTypeInstance + " using managed instance from now on");
        }

        return (AuditType)persistenceCtxTypeInstance;
    }

    // Inner classes -------------------------------------------------------------------------------

}
