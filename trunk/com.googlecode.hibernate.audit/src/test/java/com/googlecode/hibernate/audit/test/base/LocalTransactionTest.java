package com.googlecode.hibernate.audit.test.base;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;
import org.testng.annotations.Test;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(enabled = false) // TEST_OFF
public class LocalTransactionTest extends ConfigurableEnvironmentSupport
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Override
    @BeforeTest
    public void beforeTest() throws Exception
    {
        // make sure no JTA environment remnants are in sight
        super.beforeTest();
    }

    @Override
    @AfterTest
    public void afterTest() throws Exception
    {
        super.afterTest();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    @Override
    protected String getHibernateConfigurationFileName()
    {
        return "/hibernate-thread.cfg.xml";
    }
    @Override
    protected TransactionType getTransactionType()
    {
        return TransactionType.LOCAL;
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
