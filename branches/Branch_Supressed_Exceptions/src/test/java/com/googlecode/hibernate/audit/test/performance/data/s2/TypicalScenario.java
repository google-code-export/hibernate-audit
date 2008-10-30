package com.googlecode.hibernate.audit.test.performance.data.s2;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class TypicalScenario implements Scenario
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Scenario implementation ---------------------------------------------------------------------

    public int getPPerDCount()
    {
        return 2;
    }

    public int getEPerPCount()
    {
        return 1;
    }

    public int getEXPerPCount()
    {
        return 7;
    }

    public int getLPerPCount()
    {
        return 3;
    }

    public int getAPerPCount()
    {
        return 3;
    }

    public int getCLCPerPCount()
    {
        return 1;
    }

    public int getPPPerPCount()
    {
        return 3;
    }

    public int getPAPerPCount()
    {
        return 2;
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
