package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.model.AuditPair;

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
public class AuditPairTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testToString() throws Exception
    {
        AuditPair p = new AuditPair();
        p.setValue("abc");

        assert "abc".equals(p.getValue());
        assert "abc".equals(p.getStringValue());
        assert String.class.getName().equals(p.getValueClassName());
    }

    @Test(enabled = true)
    public void testFromString() throws Exception
    {
        // same as AuditPair, with the exception that it offers access to protected methods
        AuditPairAccess pa = new AuditPairAccess();

        pa.setStringValue("abc");
        pa.setValueClassName(String.class.getName());

        assert "abc".equals(pa.getValue());
        assert "abc".equals(pa.getStringValue());
        assert String.class.getName().equals(pa.getValueClassName());
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
