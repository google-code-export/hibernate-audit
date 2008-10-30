package com.googlecode.hibernate.audit.test.performance.util;

import java.util.List;
import java.util.ArrayList;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Series
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private List<Run> runs;

    // Constructors --------------------------------------------------------------------------------

    public Series(int runCount)
    {
        runs = new ArrayList<Run>(runCount);

        for(int i = 0; i < runCount; i++)
        {
            runs.add(new Run());
        }
    }

    // Public --------------------------------------------------------------------------------------

    public List<Run> getRuns()
    {
        return runs;
    }

    public void printStatistics(boolean index)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintWriter pw = new PrintWriter(baos);

        pw.println();
        pw.println();
        pw.println("Run count: " + runs.size());
        pw.println();

        int i = 1;
        for(Run r: runs)
        {
            if (index)
            {
                pw.print(' ');

                if (i < 10)
                {
                    pw.print(' ');
                }
                pw.print(i + ": ");
            }

            pw.print(r.getElapsed());
            pw.println();
            i++;
        }

        pw.println();
        pw.flush();
        pw.close();
        
        System.out.println(new String(baos.toByteArray()));
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
