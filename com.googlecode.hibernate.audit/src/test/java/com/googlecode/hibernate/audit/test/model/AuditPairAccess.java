package com.googlecode.hibernate.audit.test.model;

import com.googlecode.hibernate.audit.model.AuditPair;

/**
 * This class exists to allow access to protected methods of AuditPair.
 * Access is necessary for testing, while we would like to restrict access to those methods.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
class AuditPairAccess extends AuditPair
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    AuditPairAccess()
    {
        super();
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * Allow access for testing.
     */
    @Override
    public void setStringValue(String s)
    {
        super.setStringValue(s);
    }

    /**
     * Allow access for testing.
     */
    @Override
    public void setValueClassName(String s)
    {
        super.setValueClassName(s);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
