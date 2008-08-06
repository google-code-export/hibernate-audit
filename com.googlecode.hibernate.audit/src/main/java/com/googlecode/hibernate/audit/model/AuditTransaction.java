package com.googlecode.hibernate.audit.model;

import org.hibernate.Transaction;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
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
     * The session used to persist this transaction and all related audit elements.
     */
    @Transient
    private Session session;

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

        SessionFactory sf = HibernateAudit.getSessionFactory();
        session = sf.openSession();

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
     * Write a name/value pair on persistent storage, in the context of this transaction.
     */
    public void log(AuditEventPair pair)
    {
        if (pair.getEvent() == null)
        {
            throw new IllegalArgumentException("orphan name/value pair " + pair);
        }

        session.save(pair);
        log.debug(this + " logged " + pair);
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
     * TODO must refactor this, it doesn't belong here, and also the implementation is bad
     *
     * Returns the corresponding AuditType, making a database insert if the underlying class (or
     * classes) were not persised in the database yet.
     */
    public AuditType getAuditType(Class c)
    {
        return AuditType.getInstanceFromDatabase(c, true, session);
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
