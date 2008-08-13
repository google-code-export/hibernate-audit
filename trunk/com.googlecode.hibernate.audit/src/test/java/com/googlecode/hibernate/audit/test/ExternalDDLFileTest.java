package com.googlecode.hibernate.audit.test;

import com.googlecode.hibernate.audit.util.DDLSchema;
import com.googlecode.hibernate.audit.util.DDL;
import com.googlecode.hibernate.audit.util.DDLAction;
import com.googlecode.hibernate.audit.util.DDLType;
import com.googlecode.hibernate.audit.test.base.LocalTransactionTest;
import org.testng.annotations.Test;
import org.apache.log4j.Logger;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.List;

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
public class ExternalDDLFileTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ExternalDDLFileTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testExternalDDLFile() throws Exception
    {
        // we simulate the testing environment, by creating an instance of JTATransactionTest
        // by ourselves and making sure it creates and then drops the schema correctly. JTA is not
        // essential here, LocalTransactionTest would have done as well, but wasn't fully
        // implemented at the time of the writing.

        // make sure tables and sequences specified in 'ddlFile2.sql' do not exist in database.

        String f = "ddlFile2.sql";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(f);

        if (is == null)
        {
            throw new Error("couldn't find " + f);
        }

        DDLSchema schema = new DDLSchema(is);
        assert schema.getStatementCount() == 2;

        LocalTransactionTest tt = new LocalTransactionTest();
        tt.setUseExternallyCreatedSchema(true);
        tt.setSchemaDDLFileName(f);

        Connection c = null;
        Statement s = null;

        try
        {
            c = DDL.getRawConnection(tt.getConnectionDriverClassName(), tt.getConnectionUrl(),
                                     tt.getConnectionUsername(), tt.getConnectionPassword());

            // TODO this test is specific to Oracle, needs to be generalized
            s = c.createStatement();

            List<String> createdTables = schema.getTargetNames(DDLAction.CREATE, DDLType.TABLE);
            assert createdTables.size() == 1;
            String tableName = createdTables.get(0);

            String qs = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME = '" + tableName + "'";
            log.debug(qs);
            ResultSet rs = s.executeQuery(qs);
            assert !rs.next();

            List<String> createdSeqs = schema.getTargetNames(DDLAction.CREATE, DDLType.SEQUENCE);
            assert createdSeqs.size() == 1;
            String seqName = createdSeqs.get(0);

            qs = "SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" + seqName +  "'";
            log.debug(qs);
            rs = s.executeQuery(qs);
            assert !rs.next();
        }
        finally
        {
            if (s != null)
            {
                s.close();
                s = null;
            }

            if (c != null)
            {
                c.close();
                c = null;
            }
        }

        tt.createAuditTables();

        // make sure DDL elements exist

        try
        {
            c = DDL.getRawConnection(tt.getConnectionDriverClassName(), tt.getConnectionUrl(),
                                     tt.getConnectionUsername(), tt.getConnectionPassword());

            // TODO this test is specific to Oracle, needs to be generalized
            s = c.createStatement();

            List<String> createdTables = schema.getTargetNames(DDLAction.CREATE, DDLType.TABLE);
            assert createdTables.size() == 1;
            String tableName = createdTables.get(0);

            String qs = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME = '" + tableName + "'";
            log.debug(qs);
            ResultSet rs = s.executeQuery(qs);
            assert rs.next();
            assert !rs.next();

            List<String> createdSeqs = schema.getTargetNames(DDLAction.CREATE, DDLType.SEQUENCE);
            assert createdSeqs.size() == 1;
            String seqName = createdSeqs.get(0);

            qs = "SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" + seqName +  "'";
            log.debug(qs);
            rs = s.executeQuery(qs);
            assert rs.next();
            assert !rs.next();
        }
        finally
        {
            if (s != null)
            {
                s.close();
                s = null;
            }

            if (c != null)
            {
                c.close();
                c = null;
            }
        }

        tt.dropAuditTables();

        // make sure DDL elements are gone

        try
        {
            c = DDL.getRawConnection(tt.getConnectionDriverClassName(), tt.getConnectionUrl(),
                                     tt.getConnectionUsername(), tt.getConnectionPassword());

            // TODO this test is specific to Oracle, needs to be generalized
            s = c.createStatement();

            List<String> createdTables = schema.getTargetNames(DDLAction.CREATE, DDLType.TABLE);
            assert createdTables.size() == 1;
            String tableName = createdTables.get(0);

            String qs = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME = '" + tableName + "'";
            log.debug(qs);
            ResultSet rs = s.executeQuery(qs);
            assert !rs.next();

            List<String> createdSeqs = schema.getTargetNames(DDLAction.CREATE, DDLType.SEQUENCE);
            assert createdSeqs.size() == 1;
            String seqName = createdSeqs.get(0);

            qs = "SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" + seqName +  "'";
            log.debug(qs);
            rs = s.executeQuery(qs);
            assert !rs.next();
        }
        finally
        {
            if (s != null)
            {
                s.close();
                s = null;
            }

            if (c != null)
            {
                c.close();
                c = null;
            }
        }
    }

    @Test(enabled = true)
    public void testExternalDDLFile2() throws Exception
    {
        // we simulate the testing environment, by creating an instance of JTATransactionTest
        // by ourselves and making sure it creates and then drops the schema correctly. JTA is not
        // essential here, LocalTransactionTest would have done as well, but wasn't fully
        // implemented at the time of the writing.

        // make sure tables and sequences specified in 'ddlFile2.sql' do not exist in database.

        String f = "1.0.0.Alpha6.sql";
        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(f);

        if (is == null)
        {
            throw new Error("couldn't find " + f);
        }

        DDLSchema schema = new DDLSchema(is);

        LocalTransactionTest tt = new LocalTransactionTest();
        tt.setUseExternallyCreatedSchema(true);
        tt.setSchemaDDLFileName(f);

        Connection c = null;
        Statement s = null;

        try
        {
            c = DDL.getRawConnection(tt.getConnectionDriverClassName(), tt.getConnectionUrl(),
                                     tt.getConnectionUsername(), tt.getConnectionPassword());

            // TODO this test is specific to Oracle, needs to be generalized
            s = c.createStatement();

            List<String> tablesToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.TABLE);
            for(String table: tablesToCreate)
            {
                String qs = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME = '" + table + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert !rs.next();
            }

            List<String> indexesToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.INDEX);
            for(String index: indexesToCreate)
            {
                String qs = "SELECT TABLE_NAME FROM USER_INDEXES WHERE INDEX_NAME = '" + index + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert !rs.next();
            }

            List<String> seqsToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.SEQUENCE);
            for(String seq: seqsToCreate)
            {
                String qs = "SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" +
                            seq + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert !rs.next();
            }
        }
        finally
        {
            if (s != null)
            {
                s.close();
                s = null;
            }

            if (c != null)
            {
                c.close();
                c = null;
            }
        }


        tt.createAuditTables();

        // make sure DDL elements exist

        try
        {
            c = DDL.getRawConnection(tt.getConnectionDriverClassName(), tt.getConnectionUrl(),
                                     tt.getConnectionUsername(), tt.getConnectionPassword());

            // TODO this test is specific to Oracle, needs to be generalized
            s = c.createStatement();

            List<String> tablesToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.TABLE);
            for(String table: tablesToCreate)
            {
                String qs = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME = '" + table + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert rs.next();
                assert !rs.next();
            }

            List<String> indexesToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.INDEX);
            for(String index: indexesToCreate)
            {
                String qs = "SELECT TABLE_NAME FROM USER_INDEXES WHERE INDEX_NAME = '" + index + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert rs.next();
                assert !rs.next();

            }

            List<String> seqsToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.SEQUENCE);
            for(String seq: seqsToCreate)
            {
                String qs = "SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" +
                            seq + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert rs.next();
                assert !rs.next();

            }
        }
        finally
        {
            if (s != null)
            {
                s.close();
                s = null;
            }

            if (c != null)
            {
                c.close();
                c = null;
            }
        }

        tt.dropAuditTables();

        // make sure DDL elements are gone

        try
        {
            c = DDL.getRawConnection(tt.getConnectionDriverClassName(), tt.getConnectionUrl(),
                                     tt.getConnectionUsername(), tt.getConnectionPassword());

            // TODO this test is specific to Oracle, needs to be generalized
            s = c.createStatement();

            List<String> tablesToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.TABLE);
            for(String table: tablesToCreate)
            {
                String qs = "SELECT TABLE_NAME FROM USER_TABLES WHERE TABLE_NAME = '" + table + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert !rs.next();
            }

            List<String> indexesToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.INDEX);
            for(String index: indexesToCreate)
            {
                String qs = "SELECT TABLE_NAME FROM USER_INDEXES WHERE INDEX_NAME = '" + index + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert !rs.next();
            }

            List<String> seqsToCreate = schema.getTargetNames(DDLAction.CREATE, DDLType.SEQUENCE);
            for(String seq: seqsToCreate)
            {
                String qs = "SELECT SEQUENCE_NAME FROM USER_SEQUENCES WHERE SEQUENCE_NAME = '" +
                            seq + "'";
                log.debug(qs);
                ResultSet rs = s.executeQuery(qs);
                assert !rs.next();
            }
        }
        finally
        {
            if (s != null)
            {
                s.close();
                s = null;
            }

            if (c != null)
            {
                c.close();
                c = null;
            }
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
