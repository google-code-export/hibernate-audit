package com.googlecode.hibernate.audit.test.util;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.util.List;

import com.googlecode.hibernate.audit.util.DDLSchema;
import com.googlecode.hibernate.audit.util.DDLStatement;
import com.googlecode.hibernate.audit.util.DDLAction;
import com.googlecode.hibernate.audit.util.DDLType;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class DDLSchemaTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DDLSchemaTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testLoad() throws Exception
    {
        String filename = "ddlFile1.sql";
        InputStream is =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        log.debug(is);
        
        if (is == null)
        {
            throw new Error("couldn't find " + filename);
        }

        DDLSchema schema = new DDLSchema();

        schema.load(is);

        List<DDLStatement> statements = schema.getStatements();

        assert statements.size() == 5;

        DDLStatement stat = statements.get(0);
        String s = stat.getString();
        assert s.startsWith("create table A");
        assert s.indexOf(';') == -1;
        assert DDLAction.CREATE.equals(stat.getAction());
        assert DDLType.TABLE.equals(stat.getTargetType());
        assert "A".equals(stat.getTargetName());

        stat = statements.get(1);
        s = stat.getString();
        assert s.startsWith("create table B");
        assert s.indexOf(';') == -1;
        assert DDLAction.CREATE.equals(stat.getAction());
        assert DDLType.TABLE.equals(stat.getTargetType());
        assert "B".equals(stat.getTargetName());

        stat = statements.get(2);
        s = stat.getString();
        assert s.startsWith("create index B_IX");
        assert s.indexOf(';') == -1;
        assert DDLAction.CREATE.equals(stat.getAction());
        assert DDLType.INDEX.equals(stat.getTargetType());
        assert "B_IX".equals(stat.getTargetName());

        stat = statements.get(3);
        s = stat.getString();
        assert s.startsWith("alter table A");
        assert s.indexOf(';') == -1;
        assert DDLAction.ALTER.equals(stat.getAction());
        assert DDLType.TABLE.equals(stat.getTargetType());
        assert "A".equals(stat.getTargetName());

        stat = statements.get(4);
        s = stat.getString();
        assert s.startsWith("create sequence A_SEQ");
        assert s.indexOf(';') == -1;
        assert DDLAction.CREATE.equals(stat.getAction());
        assert DDLType.SEQUENCE.equals(stat.getTargetType());
        assert "A_SEQ".equals(stat.getTargetName());
    }

    @Test(enabled = true)
    public void testLoad2() throws Exception
    {
        String filename = "1.0.0.Alpha6.sql";

        InputStream is =
            Thread.currentThread().getContextClassLoader().getResourceAsStream(filename);

        log.debug(is);

        if (is == null)
        {
            throw new Error("couldn't find " + filename);
        }

        DDLSchema schema = new DDLSchema();

        schema.load(is);

        List<DDLStatement> statements = schema.getStatements();

        assert statements.size() == 23;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
