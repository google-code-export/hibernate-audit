package com.googlecode.hibernate.audit.model2;

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
@Table(name = "AUDIT_TRANSACTION_2")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TRANSACTION_ID_SEQ")
public class AuditTransaction2 implements Synchronization
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditTransaction2.class);

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "TIMESTAMPX")
    @Temporal(value = TemporalType.TIMESTAMP)
    private Date timestamp;

    @Column(name = "USERX")
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

    AuditTransaction2()
    {
        timestamp = new Date();
    }

    /**
     * @param auditedSession - the Hibernate session the audited event belongs to.
     */
    public AuditTransaction2(EventSource auditedSession, String user)
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
        session.getTransaction().commit();
        log.debug("audit transaction committed");
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
     * Write this transaction information on persistent storage.
     */
    public void log()
    {
        session.insert(this);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
