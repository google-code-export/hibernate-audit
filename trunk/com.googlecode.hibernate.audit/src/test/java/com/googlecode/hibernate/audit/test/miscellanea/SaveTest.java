package com.googlecode.hibernate.audit.test.miscellanea;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
import com.googlecode.hibernate.audit.test.AuditTest;
import com.googlecode.hibernate.audit.test.miscellanea.model.A;
import com.googlecode.hibernate.audit.model.transaction.AuditTransaction;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionRecord;
import com.googlecode.hibernate.audit.model.AuditOperation;

import java.util.List;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class SaveTest extends AuditTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(SaveTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public void testSave() throws Exception
    {
        Session session = getSession();
        Transaction t = session.beginTransaction();

        A a = new A();
        a.setName("alice");

        session.save(a);

        t.commit();

        // check audit database; currently we do it via raw access to the database, will be
        // replaced with the API

        session = getSession();
        t = session.beginTransaction();

        // looking up the AuditTransaction

        Query q = session.createQuery("from " + AuditTransaction.class.getName());
        List result = q.list();

        assert result.size() == 1;

        AuditTransaction at = (AuditTransaction)result.get(0);

        // looking up the record

        q = session.createQuery("from " + AuditTransactionRecord.class.getName());
        result = q.list();

        assert result.size() == 1;

        AuditTransactionRecord rec = (AuditTransactionRecord)result.get(0);

        assert at.getId().equals(rec.getAuditTransaction().getId());
        assert AuditOperation.INSERT.equals(rec.getOperation());        

        t.commit();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    @Override
    protected String[] getTestTables()
    {
        return new String[] { "A" };
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
