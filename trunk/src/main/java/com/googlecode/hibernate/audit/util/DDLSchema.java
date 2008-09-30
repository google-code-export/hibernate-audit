package com.googlecode.hibernate.audit.util;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;
import java.util.ArrayList;

/**
 * Contains a collection of names and types of schema objects. Used to drop those objects
 * after successful creation and test run.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class DDLSchema
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DDLSchema.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private List<DDLStatement> statements;

    // Constructors --------------------------------------------------------------------------------

    public DDLSchema()
    {
        statements = new ArrayList<DDLStatement>();
    }

    public DDLSchema(InputStream is) throws Exception
    {
        this();
        load(is);
    }

    public DDLSchema(File f) throws Exception
    {
        this();
        load(f);
    }

    // Public --------------------------------------------------------------------------------------

    public void load(File ddlFile) throws Exception
    {
        log.debug("loading from " + ddlFile);

        InputStream is = null;

        try
        {
            is = new FileInputStream(ddlFile);
            load(is);
        }
        finally
        {
            if (is != null)
            {
                is.close();
            }
        }
    }

    public void load(InputStream is) throws Exception
    {
        BufferedReader br = null;
        InputStreamReader isr = null;

        try
        {
            isr = new InputStreamReader(is);
            br = new BufferedReader(isr);

            String line = null;
            DDLStatement current = null;

            while((line = br.readLine()) != null)
            {
                line = line.trim();
                boolean beginsStatement = beginsStatement(line);

                if (line.length() == 0 || line.indexOf("/*") != -1 || beginsStatement)
                {
                    // empty line, other 'separator' lines, such as comment, etc
                    // TODO - superficial implementation for comments, need more careful handling,
                    // but good for the time being

                    if (current != null)
                    {
                        // "file" the current statement
                        current.postProcess();
                        statements.add(current);
                        current = null;
                    }

                    if (!beginsStatement)
                    {
                        continue;
                    }
                }

                if (current == null)
                {
                    // start "collecting" a new statement
                    current = new DDLStatement();
                }

                current.append(line);
            }

            // handle the last one
            if (current != null)
            {
                current.postProcess();
                statements.add(current);
                current = null;
            }
        }
        finally
        {
            if (isr != null)
            {
                isr.close();
            }

            if (br != null)
            {
                br.close();
            }
        }
    }

    public void create(Connection c) throws Exception
    {
        boolean originalAutoCommit = c.getAutoCommit();
        c.setAutoCommit(false);

        Statement jdbcStat = null;

        try
        {
            jdbcStat = c.createStatement();

            for(DDLStatement ddlStat: statements)
            {
                String s = ddlStat.getString();
                log.debug("executing DDL statement: " + s);
                jdbcStat.execute(s);
            }
        }
        finally
        {
            c.setAutoCommit(originalAutoCommit);

            if (jdbcStat != null)
            {
                jdbcStat.close();
            }
        }
    }

    /**
     * TODO This only works with Oracle.
     */
    public void drop(Connection c) throws Exception
    {
        boolean originalAutoCommit = c.getAutoCommit();
        c.setAutoCommit(false);

        Statement jdbcStat = null;

        try
        {
            jdbcStat = c.createStatement();

            // drop indexes, sequences and tables, in the inverse creation order
            DDLType[] types = new DDLType[] { DDLType.INDEX, DDLType.SEQUENCE, DDLType.TABLE };

            for(DDLType type: types)
            {
                List<String> targets = getTargetNames(DDLAction.CREATE, type);

                for(int i = targets.size() - 1; i >= 0; i --)
                {
                    String tname = targets.get(i);
                    String s = "DROP " + type + " " + tname;
                    if (DDLType.TABLE.equals(type))
                    {
                        s += " CASCADE CONSTRAINTS";
                    }
                    log.debug("executing DDL statement: " + s);
                    jdbcStat.execute(s);
                }
            }
        }
        finally
        {
            c.setAutoCommit(originalAutoCommit);

            if (jdbcStat != null)
            {
                jdbcStat.close();
            }
        }
    }

    public List<DDLStatement> getStatements()
    {
        return statements;
    }

    public int getStatementCount()
    {
        return statements.size();
    }

    /**
     * @param action - one of "CREATE", "ALTER", "DROP", ....
     * @param targetType - one of "table", "sequence", etc, ...
     *
     * @return a list of database elements contained by this schema instance, and which match the
     *         query. The order from original file is preserved.
     */
    public List<String> getTargetNames(DDLAction action, DDLType targetType)
    {
        List<String> result = new ArrayList<String>();
        for(DDLStatement s: statements)
        {
            if (s.getAction().equals(action) && s.getTargetType().equals(targetType))
            {
                result.add(s.getTargetName());
            }
        }
        
        return result;
    }
    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    /**
     * @param line - we expect an already trimmed line.
     */
    private static boolean beginsStatement(String line)
    {
        String s  = line.toLowerCase();

        return s.startsWith("create table") ||
               s.startsWith("alter table") ||
               s.startsWith("create index") ||
               s.startsWith("create sequence");
    }

    // Inner classes -------------------------------------------------------------------------------
}
