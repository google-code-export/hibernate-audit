package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.FetchType;
import java.util.List;
import java.util.ArrayList;

/**
 * An atomic audit name/value pair that contains a collection.
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
@DiscriminatorValue("Y")
public class AuditEventCollectionPair extends AuditEventPair
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @CollectionOfElements(fetch = FetchType.EAGER) // TODO https://jira.novaordis.org/browse/HBA-68
    @JoinTable(name = "AUDIT_EVENT_PAIR_COLLECTION",
               joinColumns = @JoinColumn(name = "AUDIT_EVENT_PAIR_ID"))
    @Column(name = "COLLECTION_ENTITY_ID", nullable = false, columnDefinition="NUMBER(30, 0)")
    @ForeignKey(name = "FK_AUDIT_EVENT_PAIR_COLLECTION")
    private List<Long> ids;

    /**
     * Reserved for future use (lists)
     */
//    @Column(name = "LIST_INDEX")
//    private Integer index;

    // Constructors --------------------------------------------------------------------------------

    public AuditEventCollectionPair()
    {
        ids = new ArrayList<Long>();
    }

    // Public --------------------------------------------------------------------------------------

    public List<Long> getIds()
    {
        return ids;
    }

    public void setIds(List<Long> ids)
    {
        this.ids = ids;
    }

    @Override
    public boolean isCollection()
    {
        return true;
    }

    @Override
    public Object getValue()
    {
        return ids;
    }

    @Override
    public String toString()
    {
        return getField() + "=" + ids;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
