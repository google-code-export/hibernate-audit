package com.googlecode.hibernate.audit.test.miscellanea;

import org.testng.annotations.Test;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.Query;
//import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.test.AuditTest;
import com.googlecode.hibernate.audit.test.miscellanea.model.A;

import java.util.List;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class SimplestTest extends AuditTest
{
    // Constants -----------------------------------------------------------------------------------

    //private static final Logger log = Logger.getLogger(SimplestTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test
    public void testSave() throws Exception
    {
        Session session = getSession();
        Transaction t = session.beginTransaction();

        A a = new A();
        a.setName("alice");

        session.save(a);

        t.commit();
    }

    @Test
    public void testGet() throws Exception
    {
        Session session = getSession();
        Transaction t = session.beginTransaction();

        Query q = session.createQuery("from A a");

        List result = q.list();

        t.commit();

        assert !result.isEmpty();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
