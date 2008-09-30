package com.googlecode.hibernate.audit.test.util.data;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 * 
 * $Id$
 */
public class ApplicationLevelImmutableImpl implements ApplicationLevelImmutable
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public void setSomething(Object o)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    // we declare this to "taint" the class and make it "mutable" in the common acception

}
