package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.EntityMode;

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
    private TypeCache typeCache;

    @Transient
    private SessionFactoryImplementor sf;

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

    /**
     * Only constructor to use when creating fresh AuditLogicalGroups instances to be persisted.
     *
     * @param sf - the session factory containing the metadata for the hibernate entity defining
     *        this logical group.
     */
    public AuditLogicalGroup(TypeCache typeCache, SessionFactoryImplementor sf)
    {
        this.typeCache = typeCache;
        this.sf = sf;
    }

    // LogicalGroup implementation -----------------------------------------------------------------

    public Serializable getLogicalGroupId()
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

    public void setLogicalGroupId(Long externalId)
    {
        this.externalId = externalId;
    }

    /**
     * @throws org.hibernate.MappingException
     * @throws Exception (possibly thrown by type cache)
     */
    public void setDefiningEntityName(String entityName) throws Exception
    {
        if (sf == null)
        {
            throw new IllegalStateException("session factory reference not set");
        }

        if (typeCache == null)
        {
            throw new IllegalStateException("type cache reference not set");
        }

        // TODO LAT
        EntityPersister ep = sf.getEntityPersister(entityName);
        Class ec = ep.getMappedClass(EntityMode.POJO);
        Class idc = ep.getIdentifierType().getReturnedClass();
        auditType = typeCache.getAuditEntityType(idc, ec);
        this.definingEntityName = entityName;
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