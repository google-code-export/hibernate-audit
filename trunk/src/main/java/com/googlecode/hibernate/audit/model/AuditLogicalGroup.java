package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;
import javax.persistence.Transient;

import com.googlecode.hibernate.audit.LogicalGroup;

import java.io.Serializable;

/**
 * Never create an instance of this class directly, use a LogicalGroupCache instance to get such
 * an instance.
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
@Table(name = "AUDIT_LOGICAL_GROUP")
@GenericGenerator(name = "audit-logical-group-seqhilo-generator",
                  strategy = "seqhilo",
                  parameters =
                  {
                      @Parameter(name = "sequence", value = "AUDIT_LOGICAL_GROUP_SEQ"),
                      @Parameter(name = "max_lo", value = "100")
                  })
public class AuditLogicalGroup implements LogicalGroup
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_LOGICAL_GROUP_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
                    generator = "audit-logical-group-seqhilo-generator")
    private Long internalId;

    @Column(name = "EXTERNAL_ID", columnDefinition="NUMBER(30, 0)", nullable = false)
    private Long externalId;

    @ManyToOne(optional = false)
    @Cascade(CascadeType.SAVE_UPDATE)
    @JoinColumn(name = "AUDIT_TYPE_ID")
    @ForeignKey(name = "FK_AUDIT_TYPE_LOGICAL_GROUP")
    private AuditType auditType;

    @Transient
    private String definingEntityName;

    // Constructors --------------------------------------------------------------------------------

    /**
     * Used by Hibernate, by test subclasses and by classes of this package (who presumably know
     * what they're doing).
     */
    protected AuditLogicalGroup()
    {
    }

    // LogicalGroup implementation -----------------------------------------------------------------

    public Serializable getExternalId()
    {
        return externalId;
    }

    public String getDefiningEntityName()
    {
        // inferred from auditType. Because setAuditType() is never called (if it was, that would
        // have been the place to synchronize definingEntityName), we synchronize (and cache) here.

        if (definingEntityName == null && auditType != null)
        {
            // try to get it from audit type
            definingEntityName = ((AuditEntityType)auditType).getEntityName();
        }
        
        return definingEntityName;
    }

    // Public --------------------------------------------------------------------------------------

    public Long getId()
    {
        return internalId;
    }

    public void setId(Long internalId)
    {
        this.internalId = internalId;
    }

    public void setExternalId(Long externalId)
    {
        this.externalId = externalId;
    }

    public AuditType getAuditType()
    {
        return auditType;
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

        if (!(o instanceof AuditLogicalGroup))
        {
            return false;
        }

        AuditLogicalGroup that = (AuditLogicalGroup)o;

        return internalId != null && internalId.equals(that.internalId);
    }

    /**
     * Falls back to database identity.
     */
    @Override
    public int hashCode()
    {
        if (internalId == null)
        {
            return 0;
        }

        return internalId.hashCode();
    }

    @Override
    public String toString()
    {
        return "LogicalGroup[" + (internalId == null ? "TRANSIENT" : internalId) + "]@" +
               Integer.toHexString(System.identityHashCode(this));
    }

    // Package protected ---------------------------------------------------------------------------

    /**
     * Used by classes of this package to restore cache instance integrity (see
     * https://jira.novaordis.org/browse/HBA-149).
     */
    void setAuditType(AuditType at)
    {
        this.auditType = at;
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}