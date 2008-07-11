package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.ManyToOne;
import javax.persistence.JoinColumn;

/**
 * An atomic audit name/value pair.
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
@Table(name = "AUDIT_EVENT_PAIR")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_EVENT_PAIR_ID_SEQUENCE")
public class AuditEventPair
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // TODO Do we really need to have ids for each of them? I don't see gain ...
    @Id
    @Column(name = "AUDIT_EVENT_PAIR_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "AUDIT_EVENT_ID")
    private AuditEvent event;

    @ManyToOne
    @JoinColumn(name = "AUDIT_CLASS_FIELD_ID")
    private AuditTypeField field;

    @Column(name = "STRING_VALUE")
    private String stringValue;

    /**
     * Reserved for future use (lists)
     */
    @Column(name = "LIST_INDEX")
    private String index;    

    // Constructors --------------------------------------------------------------------------------

    public AuditEventPair()
    {
    }

    // Public --------------------------------------------------------------------------------------

    public AuditTypeField getField()
    {
        return field;
    }

    public void setField(AuditTypeField field)
    {
        this.field = field;
    }

    public Object getValue()
    {
        if (field == null)
        {
            return null;
        }

        return field.stringToValue(stringValue);
    }

    /**
     * @exception NullPointerException no field was previously set on this pair.
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public void setValue(Object o)
    {
        stringValue = field.valueToString(o);
    }

    public AuditEvent getEvent()
    {
        return event;
    }

    public void setEvent(AuditEvent event)
    {
        this.event = event;
    }

    public String getStringValue()
    {
        return stringValue;
    }

    @Override
    public String toString()
    {
        return field + "=" + stringValue;
    }

    // Package protected ---------------------------------------------------------------------------

    Long getId()
    {
        return id;
    }

    void setId(Long id)
    {
        this.id = id;
    }

    // Protected -----------------------------------------------------------------------------------

    /**
     * protected to allow access for testing.
     */
    protected void setStringValue(String stringValue)
    {
        this.stringValue = stringValue;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
