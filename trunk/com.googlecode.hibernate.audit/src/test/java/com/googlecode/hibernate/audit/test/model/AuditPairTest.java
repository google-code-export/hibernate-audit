package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditPair;
import com.googlecode.hibernate.audit.model.AuditField;
import com.googlecode.hibernate.audit.model.AuditType;

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

    private static final Logger log = Logger.getLogger(AuditPairTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testNoField() throws Exception
    {
        AuditPair p = new AuditPair();

        try
        {
            p.setValue("abc");
            throw new Error("should've failed");
        }
        catch(NullPointerException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testSetValue_UnsupportedType() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(String.class.getName());

        AuditField field = new AuditField();
        field.setType(type);

        AuditPair p = new AuditPair();
        p.setField(field);

        try
        {
            p.setValue(new Object());
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException e)
        {
            log.debug(e.getMessage());
        }
    }
    
    @Test(enabled = true)
    public void testSetValue_String() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(String.class.getName());

        AuditField field = new AuditField();
        field.setType(type);

        AuditPair p = new AuditPair();
        p.setField(field);
        p.setValue("abc");

        assert "abc".equals(p.getValue());
        assert "abc".equals(p.getStringValue());
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
