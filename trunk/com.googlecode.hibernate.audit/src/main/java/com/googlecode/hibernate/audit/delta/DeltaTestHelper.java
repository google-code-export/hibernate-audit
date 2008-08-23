package com.googlecode.hibernate.audit.delta;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;

import java.util.List;
import java.io.Serializable;

import com.googlecode.hibernate.audit.util.Reflections;

/**
 * A DeltaTest superclass to test package protected methods.
 *
 * Could be safely filtered out when packing the public artifact of this project.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$</tt>
 */
public abstract class DeltaTestHelper
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DeltaTestHelper.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testAddEntityExpectation() throws Exception
    {
        Delta d = new Delta();

        Object o = getEntityInstanceToTest();
        Serializable id = Reflections.getId(o);

        EntityExpectation e = new EntityExpectation(o.getClass(), id);
        d.addEntityExpectation(e);

        List<EntityExpectation> ees = d.getEntityExpectations();
        assert ees.size() == 1;

        assert ees.get(0) == e;
        assert new Long(1).equals(id);
    }

    @Test(enabled = true)
    public void testAddEntityExpectation_AlreadyPresent_NullDetachedInstance() throws Exception
    {
        Delta d = new Delta();

        Object o = getEntityInstanceToTest();
        Serializable id = Reflections.getId(o);

        EntityExpectation e = new EntityExpectation(o.getClass(), id);
        d.addEntityExpectation(e);

        List<EntityExpectation> ees = d.getEntityExpectations();
        assert ees.size() == 1;

        EntityExpectation e2 = new EntityExpectation(o.getClass(), id);
        d.addEntityExpectation(e2);

        ees = d.getEntityExpectations();
        assert ees.size() == 1;

        assert ees.get(0) != e2;
        assert ees.get(0).getDetachedInstance() == e2.getDetachedInstance();
    }

    @Test(enabled = true)
    public void testAddEntityExpectation_AlreadyPresent_DifferentDetachedInstances() throws Exception
    {
        Delta d = new Delta();

        Object o = getEntityInstanceToTest();
        Serializable id = Reflections.getId(o);

        EntityExpectation e = new EntityExpectation(o.getClass(), id);
        d.addEntityExpectation(e);

        List<EntityExpectation> ees = d.getEntityExpectations();
        assert ees.size() == 1;

        EntityExpectation e2 = new EntityExpectation(o.getClass(), id);
        e2.setDetachedInstance(getEntityInstanceToTest()); // different physical instance

        try
        {
            d.addEntityExpectation(e2);
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException ex)
        {
            log.debug(">>> " + ex.getMessage());
        }

        ees = d.getEntityExpectations();
        assert ees.size() == 1;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    protected abstract Object getEntityInstanceToTest();

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
