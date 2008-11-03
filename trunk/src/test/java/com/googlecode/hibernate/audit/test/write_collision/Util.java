package com.googlecode.hibernate.audit.test.write_collision;

import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.engine.SessionFactoryImplementor;

import java.util.Set;
import java.util.HashSet;
import java.util.Collection;

import com.googlecode.hibernate.audit.test.write_collision.data.Root;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Util
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static Set<Entity> getChanged(SessionFactoryImplementor sf, Versions v1, Versions v2)
    {
        Set<Entity> result = new HashSet<Entity>();

        for(Entity e: v2.entities())
        {
            // TODO: what happens with entities that were present in v1, but not present in v2

            EntityPersister ep = sf.getEntityPersister(e.getName());
            EntityMode m = EntityMode.POJO; // TODO dynamically get the mode

            VersionedEntity newVe = v2.getVersionedEntity(e);
            VersionedEntity oldVe = v1.getVersionedEntity(e);

            if (isEntityChanged(sf, ep, m, oldVe.getEntity(), newVe.getEntity()))
            {
                result.add(e);
            }
        }

        return result;
    }

    /**
     * TODO incomplete, non tested implementation
     */
    public static boolean isEntityChanged(SessionFactoryImplementor sf,
                                          EntityPersister ep, EntityMode m,
                                          Object oldEntity, Object newEntity)
    {
        // TODO need to review implementation

        Object[] oldProperties = ep.getPropertyValues(oldEntity, m);
        Object[] newProperties = ep.getPropertyValues(newEntity, m);

        int oldSize = oldProperties == null ? 0 : oldProperties.length;
        int newSize = newProperties == null ? 0 : newProperties.length;

        if (oldSize != newSize)
        {
            return true;
        }

        // oldSize and newSize are the same

        for(int i = 0; i < newSize; i++)
        {
            Object oldProperty = oldProperties[i];
            Object newProperty = newProperties[i];
            if (isPropertyChanged(sf, oldProperty, newProperty))
            {
                return true;
            }
        }

        return false;
    }

    /**
     * TODO incomplete, quick and dirty, non tested implementation
     */
    public static boolean isPropertyChanged(SessionFactoryImplementor sf,
                                            Object oldProperty,
                                            Object newProperty)
    {
        if (oldProperty == null)
        {
            if (newProperty == null)
            {
                return false;
            }

            return true;
        }

        if (newProperty == null)
        {
            if (oldProperty == null)
            {
                return false;
            }

            return true;
        }

        if (oldProperty instanceof Collection)
        {
            if (!(newProperty instanceof Collection))
            {
                throw new IllegalStateException("inconsistent collections: " +
                                                oldProperty + ", " + newProperty);
            }

            Collection oldCollection = (Collection)oldProperty;
            Collection newCollection = (Collection)newProperty;
            return isCollectionChanged(sf, oldCollection, newCollection);
        }

        EntityPersister oldEp = null;

        try
        {
            oldEp = sf.getEntityPersister(oldProperty.getClass().getName());
        }
        catch(Exception e)
        {
            // ignore

            // TODO BAD implementation
        }

        if (oldEp != null)
        {
            EntityPersister newEp = sf.getEntityPersister(newProperty.getClass().getName());

            if (!oldEp.equals(newEp))
            {
                throw new IllegalStateException("inconsistent entities: " +
                                                oldProperty + ", " + newProperty);
            }

            Object oldId = oldEp.getIdentifier(oldProperty, EntityMode.POJO);
            Object newId = newEp.getIdentifier(newProperty, EntityMode.POJO);

            return oldId == null ? newId == null : !oldId.equals(newId);
        }

        if (!oldProperty.equals(newProperty))
        {
            return true;
        }

        return false;
    }

    /**
     * TODO incomplete, quick and dirty, non tested implementation
     */
    public static boolean isCollectionChanged(SessionFactoryImplementor sf,
                                              Collection oldCollection,
                                              Collection newCollection)
    {
        return false;
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
