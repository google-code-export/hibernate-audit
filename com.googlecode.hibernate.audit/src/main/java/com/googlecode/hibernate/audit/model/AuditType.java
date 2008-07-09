package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.SequenceGenerator;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Transient;
import java.lang.reflect.Method;
import java.util.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.text.ParseException;

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
@Table(name = "AUDIT_TYPE")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TYPE_ID_SEQUENCE")
public class AuditType 
{
    // Constants -----------------------------------------------------------------------------------

    public static final Format oracleDateFormat =
        new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_TYPE_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "CLASS_NAME", unique=true)
    private String className;

    @Column(name = "LABEL")
    private String label;

    @Transient
    private Class c;

    // Constructors --------------------------------------------------------------------------------

    public AuditType()
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

    public String getClassName()
    {
        return className;
    }

    /**
     *
     * @exception IllegalArgumentException if the class name cannot be converted to a type known
     *            to the VM.
     */
    public void setClassName(String className)
    {
        this.className = className;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(String label)
    {
        this.label = label;
    }

    public Class getClassInstance()
    {
        if (className == null)
        {
            return null;
        }

        if (c == null)
        {
            try
            {
                c = Class.forName(this.className);
            }
            catch(ClassNotFoundException e)
            {
                throw new IllegalArgumentException("cannot resolve type " + className, e);
            }
        }

        return c;
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public String valueToString(Object o)
    {
        getClassInstance();
        if (!c.isInstance(o))
        {
            throw new IllegalArgumentException(
                "the argument is not a " + c.getName() + " instance");
        }

        try
        {
            Method m = c.getMethod("toString");
            return (String)m.invoke(o);
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException(
                "failed to invoke " + c.getName() + "'s toString()", e);
        }
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public Object stringToValue(String s)
    {
        getClassInstance();
        if (String.class == c)
        {
            return s;
        }
        else if (Integer.class == c)
        {
            return Integer.parseInt(s);
        }
        else if (Date.class == c)
        {
            try
            {
                return (Date)oracleDateFormat.parseObject(s);
            }
            catch(ParseException e)
            {
                throw new IllegalArgumentException(
                    "conversion of '" + s + "' to a Date value failed", e);
            }
        }

        throw new RuntimeException("don't know to convert string to " + c.getName());
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

        if (!(o instanceof AuditType))
        {
            return false;
        }

        AuditType that = (AuditType)o;

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
        return className + "[" + (id == null ? "TRANSIENT" : id) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
