package com.googlecode.hibernate.audit.delta;

import java.io.Serializable;
import java.util.Date;
import java.util.Set;
import java.util.HashSet;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public class TransactionDeltaImpl implements TransactionDelta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Serializable id;
    private Serializable lgi;
    private Date timestamp;
    private String user;

    private Set<EntityDelta> entityDeltas;

    // Constructors --------------------------------------------------------------------------------

    public TransactionDeltaImpl(Serializable id, Serializable lgi, Date timestamp, String user)
    {
        this.id = id;
        this.lgi = lgi;
        this.timestamp = timestamp;
        this.user = user;

        entityDeltas = new HashSet<EntityDelta>();
    }

    // TransactionDelta implementation -------------------------------------------------------------

    public Serializable getId()
    {
        return id;
    }

    public Serializable getLogicalGroupId()
    {
        return lgi;
    }

    public Date getTimestamp()
    {
        return timestamp;
    }

    public String getUser()
    {
        return user;
    }

    /**
     * @return the underlying storage (which is mutable) so handle with care.
     */
    public Set<EntityDelta> getEntityDeltas()
    {
        return entityDeltas;
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
