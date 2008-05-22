package com.googlecode.hibernate.audit.test.miscellanea;

import org.testng.annotations.Test;
import org.hibernate.Session;
import org.hibernate.Transaction;
import com.googlecode.hibernate.audit.test.AuditTest;
import com.googlecode.hibernate.audit.test.miscellanea.model.A;

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

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test
    public void testSave() throws Exception
    {
        Session session = getSession();

        A a = new A();
        a.setName("alice");

        Transaction transaction = session.beginTransaction();

        session.save(a);

        transaction.commit();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
