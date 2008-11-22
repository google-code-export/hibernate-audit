package com.googlecode.hibernate.audit.collision;

import com.googlecode.hibernate.audit.RollingBackAuditException;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class WriteCollisionException extends RollingBackAuditException
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public WriteCollisionException()
    {
        super();
    }

    public WriteCollisionException(String message)
    {
        super(message);
    }

    public WriteCollisionException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public WriteCollisionException(Throwable cause)
    {
        super(cause);
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
