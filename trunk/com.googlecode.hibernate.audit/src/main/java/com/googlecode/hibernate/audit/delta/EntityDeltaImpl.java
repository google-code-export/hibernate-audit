package com.googlecode.hibernate.audit.delta;

import java.util.Set;
import java.util.HashSet;
import java.io.Serializable;

/**
 * Encapsulates the set of changes applied to entity state during a transaction.
 *
 * An EntityDelta instance contains complete information allowing to recreate the state of the
 * entity at the end of the transaction, given that we have access to the state of the entity as
 * persisted just before the transaction started.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public class EntityDeltaImpl implements EntityDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // the entity id
    private Serializable id;
    private String entityName;
    private ChangeType ct;

    // TODO can be optimized by maintaining a map, keyed on variable name
    private Set<ScalarDelta> scalarDeltas;
    private Set<CollectionDelta> collectionDeltas;

    // Constructors --------------------------------------------------------------------------------

    public EntityDeltaImpl(Serializable id, String entityName, ChangeType ct)
    {
        this.id = id;
        this.entityName = entityName;
        this.ct = ct;
        scalarDeltas = new HashSet<ScalarDelta>();
        collectionDeltas = new HashSet<CollectionDelta>();
    }

    // EntityDelta implementation ------------------------------------------------------------------

    public Serializable getId()
    {
        return id;
    }

    public String getEntityName()
    {
        return entityName;
    }

    public ChangeType getChangeType()
    {
        return ct;
    }

    public boolean isInsert()
    {
        return ChangeType.INSERT.equals(ct);
    }

    public boolean isUpdate()
    {
        return ChangeType.UPDATE.equals(ct);
    }

    public boolean isDelete()
    {
        return ChangeType.DELETE.equals(ct);
    }

    public Set<ScalarDelta> getScalarDeltas()
    {
        return scalarDeltas;
    }

    public Set<CollectionDelta> getCollectionDeltas()
    {
        return collectionDeltas;
    }

    public ScalarDelta getScalarDelta(String name)
    {
        for(ScalarDelta p: scalarDeltas)
        {
            if (p.getName().equals(name))
            {
                return p;
            }
        }

        return null;
    }

    public PrimitiveDelta getPrimitiveDelta(String name)
    {
        for(ScalarDelta p: scalarDeltas)
        {
            if (p.getName().equals(name) && p.isPrimitive())
            {
                return (PrimitiveDelta)p;
            }
        }

        return null;
    }

    public EntityReferenceDelta getEntityReferenceDelta(String name)
    {
        for(ScalarDelta p: scalarDeltas)
        {
            if (p.getName().equals(name) && p.isEntityReference())
            {
                return (EntityReferenceDelta)p;
            }
        }

        return null;
    }

    public CollectionDelta getCollectionDelta(String name)
    {
        for(CollectionDelta c: collectionDeltas)
        {
            if (c.getName().equals(name))
            {
                return c;
            }
        }

        return null;
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * Not exposed in EntityDelta as its usage makes sense only when creating the delta.
     *
     * @return true if delta is successfully added, false if there's already a delta corresponding
     *         to the same member variable.
     */
    public boolean addMemberVariableDelta(MemberVariableDelta d)
    {
        if (d instanceof ScalarDelta)
        {
            return scalarDeltas.add((ScalarDelta)d);
        }
        else if (d instanceof CollectionDelta)
        {
            return collectionDeltas.add((CollectionDelta)d);
        }
        else
        {
            throw new IllegalArgumentException("unknown delta type " + d);
        }
    }

    public void setChangeType(ChangeType ct)
    {
        this.ct = ct;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }

        if (id == null || entityName == null)
        {
            return false;
        }

        if (!(o instanceof EntityDelta))
        {
            return false;
        }

        EntityDelta that = (EntityDelta)o;
        return id.equals(that.getId()) && entityName.equals((that.getEntityName()));
    }

    @Override
    public int hashCode()
    {
        return (entityName == null ? 0 : entityName.hashCode()) * 37 +
               (id == null ? 0 : id.hashCode());
    }

    @Override
    public String toString()
    {
        return "EntityDelta[" + entityName + "][" + id + "][" + ct + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
