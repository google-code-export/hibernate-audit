package com.googlecode.hibernate.audit.model;

import org.hibernate.Transaction;
import org.hibernate.StatelessSession;
import org.hibernate.Query;
import org.hibernate.SessionFactory;
import org.hibernate.SQLQuery;
import org.hibernate.event.EventSource;
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
import javax.transaction.Synchronization;
import java.util.Date;
import java.util.List;
import java.util.Collection;
import java.security.Principal;

import com.googlecode.hibernate.audit.HibernateAudit;

/**
 * TODO mixing administrative logic with persistence concerns in such a way does not feel good to
        me, this class must likely will be refactored.
 
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
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TRANSACTION_ID_SEQUENCE")
public class AuditTransaction implements Synchronization
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditTransaction.class);

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_TRANSACTION_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "TRANSACTION_TMSTP")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "TRANSACTION_USER")
    private String user;

    /**
     * The originating Hibernate transaction. Can be a JDBCTransaction or a JTATransaction.
     */
    @Transient
    private Transaction transaction;

    /**
     * The stateless session used to persist this transaction and all related audit elements.
     */
    @Transient
    private StatelessSession session;

    // Constructors --------------------------------------------------------------------------------

    AuditTransaction()
    {
        timestamp = new Date();
    }

    /**
     * @param principal - could be null if it couldn't be determined by the upper layers.
     * @param auditedSession - the Hibernate session the audited event belongs to.
     */
    public AuditTransaction(EventSource auditedSession, Principal principal)
    {
        this();
        this.transaction = auditedSession.getTransaction();

        if (principal != null)
        {
            this.user = principal.getName();
        }

        // persist in the context of the audited session, if no dedicated session is available, or
        // in the context of the dedicated session, if available. TODO: for the time being we
        // operate under the assumption that no dedicated session is available

        SessionFactory sessionFactory = auditedSession.getFactory();

        session = sessionFactory.openStatelessSession();

        // if we're in a JTA environment and there's an active JTA transaction, we'll just enroll
        session.beginTransaction();

        log.debug(this + " registering itself as synchronization on " + this.transaction);
        this.transaction.registerSynchronization(this);
    }

    // Synchronization implementation --------------------------------------------------------------

    public void beforeCompletion()
    {
        // most likely, this won't be called for a JTA transaction, because AuditTransaction
        // synchronization is registered during the JTA transaction "beforeCompletion()" call.
        // see https://jira.novaordis.org/browse/HBA-37
        try
        {
            session.getTransaction().commit();
            log.debug(this + " committed");

            session.close();
            session = null;
        }
        finally
        {
            // no matter what happens, disassociate myself from the thread
            HibernateAudit.setCurrentAuditTransaction(null);
        }
    }

    public void afterCompletion(int i)
    {
        log.debug("after completion, commit status " + i);
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
        return transaction;
    }

    /**
     * Write this transaction information on persistent storage, in the context of the transaction
     * itself.
     */
    public void log()
    {
        log.debug(this + ".log()");
        session.insert(this);
    }

    /**
     * Write audit event information on persistent storage, in the context of this transaction.
     */
    public void logEvent(AuditEvent ae)
    {
        log.debug(this + " logging " + ae);
        ae.setTransaction(this);

        // because we're using a stateless session, we cannot rely on persistence by reachability
        // so if the AuditType instance is not persisted, explicitely persist it

        AuditType at = ae.getTargetType();

        // TODO remove this if using a non-stateless session, it will be persisted by reachability
        if (at != null && at.getId() == null)
        {
            // look it up in the database first
            AuditType persisted = getAuditType(at);
            ae.setTargetType(persisted);
        }

        session.insert(ae);
    }

    /**
     * Write a name/value pair on persistent storage, in the context of this transaction.
     */
    public void logPair(AuditEventPair pair)
    {
        log.debug(this + " logging " + pair);

        if (pair.getEvent() == null)
        {
            throw new IllegalArgumentException("orphan name/value pair " + pair);
        }

        // because we're using a stateless session, we cannot rely on persistence by reachability
        // so if the related AuditTypeField and AuditType instances are not persisted, we explicitly
        // persist them here

        AuditTypeField field = pair.getField();
        AuditType at = field.getType();

        // TODO remove this if using a non-stateless session, it will be persisted by reachability
        if (at.getId() == null)
        {
            // look it up in the database first
            AuditType persistedType = getAuditType(at);
            field.setType(persistedType);
        }

        // TODO remove this if using a non-stateless session, it will be persisted by reachability
        if (field.getId() == null)
        {
            // look it up in the database first
            Query q = session.
                createQuery("from AuditTypeField as f where f.name = :name and f.type = :type");

            q.setString("name", field.getName());
            q.setParameter("type", field.getType());

            AuditTypeField persistedField = (AuditTypeField)q.uniqueResult();

            if (persistedField != null)
            {
                pair.setField(persistedField);
            }
            else
            {
                session.insert(field);
            }
        }

        session.insert(pair);

        // the stateless session doesn't automatically persist the id collection for
        // AuditEventCollectionPair so I have to do it by hand. Consider using a regular session,
        // see https://jira.novaordis.org/browse/HBA-64

        if (pair instanceof AuditEventCollectionPair)
        {
            // TODO This is a hack and must be changed! See https://jira.novaordis.org/browse/HBA-65
            Long pairId = pair.getId();
            AuditEventCollectionPair cpair = (AuditEventCollectionPair)pair;
            List<Long> ids = cpair.getIds();
            for(Long id: ids)
            {
                // TODO insert multiple pairs with the same statement
                String qs =
                    "insert into AUDIT_EVENT_PAIR_COLLECTION " +
                    "(AUDIT_EVENT_PAIR_ID, COLLECTION_ENTITY_ID) " +
                    "VALUES (" + pairId + ", " + id + ")";
                SQLQuery sqlQuery = session.createSQLQuery(qs);
                int updated = sqlQuery.executeUpdate();

                if (updated != 1)
                {
                    throw new IllegalStateException(qs + " did not succeed");
                }
            }
        }
    }

    /**
     * TODO must refactor this, it doesn't belong here, and also the implementation is bad
     */
    public AuditType getAuditType(AuditType at)
    {
        if(at.getId() != null)
        {
            // already persisted
            return at;
        }

        if (at.isPrimitiveType())
        {
            return AuditType.getInstanceFromDatabase(at.getClassInstance(), true, session);
        }
        else if (at.isEntityType())
        {
            AuditEntityType et = (AuditEntityType)at;
            return AuditEntityType.getInstanceFromDatabase(
                et.getClassInstance(), et.getIdClassInstance(), true, session);
        }
        else if (at.isCollectionType())
        {
            AuditCollectionType ct = (AuditCollectionType)at;
            return AuditCollectionType.getInstanceFromDatabase(
                ct.getCollectionClassInstance(), ct.getClassInstance(), true, session);
        }

        throw new IllegalArgumentException("don't know how to handle " + at);
    }

    /**
     * TODO must refactor this, it doesn't belong here, and also the implementation is bad
     *
     * Returns the corresponding AuditType (AuditCollectionType, AuditEntityType, etc), making a
     * database insert if the underlying class (or classes) were not persised in the database yet.
     */
    public AuditType getAuditType(Class collectionOrEntityClass, Class memberOrIdClass)
    {
        if (Collection.class.isAssignableFrom(collectionOrEntityClass))
        {
            return AuditCollectionType.
                getInstanceFromDatabase(collectionOrEntityClass, memberOrIdClass, true, session);
        }

        // it's an entity
        return AuditEntityType.
            getInstanceFromDatabase(collectionOrEntityClass, memberOrIdClass, true, session);
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
        return "AuditTransaction[" + (id == null ? "TRANSIENT" : id) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
