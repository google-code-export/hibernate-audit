package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.test.util.Formats;

import java.util.Date;
import java.lang.reflect.Method;

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
public class AuditTypeTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditTypeTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testTypeConversion_UnsupportedType() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(String.class.getName());

        try
        {
            type.valueToString(new Object());
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testTypeConversion_String() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(String.class.getName());

        assert "abc".equals(type.valueToString("abc"));
        assert "abc".equals(type.stringToValue("abc"));
    }

    @Test(enabled = true)
    public void testTypeConversion_Integer() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Integer.class.getName());

        assert "77".equals(type.valueToString(new Integer(77)));
        assert new Integer(77).equals(type.stringToValue("77"));
    }

    @Test(enabled = true)
    public void testTypeConversion_Date() throws Exception
    {
        AuditType type = new AuditType();
        type.setClassName(Date.class.getName());

        // it only supports "oracle" date format so far, this will be a problem in the future
        Date d = Formats.testDateFormat.parse("07/07/2008");
        assert "Mon Jul 07 00:00:00 PDT 2008".equals(type.valueToString(d));
        assert d.equals(type.stringToValue("Mon Jul 07 00:00:00 PDT 2008"));
    }

    @Test(enabled = true)
    public void testValueToString_Entity_InvalidState() throws Exception
    {
        AuditEntityType type = new AuditEntityType(null);

        try
        {
            type.valueToString("doesn't matter");
            throw new Error("should've failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testValueToString_Entity_InvalidId() throws Exception
    {
        Method m = A.class.getMethod("getId");
        AuditEntityType type = new AuditEntityType(m.getReturnType());
        type.setClassName(A.class.getName());

        try
        {
            type.valueToString(new RandomType());
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testValueToString_Entity() throws Exception
    {
        Method m = A.class.getMethod("getId");
        AuditEntityType type = new AuditEntityType(m.getReturnType());
        type.setClassName(A.class.getName());

        assert "7777".equals(type.valueToString(new Long(7777)));
    }

    @Test(enabled = true)
    public void testStringToValue_CustomType() throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType.class.getName());

        CustomType ct = (CustomType)t.stringToValue("123");

        assert new CustomType(123).equals(ct);
    }

    @Test(enabled = true)
    public void testStringToValue_NonConvertibleCustomType() throws Exception
    {
        AuditType t = new AuditType();
        t.setClassName(CustomType.class.getName());

        try
        {
            t.stringToValue("this won't convert into an int");
            throw new Error("should've failed");
        }
        catch(RuntimeException e)
        {
            log.debug(e.getMessage());
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class RandomType
    {
    }
}
