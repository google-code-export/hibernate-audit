package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.delta.EntityDeltaImpl;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.Deltas;

import java.util.Set;

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
public class EntityDeltaTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testAddPrimitiveDelta() throws Exception
    {
        EntityDeltaImpl ed = new EntityDeltaImpl(new Long(1));

        PrimitiveDelta pd = Deltas.createPrimitiveDelta("a", "alice");

        assert ed.addPrimitiveDelta(pd);

        Set<PrimitiveDelta> pds = ed.getPrimitiveDeltas();
        assert pds.size() == 1;
        assert pds.contains(pd);

        assert !ed.addPrimitiveDelta(pd);
        assert pds.size() == 1;
        assert pds.contains(pd);

        PrimitiveDelta pd2 = Deltas.createPrimitiveDelta("a", "bob");
        assert !ed.addPrimitiveDelta(pd2);
        assert pds.size() == 1;
        assert pds.contains(pd);

        PrimitiveDelta pd3 = Deltas.createPrimitiveDelta("x", "xoxo");

        assert ed.addPrimitiveDelta(pd3);
        assert pds.size() == 2;
        assert pds.contains(Deltas.createPrimitiveDelta("a", null));
        assert pds.contains(Deltas.createPrimitiveDelta("x", null));
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
