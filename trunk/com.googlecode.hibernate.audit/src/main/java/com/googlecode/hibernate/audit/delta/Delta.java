package com.googlecode.hibernate.audit.delta;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents a transactional delta.
 *
 * TODO experimental class
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$</tt>
 */
public class Delta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private List<EntityExpectation> entityExpectations;

    private Object target;
    private List<Change> changes;

    // Constructors --------------------------------------------------------------------------------

    public Delta()
    {
        entityExpectations = new ArrayList<EntityExpectation>();
        changes = new ArrayList<Change>();
    }

    // Public --------------------------------------------------------------------------------------

    public void addChange(Change c)
    {
        changes.add(c);
    }

    public List<Change> getChanges()
    {
        return changes;
    }

    public void setTarget(Object target)
    {
        this.target = target;
    }

    public Object getTarget()
    {
        return target;
    }

    // Package protected ---------------------------------------------------------------------------

    /**
     * Adds the given entity expectation to delta, possibly adjusting the internal state of the
     * expectation, if a previous equivalent expectation was already added.
     *
     * Note: Do not make this method public, it must be accessed only within the package.
     *
     * @exception IllegalArgumentException if the entity expectation passed as argument already
     *            has a detached instance, the same entity is already registered, and it has a
     *            different detached instance.
     */
    void addEntityExpectation(EntityExpectation e) throws Exception
    {
        // make sure that if the expectation is already here, we use that one

        for(EntityExpectation i: entityExpectations)
        {
            if (i.equals(e))
            {
                Object idi = i.getDetachedInstance();
                Object edi = e.getDetachedInstance();

                if (edi == null)
                {
                    e.setDetachedInstance(idi);
                }
                else if (edi != idi)
                {
                    throw new IllegalArgumentException(
                        "entity expectation already registered with a different detached instance");
                }
                
                return;
            }
        }

        e.initializeDetachedInstance();
        entityExpectations.add(e);
    }

    /**
     * Note: Do not make this method public, it must be accessed only within the package.
     *
     * @return returns the underlying collection, not a copy, so handle with care.
     */
    List<EntityExpectation> getEntityExpectations()
    {
        return entityExpectations;
    }

    /**
     * TODO probably I want to change the name
     */
    void compact()
    {
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
