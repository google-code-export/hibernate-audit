package com.googlecode.hibernate.audit.test.model.base;

import org.apache.log4j.Logger;
import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public abstract class AuditTypeTestBase extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditTypeTestBase.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testEquals() throws Exception
    {
        AuditType at = getAuditTypeToTest();
        at.setId(new Long(777));

        log.debug(at);

        AuditType at2 = getAuditTypeToTest(new Long(777));

        assert at.equals(at2);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected abstract AuditType getAuditTypeToTest();
    protected abstract AuditType getAuditTypeToTest(Long id);

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
