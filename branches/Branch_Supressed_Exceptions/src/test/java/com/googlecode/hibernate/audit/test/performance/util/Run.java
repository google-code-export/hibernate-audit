package com.googlecode.hibernate.audit.test.performance.util;

import java.util.Date;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Run
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Date start;
    private Date stop;

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    /**
     * @exception IllegalStateException if clock already started
     */
    public void startClock()
    {
        if (start != null)
        {
            throw new IllegalStateException("clock already started");
        }

        start = new Date();
    }

    /**
     * @exception IllegalStateException if clock was not started or it was already stopped
     */
    public void stopClock()
    {
        if (start == null)
        {
            throw new IllegalStateException("clock not started");
        }

        if (stop != null)
        {
            throw new IllegalStateException("clock already stopped");
        }

        stop = new Date();
    }

    /**
     * @exception IllegalStateException if clock was not started or stopped
     */
    public long getElapsed()
    {
        if (start == null)
        {
            throw new IllegalStateException("clock not started");
        }

        if (stop == null)
        {
            throw new IllegalStateException("clock not stopped");
        }

        return stop.getTime() - start.getTime();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
