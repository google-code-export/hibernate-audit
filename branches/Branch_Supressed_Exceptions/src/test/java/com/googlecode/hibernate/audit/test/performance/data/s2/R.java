package com.googlecode.hibernate.audit.test.performance.data.s2;

import com.googlecode.hibernate.audit.test.performance.util.Util;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.Transient;
import javax.persistence.SequenceGenerator;
import javax.persistence.GenerationType;
import java.util.Date;

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
@Table(name = "R")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.STRING)
@SequenceGenerator(name = "sequence", sequenceName = "R_SEQUENCE", allocationSize = 50)
public abstract class R
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    @Id
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
    private Long id;

    @Transient
    private int i = -1;

    private String code;

    private Date d0;
    private Date d1;
    private Boolean b0;
    private Integer i0;
    private String s0;

    // Constructors --------------------------------------------------------------------------------

    public R(int i)
    {
        this.i = i;
        String s = getClass().getName();
        s = s.substring(s.lastIndexOf('.') + 1);

        code = s + "_" + (i < 10 ? "0" : "") + i + "_" + Util.randomString(12);

        d0 = new Date();
        d1 = new Date(d0.getTime() + 5000);

        b0 = Util.getRandom().nextBoolean();
        i0 = Util.getRandom().nextInt();
        s0 = Util.randomString(50);
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

    public String getCode()
    {
        return code;
    }

    public void setCode(String code)
    {
        this.code = code;
    }

    public Date getD0()
    {
        return d0;
    }

    public void setD0(Date d0)
    {
        this.d0 = d0;
    }

    public Date getD1()
    {
        return d1;
    }

    public void setD1(Date d1)
    {
        this.d1 = d1;
    }

    public Boolean getB0()
    {
        return b0;
    }

    public void setB0(Boolean b0)
    {
        this.b0 = b0;
    }

    public Integer getI0()
    {
        return i0;
    }

    public void setI0(Integer i0)
    {
        this.i0 = i0;
    }

    public String getS0()
    {
        return s0;
    }

    public void setS0(String s0)
    {
        this.s0 = s0;
    }

    @Override
    public String toString()
    {
        return code;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    int getI()
    {
        return i;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
