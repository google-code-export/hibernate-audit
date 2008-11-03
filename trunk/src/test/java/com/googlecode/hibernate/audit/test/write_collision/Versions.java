package com.googlecode.hibernate.audit.test.write_collision;

import java.io.Serializable;
import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

/**
 * TODO nothing is synchronized, Version instance is supposed to be accessed only from within a
 * single transaction (one thread at a time)
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Versions
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Map<Entity, VersionedEntity> versions;

    // Constructors --------------------------------------------------------------------------------

    public Versions()
    {
        versions = new HashMap<Entity, VersionedEntity>();
    }

    // Public --------------------------------------------------------------------------------------

    public void put(String entityName, Long entityId, Object entity, Long version)
    {
        versions.put(new Entity(entityName, entityId), new VersionedEntity(entity, version));
    }

    public VersionedEntity getVersionedEntity(String entityName, Serializable id)
    {
        return getVersionedEntity(new Entity(entityName, id));
    }

    public VersionedEntity getVersionedEntity(Entity e)
    {
        return versions.get(e);
    }

    public Object getEntity(String entityName, Serializable id)
    {
        VersionedEntity ve = getVersionedEntity(entityName, id);

        if (ve == null)
        {
            return null;
        }

        return ve.getEntity();
    }

    public Long getVersion(Entity e)
    {
        VersionedEntity ve = versions.get(e);

        if (ve == null)
        {
            return null;
        }

        return ve.getVersion();
    }

    public Set<Entity> entities()
    {
        return versions.keySet();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------


}
