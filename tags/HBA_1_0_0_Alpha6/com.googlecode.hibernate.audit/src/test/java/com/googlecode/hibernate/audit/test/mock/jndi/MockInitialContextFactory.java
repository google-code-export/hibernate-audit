package com.googlecode.hibernate.audit.test.mock.jndi;

import javax.naming.spi.InitialContextFactory;
import javax.naming.NamingException;
import javax.naming.Context;
import java.util.Hashtable;
import java.util.Map;

/**
 * A MockInitialContextFactory that allows us to hijack JNDI for testing purposes.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockInitialContextFactory implements InitialContextFactory
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Map<String, Object> initialJNDIContent;

    // Constructors --------------------------------------------------------------------------------

    public MockInitialContextFactory(Map<String, Object> initialJNDIContent)
    {
        this.initialJNDIContent = initialJNDIContent;
    }

    // InitialContextFactory implementation --------------------------------------------------------

    public Context getInitialContext(Hashtable<?, ?> environment) throws NamingException
    {
        return new MockContext(initialJNDIContent);
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
