package com.googlecode.hibernate.audit.util;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.apache.log4j.Logger;

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.sql.DriverManager;
import java.sql.Connection;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class DDL
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DDL.class);

    // Static --------------------------------------------------------------------------------------

    private static Command command;
    private static String fileName;

    private static final String usageString = "Usage: DDL <create|drop> [-f]";

    public static void main(String[] args) throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure("/hibernate-thread.cfg.xml");
        SchemaExport se = new SchemaExport(config);

        log.debug(se);

        processCommandLine(args);

        if (command == null)
        {
            System.out.println(usageString);
            return;
        }

        // redirect System.out for a little bit
        PrintStream systemOut = System.out;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        System.setOut(ps);

        try
        {
            if (Command.CREATE.equals(command))
            {
                se.create(true, false);
            }
            else
            {
                se.drop(true, false);
            }

            ps.flush();
        }
        finally
        {
            // restore System.out
            System.setOut(systemOut);
        }


        byte[] ddl = baos.toByteArray();
        ps.close();
        baos.close();

        BufferedReader br =
            new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ddl)));

        PrintStream outPs = null;
        FileOutputStream fos = null;

        if (fileName == null)
        {
            outPs = System.out;
        }
        else
        {
            fos = new FileOutputStream(fileName);
            outPs = new PrintStream(fos);
        }

        String line = null;
        while((line = br.readLine()) != null)
        {
            outPs.println(line + ";");
        }

        br.close();
        outPs.flush();

        if (fos != null)
        {
            outPs.close();
            fos.close();
            System.out.println("Output written in " + fileName);
        }
    }

    private static void processCommandLine(String[] args) throws Exception
    {
        if (args.length == 0)
        {
            throw new Exception(usageString);
        }

        for(String arg: args)
        {
            if ("create".equals(arg))
            {
                command = Command.CREATE;
            }
            else if ("drop".equals(arg))
            {
                command = Command.DROP;
            }
            else if ("-f".equals(arg))
            {
                fileName = "./ddl.sql";
            }
        }
    }

    public static Connection getRawConnection(String driverClassName, String url,
                                            String username, String password) throws Exception
    {
        Class.forName(driverClassName);
        return DriverManager.getConnection(url, username, password);
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private enum Command
    {
        CREATE,
        DROP
    }
}
