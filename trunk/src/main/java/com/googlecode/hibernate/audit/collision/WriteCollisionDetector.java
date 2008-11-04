package com.googlecode.hibernate.audit.collision;

import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.HibernateAudit;

import java.io.Serializable;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class WriteCollisionDetector
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteCollisionDetector.class);
    private static final boolean traceEnabled = log.isTraceEnabled();

    // Static --------------------------------------------------------------------------------------

    private static ThreadLocal<Long> referenceVersion = new ThreadLocal<Long>();

    /**
     * Use it to set the reference version corresponding to the logical group we're detecting write
     * collision for, at the begining of the detection interval.
     */
    public static void setReferenceVersion(Long v)
    {
        Long crt = referenceVersion.get();

        if (v != null && crt != null)
        {
            // most likely someone forgot to clean thread context, so warn
            log.warn("found not null reference version associated with thread context, " + 
                     "make sure you clean the tread context from previous reference version " +
                     "when you're done with it!");
        }

        referenceVersion.set(v);
    }

    public static Long getReferenceVersion()
    {
        return referenceVersion.get();
    }

    // Attributes ----------------------------------------------------------------------------------

    private volatile boolean enabled;

    // Constructors --------------------------------------------------------------------------------

    public WriteCollisionDetector()
    {
        // disabled by default
        this.enabled = false;
    }

    // Public --------------------------------------------------------------------------------------

    public boolean isWriteCollisionDetectionEnabled()
    {
        return enabled;
    }

    public void setWriteCollisionDetectionEnabled(boolean b)
    {
        this.enabled = b;
    }

    /**
     * Is a noop if collision detection is disabled.
     *
     * Simply returns with no side effects if collision detection is enabled, but no colission
     * is detected.
     *
     * Throws WriteCollisionException if collision is detected but *DOES NOT* rollback the current
     * transaction. The responsiblity of catching the exception and rolling back the transaction
     * falls to the layer that started the transaction.
     *
     * @exception WriteCollisionException if a write collision has been detected.
     * @exception Exception if something else went wrong.
     */
    public void detectCollision(String entityName, Serializable entityId,
                                String fieldName,  Object currentValue)  throws Exception
    {
        if (!enabled)
        {
            if (traceEnabled) { log.debug("write collision detection disabled"); }
            return;
        }

        Long referenceVersion = getReferenceVersion();

        if (referenceVersion == null)
        {
            throw new IllegalStateException(
                "write collision detection enabled, but no reference version provided");
        }

        HibernateAudit.getValue(entityName, entityId, fieldName, referenceVersion);

    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
