package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.EnumType;
import javax.persistence.GenerationType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

import com.googlecode.hibernate.audit.delta.ChangeType;


/**
 * An atomic audit event as captured by Hibernate listeners.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Entity
@Table(name = "AUDIT_EVENT")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_EVENT_SEQ")
public class AuditEvent
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_EVENT_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne(optional = false)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "AUDIT_TRANSACTION_ID")
    private AuditTransaction transaction;

    @Column(name = "EVENT_TYPE", nullable = false)
    @Enumerated(EnumType.STRING)
    private ChangeType type;

    @Column(name = "TARGET_ENTITY_ID", nullable = false, columnDefinition="NUMBER(30, 0)")
    private Long targetId; // TODO current implementation supports only Longs as ids, this needs
                           // to be generalized if audited model uses other types as ids.

    @ManyToOne(optional = false)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "AUDIT_CLASS_ID")
    private AuditType targetType;

    // Constructors --------------------------------------------------------------------------------

    public AuditEvent()
    {
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

    public ChangeType getType()
    {
        return type;
    }

    public void setType(ChangeType type)
    {
        this.type = type;
    }

    public AuditTransaction getTransaction()
    {
        return transaction;
    }

    public void setTransaction(AuditTransaction transaction)
    {
        this.transaction = transaction;
    }

    public Long getTargetId()
    {
        return targetId;
    }

    public void setTargetId(Long targetId)
    {
        this.targetId = targetId;
    }

    public AuditType getTargetType()
    {
        return targetType;
    }

    public void setTargetType(AuditType type)
    {
        this.targetType = type;
    }

    @Override
    public String toString()
    {
        return "AuditEvent[" + (id == null ? "TRANSIENT" : id) + ", " +
               targetType + ", " + targetId + "]";

    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
