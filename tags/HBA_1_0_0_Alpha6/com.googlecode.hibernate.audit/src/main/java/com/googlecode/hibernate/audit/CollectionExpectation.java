package com.googlecode.hibernate.audit;

import com.googlecode.hibernate.audit.util.Reflections;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
class CollectionExpectation
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private EntityExpectation ownerExpectation;
    private String memberName;
    private Class collectionType;
    private Class memberType;

    private List<EntityExpectation> members;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param ownerExpectation - owner's expectation.
     * @param memberName - the owner entity's memeber name that holds this collection.
     */
    CollectionExpectation(EntityExpectation ownerExpectation,
                          String memberName, Class collectionType, Class memberType)
        throws Exception
    {
        this.ownerExpectation = ownerExpectation;
        this.memberName = memberName;
        this.collectionType = collectionType;
        this.memberType = memberType;

        this.members = new ArrayList<EntityExpectation>();
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "Collection[" + ownerExpectation.getClassInstance().getName() + "][" +
               ownerExpectation.getId() + "]." + memberName;
    }

    // Package protected ---------------------------------------------------------------------------

    Serializable getOwnerId()
    {
        if (ownerExpectation == null)
        {
            return null;
        }

        return ownerExpectation.getId();
    }

    Class getOwnerType()
    {
        if (ownerExpectation == null)
        {
            return null;
        }

        return ownerExpectation.getClassInstance();
    }

    String getMemberName()
    {
        return memberName;
    }

    Class getMemberType()
    {
        return memberType;
    }

    void add(EntityExpectation e)
    {
        members.add(e);
    }

    boolean isSameTypeAsMembers(Class prospectiveMemberType)
    {
        // TODO probably there's more to this implementation than this
        return memberType.equals(prospectiveMemberType);
    }

    /**
     * Fulfills the expectation, transfering collection members to the rightful owner.
     *
     * @exception Exception if we find unfulfilled entity expectations.
     */
    void transferToOwner() throws Exception
    {
        if (!ownerExpectation.isFulfilled())
        {
            throw new IllegalStateException("owner " + ownerExpectation + " not fulfilled");
        }

        Collection<Object> c = null;

        // TODO HACK, NEED TO BE REVIEWED
        if (Set.class.isAssignableFrom(collectionType))
        {
            c = new HashSet<Object>();
        }
        else if (List.class.isAssignableFrom(collectionType))
        {
            c = new ArrayList<Object>();
        }
        else
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        for(EntityExpectation e: members)
        {
            if (!e.isFulfilled())
            {
                throw new IllegalStateException(e + " is not fulfilled");
            }

            c.add(e.getDetachedInstance());
        }

        Reflections.mutateCollection(ownerExpectation.getDetachedInstance(), memberName, c);
    }

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}