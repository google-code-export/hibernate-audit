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
public class JTATransactionTest extends ConfigurableEnvironmentSupport
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
        startJTAEnvironment();
    }

    @Override
    @AfterTest
    public void afterTest() throws Exception
    {
        stopJTAEnvironment();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    @Override
    protected String getHibernateConfigurationFileName()
    {
        return "/hibernate-jta.cfg.xml";
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
