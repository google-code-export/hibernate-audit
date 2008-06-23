package com.googlecode.hibernate.audit.util;

import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.tool.hbm2ddl.SchemaExport;

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

    // Static --------------------------------------------------------------------------------------

    public static void main(String[] args) throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure("/hibernate.cfg.xml");
        SchemaExport se = new SchemaExport(config);

        if (args.length == 0)
        {
            System.out.println("Usage: DDL <create|drop>");
            return;
        }

        boolean create = "create".equals(args[0]);

        if (!create && !"drop".equals(args[0]))
        {
            System.out.println("Usage: DDL <create|drop>");
            return;
        }

        if (create)
        {
            se.create(true, false);
        }
        else
        {
            se.drop(true, false);
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
