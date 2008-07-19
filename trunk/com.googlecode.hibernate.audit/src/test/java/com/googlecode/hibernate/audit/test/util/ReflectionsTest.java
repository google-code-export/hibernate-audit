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

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    public class A
    {
        private String s;
        private Integer i;
        private B b;

        public void setS(String s)
        {
            this.s = s;
        }

        public String getS()
        {
            return s;
        }

        public void setI(Integer i)
        {
            this.i = i;
        }

        public Integer getI()
        {
            return i;
        }

        public void setB(B b)
        {
            this.b = b;
        }

        public B getB()
        {
            return b;
        }
    }

    public class B
    {
    }

    public class SubB extends B
    {
    }
}
