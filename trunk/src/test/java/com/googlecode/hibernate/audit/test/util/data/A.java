package com.googlecode.hibernate.audit.test.util.data;

import java.util.List;
import java.util.ArrayList;

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

    private List<R> rs;

    // Constructors --------------------------------------------------------------------------------

    public A()
    {
        rs = new ArrayList<R>();
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

    public List<R> getRs()
    {
        return rs;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
