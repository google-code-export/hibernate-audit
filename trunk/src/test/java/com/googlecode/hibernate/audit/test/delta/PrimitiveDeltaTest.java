package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.delta.Deltas;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;

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
public class PrimitiveDeltaTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testPrimitiveDelta_String() throws Exception
    {
        PrimitiveDelta<String> pd = Deltas.createPrimitiveDelta("a", "alice");
        String s = pd.getValue();
        assert "alice".equals(s);
        assert pd.isPrimitive();
        assert !pd.isEntityReference();
    }

    @Test(enabled = true)
    public void testPrimitiveDelta_Integer() throws Exception
    {
        PrimitiveDelta<Integer> pd = Deltas.createPrimitiveDelta("a", new Integer(3));
        Integer i = pd.getValue();
        assert new Integer(3).equals(i);
        assert pd.isPrimitive();
        assert !pd.isEntityReference();
    }

    @Test(enabled = true)
    public void testPrimitiveDelta_InferredType() throws Exception
    {
        PrimitiveDelta pd = Deltas.createPrimitiveDelta("a", new Long(3));
        assert Long.class.equals(pd.getType());
        assert pd.isPrimitive();
        assert !pd.isEntityReference();
        assert new Long(3).equals(pd.getValue());
    }

    @Test(enabled = true)
    public void testPrimitiveDelta_InferredTypeOnNull() throws Exception
    {
        String o = null;
        PrimitiveDelta pd = Deltas.createPrimitiveDelta("a", o);

        assert null == pd.getType();

        assert pd.isPrimitive();
        assert !pd.isEntityReference();
        assert null == pd.getValue();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
