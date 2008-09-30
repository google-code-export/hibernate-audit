package com.googlecode.hibernate.audit.test.util.data;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class D
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String s;

    private List<E> es;
    private List<FParty> fParties;

    // Constructors --------------------------------------------------------------------------------

    public D()
    {
        es = new ArrayList<E>();
        fParties = new ArrayList<FParty>();
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

    public List<E> getEs()
    {
        return es;
    }

    // NO setEs() method

    public void addE(E e)
    {
        getEs().add(e);
    }

    public List<FParty> getFParties()
    {
        return fParties;
    }

    // NO setEs() method

    public void addFParty(FParty f)
    {
        getFParties().add(f);
    }


    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}