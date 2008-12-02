package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

import com.googlecode.hibernate.audit.LogicalGroup;

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

    @Column(name = "LOGICAL_GROUP_EXTERNAL_ID", columnDefinition="NUMBER(30, 0)", nullable = false)
    private Long externalId;

    @Column(name = "LOGICAL_GROUP_TYPE", nullable = false)
    private String type;

    // Constructors --------------------------------------------------------------------------------

    public AuditLogicalGroup()
    {
    }

    // LogicalGroup implementation -----------------------------------------------------------------

    public Long getId()
    {
        return externalId;
    }

    public String getType()
    {
        return type;
    }

    // Public --------------------------------------------------------------------------------------

    public Long getInternalId()
    {
        return internalId;
    }

    public void setInternalId(Long internalId)
    {
        this.internalId = internalId;
    }

    public void setId(Long externalId)
    {
        this.externalId = externalId;
    }

    public void setType(String type)
    {
        this.type = type;
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

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}