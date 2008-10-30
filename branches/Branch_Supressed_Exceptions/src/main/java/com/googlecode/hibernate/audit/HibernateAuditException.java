package com.googlecode.hibernate.audit;

/**
 * Exception thrown by listeners if somethings goes wrong, internally.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class HibernateAuditException extends RuntimeException
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public HibernateAuditException()
    {
        super();
    }

    public HibernateAuditException(String message)
    {
        super(message);
    }

    public HibernateAuditException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public HibernateAuditException(Throwable cause)
    {
        super(cause);
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
