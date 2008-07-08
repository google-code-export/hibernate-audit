package com.googlecode.hibernate.audit.model;

import org.hibernate.Transaction;
import org.hibernate.StatelessSession;
import org.hibernate.Query;
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
    @Column(name = "ID")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "TRANSACTION_TIMESTAMP")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "TRANSACTION_USER")
    private String user;

    @Column(name = "DESCRIPTION", length = 4000)
    private String description;

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
     * @param auditedSession - the Hibernate session the audited event belongs to.
     */
    public AuditTransaction(EventSource auditedSession, String user)
    {
        this();
        this.transaction = auditedSession.getTransaction();
        this.user = user;

        // persist in the context of the audited session, if no dedicated session is available, or
        // in the context of the dedicated session, if available. TODO: for the time being we
        // operate under the assumption that no dedicated session is available

        session = auditedSession.getFactory().openStatelessSession();

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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
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

        AuditType aent = ae.getTargetType();

        // TODO remove this if using a non-stateless session, it will be persisted by reachability
        if (aent != null && aent.getId() == null)
        {
            // look it up in the database first
            Query q = session.createQuery("from AuditType as a where a.className = :className");
            q.setString("className", aent.getClassName());
            AuditType persisted = (AuditType)q.uniqueResult();

            if (persisted != null)
            {
                ae.setTargetType(persisted);
            }
            else
            {
                session.insert(aent);
            }
        }

        session.insert(ae);
    }

    /**
     * Write a name/value pair on persistent storage, in the context of this transaction.
     */
    public void logNameValuePair(AuditPair nvp)
    {
        log.debug(this + " logging " + nvp);
        if (nvp.getEvent() == null)
        {
            throw new IllegalArgumentException("orphan name/value pair " + nvp);
        }

        session.insert(nvp);
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
