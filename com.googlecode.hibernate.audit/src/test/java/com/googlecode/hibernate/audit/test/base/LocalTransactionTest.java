package com.googlecode.hibernate.audit.test.base;

import org.testng.annotations.BeforeTest;
import org.testng.annotations.AfterTest;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
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
    }

    @Override
    @AfterTest
    public void afterTest() throws Exception
    {
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    @Override
    protected String getHibernateConfigurationFileName()
    {
        return "/hibernate-thread.cfg.xml";
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
