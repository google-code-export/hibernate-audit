package com.googlecode.hibernate.audit.delta;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
class CollectionDeltaImpl extends MemberVariableDeltaSupport implements CollectionDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String memberEntityName;
    private Collection<Serializable> ids;

    // Constructors --------------------------------------------------------------------------------

    public CollectionDeltaImpl(String name, String memberEntityName, Collection<Serializable> ids)
    {
        setName(name);
        this.memberEntityName = memberEntityName;
        this.ids = ids;
    }

    // CollectionDelta implementation --------------------------------------------------------------

    public String getMemberEntityName()
    {
        return memberEntityName;
    }

    public Collection<Serializable> getIds()
    {
        return ids;
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return getName() + "[Collection][" + memberEntityName + "]=" + ids;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
