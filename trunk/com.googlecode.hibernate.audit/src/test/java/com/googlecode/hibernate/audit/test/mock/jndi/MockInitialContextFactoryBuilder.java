package com.googlecode.hibernate.audit.test.mock.jndi;

import javax.naming.spi.InitialContextFactoryBuilder;
import javax.naming.spi.InitialContextFactory;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Map;

/**
 * A MockInitialContextFactoryBuilder that allows us to hijack JNDI for testing purposes.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockInitialContextFactoryBuilder implements InitialContextFactoryBuilder
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Map<String, Object> initialJNDIContent;

    // Constructors --------------------------------------------------------------------------------

    public MockInitialContextFactoryBuilder(Map<String, Object> initialJNDIContent)
    {
        this.initialJNDIContent = initialJNDIContent;
    }

    // InitialContextFactoryBuilder implementation -------------------------------------------------

    public InitialContextFactory createInitialContextFactory(Hashtable<?, ?> environment)
        throws NamingException
    {
        return new MockInitialContextFactory(initialJNDIContent);
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
