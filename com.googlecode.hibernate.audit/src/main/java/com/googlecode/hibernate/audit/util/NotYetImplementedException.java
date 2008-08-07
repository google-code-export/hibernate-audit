package com.googlecode.hibernate.audit.util;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class NotYetImplementedException extends RuntimeException
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public NotYetImplementedException()
    {
        super();
    }

    public NotYetImplementedException(String message)
    {
        super(message);
    }

    public NotYetImplementedException(String message, Throwable cause)
    {
        super(message, cause);
    }

    public NotYetImplementedException(Throwable cause)
    {
        super(cause);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String getMessage()
    {
        return "NOT YET IMPLEMENTED " + (super.getMessage() == null ? "" : super.getMessage()); 
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
