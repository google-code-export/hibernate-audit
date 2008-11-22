package com.googlecode.hibernate.audit;

/**
 * Exception thrown by listeners if somethings goes wrong, internally, while attempting to record
 * an audit event, and the failure justifies rolling back the transaction, even if the audit is
 * "muted".
 *
 * If thrown anywhere in the audit event recording code, this exception will bubble up to the 
 * application layer, and the current transaction will be rolled back, even if the audit is "muted"
 * ("muted" means that the audit layer it suppresses any other unforeseen exception and just logs
 * them).
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class RollingBackAuditException extends RuntimeException
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public RollingBackAuditException()
    {
        super();
    }

    public RollingBackAuditException(String message)
    {
        super(message);
    }

    public RollingBackAuditException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public RollingBackAuditException(Throwable cause)
    {
        super(cause);
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
