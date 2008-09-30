package com.googlecode.hibernate.audit.test.util.data;

import java.util.Set;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class G
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static G getInstance()
    {
        return new G();
    }

    // Attributes ----------------------------------------------------------------------------------

    private String string;
    private G g;

    private Set strings;
    private Set gs;

    private Set<String> typedStrings;
    private Set<G> typedGs;

    // Constructors --------------------------------------------------------------------------------

    /**
     * This constructor must stay private, to test instantiation while overriding constructor's
     * accessiblity.
     */
    private G()
    {
    }

    // Public --------------------------------------------------------------------------------------

    public void setString(String s)
    {
        this.string = s;
    }

    public String getString()
    {
        return string;
    }

    public void setG(G g)
    {
        this.g = g;
    }

    public G getG()
    {
        return g;
    }

    public Set getStrings()
    {
        return strings;
    }

    public void setStrings(Set strings)
    {
        this.strings = strings;
    }

    public Set getGs()
    {
        return gs;
    }

    public void setGs(Set gs)
    {
        this.gs = gs;
    }

    public Set<String> getTypedStrings()
    {
        return typedStrings;
    }

    public void setTypedStrings(Set<String> typedStrings)
    {
        this.typedStrings = typedStrings;
    }

    public Set<G> getTypedGs()
    {
        return typedGs;
    }

    public void setTypedGs(Set<G> typedGs)
    {
        this.typedGs = typedGs;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
