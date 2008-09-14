package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.delta.EntityDeltaImpl;
import com.googlecode.hibernate.audit.delta.ScalarDelta;
import com.googlecode.hibernate.audit.delta.Deltas;
import com.googlecode.hibernate.audit.delta.PrimitiveDelta;
import com.googlecode.hibernate.audit.delta.EntityReferenceDelta;

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
        EntityDeltaImpl ed = new EntityDeltaImpl(new Long(1), "MockEntity", null);

        ScalarDelta pd = Deltas.createPrimitiveDelta("a", "alice");

        assert ed.addMemberVariableDelta(pd);

        Set<ScalarDelta> pds = ed.getScalarDeltas();
        assert pds.size() == 1;
        assert pds.contains(pd);

        assert !ed.addMemberVariableDelta(pd);
        assert pds.size() == 1;
        assert pds.contains(pd);

        ScalarDelta pd2 = Deltas.createPrimitiveDelta("a", "bob");
        assert !ed.addMemberVariableDelta(pd2);
        assert pds.size() == 1;
        assert pds.contains(pd);

        ScalarDelta pd3 = Deltas.createPrimitiveDelta("x", "xoxo");

        assert ed.addMemberVariableDelta(pd3);
        assert pds.size() == 2;
        assert pds.contains(Deltas.createPrimitiveDelta("a", null));
        assert pds.contains(Deltas.createPrimitiveDelta("x", null));
    }

    @Test(enabled = true)
    public void testAddPrimitiveAndEntityReferenceDelta() throws Exception
    {
        EntityDeltaImpl ed = new EntityDeltaImpl(new Long(1), "MockEntity", null);

        PrimitiveDelta pd = Deltas.createPrimitiveDelta("a", "alice");
        EntityReferenceDelta erd = Deltas.
            createEntityReferenceDelta("b", new Long(2), "MockEntity2");

        assert ed.addMemberVariableDelta(pd);
        assert ed.addMemberVariableDelta(erd);

        assert ed.getScalarDeltas().size() == 2;

        assert null == ed.getScalarDelta("nosuchvariable");
        ScalarDelta sd1 = ed.getScalarDelta("a");
        ScalarDelta sd2 = ed.getScalarDelta("b");

        assert null == ed.getPrimitiveDelta("nosuchvariable");
        PrimitiveDelta d = ed.getPrimitiveDelta("a");
        assert d == sd1;
        assert "a".equals(d.getName());
        assert String.class.equals(d.getType());
        assert "alice".equals(d.getValue());

        assert null == ed.getEntityReferenceDelta("nosuchvariable");
        EntityReferenceDelta erdd = ed.getEntityReferenceDelta("b");
        assert erdd == sd2;
        assert "b".equals(erdd.getName());
        assert new Long(2).equals(erdd.getId());
        assert "MockEntity2".equals(erdd.getEntityName());
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
