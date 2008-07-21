package com.googlecode.hibernate.audit.test.util;

import java.util.List;

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

    private List<B> bs;

    private C c;

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

    public List<B> getBs()
    {
        return bs;
    }

    public void setBs(List<B> bs)
    {
        this.bs = bs;
    }

    public C getC()
    {
        return c;
    }

    public void setC(C c)
    {
        this.c = c;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
