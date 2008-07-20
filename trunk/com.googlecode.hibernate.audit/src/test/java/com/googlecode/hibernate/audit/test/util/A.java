package com.googlecode.hibernate.audit.test.util;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class A 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String s;
    private Integer i;
    private B b;
    private Boolean bo;

    // Constructors --------------------------------------------------------------------------------

    public A()
    {
    }

    // Public --------------------------------------------------------------------------------------

    public void setS(String s)
    {
        this.s = s;
    }

    public String getS()
    {
        return s;
    }

    public void setI(Integer i)
    {
        this.i = i;
    }

    public Integer getI()
    {
        return i;
    }

    public void setB(B b)
    {
        this.b = b;
    }

    public B getB()
    {
        return b;
    }

    public void setBo(Boolean b)
    {
        this.bo = b;
    }

    public Boolean isBo()
    {
        return bo;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
