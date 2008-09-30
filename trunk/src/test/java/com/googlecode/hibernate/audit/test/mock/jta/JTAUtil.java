package com.googlecode.hibernate.audit.test.mock.jta;

import javax.transaction.Status;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class JTAUtil
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static String statusToString(int status)
    {
        if (status == Status.STATUS_ACTIVE)
        {
            return "ACTIVE";
        }
        else if (status == Status.STATUS_COMMITTED)
        {
            return "COMMITTED";
        }
        else if (status == Status.STATUS_COMMITTING)
        {
            return "COMMITTING";
        }
        else if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            return "MARKED_ROLLBACK";
        }
        else if (status == Status.STATUS_NO_TRANSACTION)
        {
            return "NO_TRANSACTION";
        }
        else if (status == Status.STATUS_PREPARED)
        {
            return "PREPARED";
        }
        else if (status == Status.STATUS_PREPARING)
        {
            return "PREPARING";
        }
        else if (status == Status.STATUS_ROLLEDBACK)
        {
            return "ROLLEDBACK";
        }
        else if (status == Status.STATUS_ROLLING_BACK)
        {
            return "ROLLING_BACK";
        }
        else if (status == Status.STATUS_UNKNOWN)
        {
            return "UNKNOWN";
        }
        else
        {
            throw new IllegalArgumentException("status " + status + " not understood");
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
