package com.googlecode.hibernate.audit.test.util.data;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class B 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String s;

    private A a;

    // Constructors --------------------------------------------------------------------------------

    public B()
    {
    }

    public B(String s)
    {
        this.s = s;
    }

    // Public --------------------------------------------------------------------------------------

    public String getS()
    {
        return s;
    }

    public void setS(String s)
    {
        this.s = s;
    }

    public A getA()
    {
        return a;
    }

    public void setA(A a)
    {
        this.a = a;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (s == null)
        {
            return false;
        }

        if (!(o instanceof B))
        {
            return false;
        }

        B that = (B)o;

        return this.s.equals(that.s);
    }

    @Override
    public int hashCode()
    {
        int result = 17;

        result = result * 37 + (s == null ? 0 : s.hashCode());

        return result;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}