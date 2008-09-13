package com.googlecode.hibernate.audit.test.delta;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.delta.TransactionDeltaImpl;
import com.googlecode.hibernate.audit.delta.EntityDeltaImpl;
import com.googlecode.hibernate.audit.delta.EntityDelta;

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
public class TransactionDeltaTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testAddEntityDelta() throws Exception
    {
        TransactionDeltaImpl td = new TransactionDeltaImpl(new Long(1), null, null, null);

        assert td.getEntityDeltas().isEmpty();

        EntityDeltaImpl ed = new EntityDeltaImpl(new Long(2), "dot.com.MockEntity");

        assert td.addEntityDelta(ed);

        Set<EntityDelta> eds = td.getEntityDeltas();

        assert eds.size() == 1;
        assert eds.contains(ed);

        assert !td.addEntityDelta(ed);
        assert eds.size() == 1;

        EntityDeltaImpl ed2 = new EntityDeltaImpl(new Long(2), "dot.com.MockEntity");

        assert !td.addEntityDelta(ed2);
        assert eds.size() == 1;

        EntityDeltaImpl ed3 = new EntityDeltaImpl(new Long(2), "dot.com.MockEntity2");

        assert td.addEntityDelta(ed3);
        assert eds.size() == 2;
        assert eds.contains(new EntityDeltaImpl(new Long(2), "dot.com.MockEntity"));
        assert eds.contains(new EntityDeltaImpl(new Long(2), "dot.com.MockEntity2"));

        EntityDeltaImpl ed4 = new EntityDeltaImpl(new Long(4), "dot.com.MockEntity");
        assert td.addEntityDelta(ed4);
        assert eds.size() == 3;
        assert eds.contains(new EntityDeltaImpl(new Long(2), "dot.com.MockEntity"));
        assert eds.contains(new EntityDeltaImpl(new Long(4), "dot.com.MockEntity"));
        assert eds.contains(new EntityDeltaImpl(new Long(2), "dot.com.MockEntity2"));

        EntityDeltaImpl ed5 = new EntityDeltaImpl(new Long(5), "dot.com.MockEntity3");
        assert td.addEntityDelta(ed5);
        assert eds.size() == 4;
        assert eds.contains(new EntityDeltaImpl(new Long(2), "dot.com.MockEntity"));
        assert eds.contains(new EntityDeltaImpl(new Long(4), "dot.com.MockEntity"));
        assert eds.contains(new EntityDeltaImpl(new Long(2), "dot.com.MockEntity2"));
        assert eds.contains(new EntityDeltaImpl(new Long(5), "dot.com.MockEntity3"));
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
