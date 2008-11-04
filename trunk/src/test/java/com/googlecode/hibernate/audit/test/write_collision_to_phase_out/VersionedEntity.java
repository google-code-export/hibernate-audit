package com.googlecode.hibernate.audit.test.write_collision_to_phase_out;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public class VersionedEntity<E>
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Long version;
    private E entity;

    // Constructors --------------------------------------------------------------------------------

    public VersionedEntity(E entity, Long version)
    {
        this.entity = entity;
        this.version = version;
    }

    // Public --------------------------------------------------------------------------------------

    public E getEntity()
    {
        return entity;
    }

    public Long getVersion()
    {
        return version;
    }

    @Override
    public String toString()
    {
        return entity + ", version " + version;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
