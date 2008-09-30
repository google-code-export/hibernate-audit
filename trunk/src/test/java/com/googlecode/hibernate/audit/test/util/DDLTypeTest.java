package com.googlecode.hibernate.audit.test.util;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;

import com.googlecode.hibernate.audit.util.DDLType;

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
public class DDLTypeTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DDLTypeTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testTable() throws Exception
    {
        DDLType t = DDLType.valueOf("TABLE");
        assert DDLType.TABLE.equals(t);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
