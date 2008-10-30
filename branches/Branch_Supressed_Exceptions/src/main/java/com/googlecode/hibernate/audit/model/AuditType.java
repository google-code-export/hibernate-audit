package com.googlecode.hibernate.audit.model;

import org.apache.log4j.Logger;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Id;
import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Transient;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.text.ParseException;
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
@Table(name = "AUDIT_CLASS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@GenericGenerator(name = "audit-type-seqhilo-generator",
                  strategy = "seqhilo",
                  parameters =
                  {
                      @Parameter(name = "sequence", value = "AUDIT_CLASS_SEQ"),
                      @Parameter(name = "max_lo", value = "1000")
                  })
@DiscriminatorColumn(name = "DTYPE", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue("P")
//@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AuditType
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditType.class);

    public static final Format oracleDateFormat =
        new SimpleDateFormat("EEE MMM dd hh:mm:ss zzz yyyy");

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @Column(name = "AUDIT_CLASS_ID", columnDefinition="NUMBER(30, 0)")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "audit-type-seqhilo-generator")
    private Long id;

    @Column(name = "CLASS_NAME", nullable = false)
    private String className;

    @Column(name = "LABEL")
    private String label;

    @Transient
    protected Class classInstance;

    // Constructors --------------------------------------------------------------------------------

    public AuditType()
    {
    }

    /**
     * Only for use by classes of this package, do not expose publicly.
     */
    AuditType(Class c)
    {
        this.classInstance = c;

        if (classInstance != null)
        {
            this.className = classInstance.getName();
        }
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
        if (classInstance != null)
        {
            return classInstance;
        }

        if (className == null)
        {
            return null;
        }
        try
        {
            classInstance = Class.forName(this.className);
        }
        catch(ClassNotFoundException e)
        {
            throw new IllegalArgumentException("cannot resolve class " + className, e);
        }

        return classInstance;
    }

    public boolean isPrimitiveType()
    {
        return true;
    }

    /**
     * Runtime information that helps with the conversion of a "value" to its string representation;
     * for entities, the string representation is the string representation of their ids. This
     * information is useful at runtime, but it is not persisted explicitly, because we already
     * have the fully qualified Java class name in the table, and that is enough to figure out
     * that this type is an entity.
     */
    public boolean isEntityType()
    {
        return false;
    }

    public boolean isCollectionType()
    {
        return false;
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public String valueToString(Object o)
    {
        getClassInstance();

        if (o == null)
        {
            return null;
        }

        if (!classInstance.isInstance(o))
        {
            throw new IllegalArgumentException(
                "the argument is not a " + classInstance.getName() + " instance");
        }

        try
        {
            Method m = classInstance.getMethod("toString");
            return (String)m.invoke(o);
        }
        catch(Exception e)
        {
            throw new IllegalArgumentException(
                "failed to invoke " + classInstance.getName() + "'s toString()", e);
        }
    }

    /**
     * @exception IllegalArgumentException if the conversion fails for some reason.
     */
    public Serializable stringToValue(String s)
    {
        if (s == null)
        {
            return null;
        }
        
        getClassInstance();

        // avoid reflection for often-used types

        if (String.class == classInstance)
        {
            return s;
        }
        else if (Integer.class == classInstance)
        {
            return Integer.parseInt(s);
        }
        else if (Long.class == classInstance)
        {
            return Long.parseLong(s);
        }
        else if (Boolean.class == classInstance)
        {
            return Boolean.parseBoolean(s);
        }
        else if (Date.class == classInstance)
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

        try
        {
            Method m = classInstance.getMethod("valueOf", String.class);
            return (Serializable)m.invoke(null, s);
        }
        catch(InvocationTargetException e)
        {
            // we found the method, conversion failed, bail out
            Throwable t = e.getCause();
            throw new RuntimeException(
                "don't know to convert string to " + classInstance.getName() + " with valueOf()", t);
        }
        catch(Exception e)
        {
            log.debug("failed to obtain value from string using valueOf() via reflection", e);
        }

        // give it one more chance, try parse...()

        String parseMethodName = classInstance.getName();
        parseMethodName = parseMethodName.substring(parseMethodName.lastIndexOf('.') + 1);
        parseMethodName = "parse" + parseMethodName;

        try
        {
            Method m = classInstance.getMethod(parseMethodName, String.class);
            return (Serializable)m.invoke(null, s);
        }
        catch(Exception e)
        {
            log.debug("failed to obtain value from string using parse...() via reflection", e);
        }

        throw new RuntimeException("don't know to convert string to " + classInstance.getName());
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
        return "PrimitiveType[" + (id == null ? "TRANSIENT" : id) + "][" + className + "]@" +
               Integer.toHexString(System.identityHashCode(this));
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
