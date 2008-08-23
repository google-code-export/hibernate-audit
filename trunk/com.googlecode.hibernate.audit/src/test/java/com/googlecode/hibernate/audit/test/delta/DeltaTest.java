package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.delta.DeltaTestHelper;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class DeltaTest extends DeltaTestHelper
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    @Override
    protected Object getEntityInstanceToTest()
    {
        A a = new A();
        a.setId(new Long(1));
        return a;
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
