package com.googlecode.hibernate.audit.model;

import org.hibernate.Transaction;
import org.hibernate.StatelessSession;
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
        // in the context of the dedicated session, if available

        // TODO for the time being we operate under assumption that no dedicated session is available

        // TODO for JTA, a stateless session is enrolled. Make sure.

        session = auditedSession.getFactory().openStatelessSession();
        session.beginTransaction();

        this.transaction.registerSynchronization(this);

    }

    // Synchronization implementation --------------------------------------------------------------

    public void beforeCompletion()
    {
        try
        {
            session.getTransaction().commit();
            log.debug("audit transaction committed");
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
        session.insert(this);
    }

    /**
     * Write audit event information on persistent storage, in the context of this transaction.
     */
    public void logEvent(AuditEvent ae)
    {
        ae.setTransaction(this);
        session.insert(ae);
    }

    /**
     * Write a name/value pair on persistent storage, in the context of this transaction.
     */
    public void logNameValuePair(AuditPair nvp)
    {
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

    @Override
    public int hashCode()
    {
        if (id == null)
        {
            return 0;
        }

        return id.hashCode();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
