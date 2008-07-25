package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;
import javax.persistence.OneToMany;
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

//    /**
//     * Reserved for future use (lists)
//     */
//    @Column(name = "LIST_INDEX")
//    private String index;
//

    //@OneToMany
    private List<Long> ids;

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

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
