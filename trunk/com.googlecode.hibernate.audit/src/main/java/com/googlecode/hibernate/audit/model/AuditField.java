package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;

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
@Table(name = "AUDIT_FIELD")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_FIELD_ID_SEQUENCE")
public class AuditField
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

//    @Column(name = "CLASS_NAME", unique=true)
//    private String className;

    // Constructors --------------------------------------------------------------------------------

    public AuditField()
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

        if (!(o instanceof AuditField))
        {
            return false;
        }

        AuditField that = (AuditField)o;

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
        return " ";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
