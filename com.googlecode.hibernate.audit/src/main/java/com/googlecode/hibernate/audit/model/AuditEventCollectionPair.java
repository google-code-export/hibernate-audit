package com.googlecode.hibernate.audit.model;

import org.hibernate.annotations.CollectionOfElements;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.JoinTable;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
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

    @CollectionOfElements
    @JoinTable(name = "AUDIT_EVENT_PAIR_COLLECTION",
               joinColumns = @JoinColumn(name = "AUDIT_EVENT_PAIR_ID"))
    @Column(name = "COLLECTION_ENTITY_ID", nullable = false)
    private List<Long> ids;

    /**
     * Reserved for future use (lists)
     */
    @Column(name = "LIST_INDEX")
    private Integer index;

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
    public String toString()
    {
        return getField() + "=" + ids;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
