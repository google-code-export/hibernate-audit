package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.EnumType;
import javax.persistence.GenerationType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.FetchType;

import com.googlecode.hibernate.audit.delta.ChangeType;

import java.util.List;


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
@GenericGenerator(name = "audit-event-seqhilo-generator",
                  strategy = "seqhilo",
                  parameters =
                  {
                      @Parameter(name = "sequence", value = "AUDIT_EVENT_SEQ"),
                      @Parameter(name = "max_lo", value = "10000")
                  })
public class AuditEvent
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_EVENT_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit-event-seqhilo-generator")
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

    // the pairs are stored in the order they were initially logged in the database. TODO implement this
    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<AuditEventPair> pairs;

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

    /**
     * The pairs are returned in the order they were initially logged in the database.
     * TODO: ordering is not implemented yet.
     */
    public List<AuditEventPair> getPairs()
    {
        return pairs;
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

        if (!(o instanceof AuditEvent))
        {
            return false;
        }

        AuditEvent that = (AuditEvent)o;

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
        return "AuditEvent[" + (id == null ? "TRANSIENT" : id) + ", " +
               targetType + ", " + targetId + "]";

    }

    // Package protected ---------------------------------------------------------------------------

    void setPairs(List<AuditEventPair> pairs)
    {
        this.pairs = pairs;
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
