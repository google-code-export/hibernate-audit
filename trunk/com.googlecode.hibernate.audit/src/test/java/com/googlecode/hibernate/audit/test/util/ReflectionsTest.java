package com.googlecode.hibernate.audit.test.util;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.util.Reflections;

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
public class ReflectionsTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ReflectionsTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testMutate() throws Exception
    {
        A a = new A();

        Reflections.mutate(a, "s", "blah");
        Reflections.mutate(a, "i", new Integer(77));

        log.debug(a);

        assert "blah".equals(a.getS());
        assert new Integer(77).equals(a.getI());
    }

    @Test(enabled = true)
    public void testMutateWithSubclass() throws Exception
    {
        A a = new A();
        SubB subB = new SubB();

        Reflections.mutate(a, "b", subB);

        assert subB == a.getB();
    }

    @Test(enabled = true)
    public void testMutate_NoSuchMethodException() throws Exception
    {
        A a = new A();
        SubB subB = new SubB();

        try
        {
            Reflections.mutate(a, "blah", subB);
            throw new Error("should've failed");
        }
        catch(NoSuchMethodException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testApplyDelta() throws Exception
    {
        A base = new A();

        A delta = new A();
        delta.setS("delta");
        delta.setI(7);
        delta.setB(new B("ben"));
        delta.setBo(true);

        A result = (A)Reflections.applyDelta(base, delta);

        assert result != base;
        assert result != delta;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
