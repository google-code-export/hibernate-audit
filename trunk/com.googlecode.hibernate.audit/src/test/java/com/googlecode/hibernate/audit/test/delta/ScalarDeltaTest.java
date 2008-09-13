package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.delta.Deltas;
import com.googlecode.hibernate.audit.delta.ScalarDelta;

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
public class ScalarDeltaTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testPrimitiveDelta_String() throws Exception
    {
        ScalarDelta<String> pd = Deltas.createPrimitiveDelta("a", "alice");
        String s = pd.getValue();
        assert "alice".equals(s);
    }

    @Test(enabled = true)
    public void testPrimitiveDelta_Integer() throws Exception
    {
        ScalarDelta<Integer> pd = Deltas.createPrimitiveDelta("a", new Integer(3));
        Integer i = pd.getValue();
        assert new Integer(3).equals(i);
    }

    @Test(enabled = true)
    public void testPrimitiveDelta_InferredType() throws Exception
    {
        ScalarDelta pd = Deltas.createPrimitiveDelta("a", new Long(3));
        assert Long.class.equals(pd.getType());
    }

    @Test(enabled = true)
    public void testPrimitiveDelta_InferredTypeOnNull() throws Exception
    {
        String o = null;
        ScalarDelta pd = Deltas.createPrimitiveDelta("a", o);

        assert null == pd.getType();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
