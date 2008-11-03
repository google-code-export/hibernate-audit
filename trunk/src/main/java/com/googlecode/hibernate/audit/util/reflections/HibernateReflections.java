package com.googlecode.hibernate.audit.util.reflections;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.EntityMode;
import org.hibernate.type.Type;
import org.hibernate.type.CollectionType;
import org.apache.log4j.Logger;

import java.util.Collection;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class HibernateReflections
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateReflections.class);

    // Static --------------------------------------------------------------------------------------

    /**
     * Applies changes that come with the 'modified' entity on 'base'.
     *
     * TODO incomplete implementation.
     *
     * @exception NullPointerException on null arguments.
     * @exception IllegalArgumentException on invalid arguments.
     * @exception org.hibernate.MappingException - no corresponding entity persister can be found.
     *
     */
    public static void applyChanges(SessionFactoryImplementor sf, EntityMode entityMode,
                                    Object base, Object modified)
    {
        if (!base.getClass().equals(modified.getClass()))
        {
            throw new IllegalArgumentException(
                base + " and " + modified + " must have the same type");
        }

        String baseEntityName = base.getClass().getName();
        String modifiedEntityName = modified.getClass().getName();

        EntityPersister ep = sf.getEntityPersister(baseEntityName);

        if (ep != sf.getEntityPersister(modifiedEntityName))
        {
            throw new IllegalArgumentException(
                base + " and " + modified + " must have the same persister");
        }

        Type[] types = ep.getPropertyTypes();
        Object[] baseProperties = ep.getPropertyValues(base, entityMode);
        Object[] modifiedProperties = ep.getPropertyValues(modified, entityMode);


        for(int i = 0; i < baseProperties.length; i ++)
        {
            Type type = types[i];

            Object baseProperty = baseProperties[i];
            Object modifiedProperty = modifiedProperties[i];

            if (type.isEntityType())
            {
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }
            else if (type.isCollectionType())
            {
                applyCollectionChanges(ep, base, i, (Collection)baseProperty,
                                       (Collection)modifiedProperty);
            }
            else if (type.isComponentType())
            {
                log.debug("component");
            }
            else
            {
                // primtive
                if (baseProperty == null && modifiedProperty != null ||
                    baseProperty != null && !baseProperty.equals(modifiedProperty))
                {
                    ep.setPropertyValue(base, i, modifiedProperty, entityMode);
                }
            }
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private static void applyCollectionChanges(EntityPersister ep, Object base, int i,
                                               Collection collectionBase, Collection modified)
    {
        if (collectionBase.isEmpty() && modified.isEmpty())
        {
            return;
        }

        throw new RuntimeException("NOT YET IMPLEMENTED");
        //ep.setPropertyValue(base, i, modifiedProperty, entityMode);
    }

    // Inner classes -------------------------------------------------------------------------------
}
