package com.googlecode.hibernate.audit;

/**
 * RuntimeException wrapper for unforeseen failures in the audit code.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class AuditRuntimeException extends RuntimeException
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public AuditRuntimeException()
    {
        super();
    }

    public AuditRuntimeException(String message)
    {
        super(message);
    }

    public AuditRuntimeException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public AuditRuntimeException(Throwable cause)
    {
        super(cause);
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
