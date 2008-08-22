package com.googlecode.hibernate.audit.util;

import org.hibernate.type.CollectionType;
import org.hibernate.type.BagType;
import org.hibernate.type.SetType;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.tuple.Tuplizer;

import java.util.List;
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
public class Hibernate
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static Class collectionTypeToClass(CollectionType ct)
    {
        if (ct instanceof BagType)
        {
            // this is hibernate's unordered collection that accepts duplicates, we use list
            return List.class;
        }
        else if (ct instanceof SetType)
        {
            return Set.class;
        }

        throw new RuntimeException("we don't know to translate " + ct);
    }

    /**
     * @return null if it cannot figure out the type.
     */
    public static Class getTypeFromTuplizer(EntityPersister p, EntityMode m)
    {
        Tuplizer t = p.getEntityMetamodel().getTuplizerOrNull(m);

        if (t == null)
        {
            return null;
        }

        return t.getMappedClass();
    }

    /**
     * TODO this is a hack, should go away when https://jira.novaordis.org/browse/HBA-80 is fixed
     * @return null if cannot figure out entityName based on class.
     */
    public static String entityNameHeuristics(Class c)
    {

        String name = c.getName();

        name = name.substring(name.lastIndexOf('.') + 1);

        if (name.endsWith("Impl"))
        {
            name = name.substring(0, name.lastIndexOf("Impl"));
        }

        return name;
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
