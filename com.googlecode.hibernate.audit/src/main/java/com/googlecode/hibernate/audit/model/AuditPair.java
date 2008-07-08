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
@Table(name = "AUDIT_PAIR")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_EVENT_ID_SEQUENCE")
public class AuditPair 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // TODO Do we really need to have ids for each of them? I don't see gain ...
    @Id
    @Column(name = "ID")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "NAME")
    private String name;

    @Column(name = "VALUE")
    private String stringValue;

    @Column(name = "VALUE_CLASS_NAME")
    private String valueClassName;

    @ManyToOne
    @JoinColumn(name = "EVENT_ID")
    private AuditEvent event;

    // Constructors --------------------------------------------------------------------------------

    public AuditPair()
    {
    }

    // Public --------------------------------------------------------------------------------------

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public Object getValue()
    {
        return stringToValue();
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public void setValue(Object value)
    {
        valueToString(value);
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

    public String getValueClassName()
    {
        return valueClassName;
    }

    @Override
    public String toString()
    {
        return name + "=" + stringValue;
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

    /**
     * protected to allow access for testing.
     */
    protected void setValueClassName(String valueClassName)
    {
        this.valueClassName = valueClassName;
    }

    // Private -------------------------------------------------------------------------------------

    private void valueToString(Object o)
    {
        if (o instanceof String)
        {
            stringValue = (String)o;
            valueClassName = String.class.getName();
        }
        else
        {
            throw new IllegalArgumentException("don't know how to convert " + o + " to string");
        }
    }

    private Object stringToValue()
    {
        if (stringValue == null)
        {
            return null;
        }

        if (String.class.getName().equals(valueClassName))
        {
            return stringValue;
        }
        else
        {
            throw new IllegalStateException("unknown type: " + valueClassName);
        }
    }

    // Inner classes -------------------------------------------------------------------------------
}
