package com.googlecode.hibernate.audit;

import com.googlecode.hibernate.audit.util.Reflections;

import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
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
class CollectionExpectation
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private EntityExpectation ownerExpectation;
    private String memberName;
    private Class memberType;

    private List<EntityExpectation> members;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param ownerExpectation - the expectation of the owner.
     * @param memberName - the owner entity's memeber name that holds this collection.
     */
    CollectionExpectation(EntityExpectation ownerExpectation,
                          String memberName, Class memberType)
        throws Exception
    {
        this.ownerExpectation = ownerExpectation;
        this.memberName = memberName;
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

        Collection<Object> c = new ArrayList<Object>();
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