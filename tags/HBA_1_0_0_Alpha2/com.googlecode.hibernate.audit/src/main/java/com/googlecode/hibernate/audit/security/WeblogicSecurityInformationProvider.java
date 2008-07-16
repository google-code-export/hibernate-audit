package com.googlecode.hibernate.audit.security;

import java.security.Principal;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
final class WeblogicSecurityInformationProvider implements SecurityInformationProvider
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    WeblogicSecurityInformationProvider() throws Exception
    {
        throw new Exception("NOT YET IMPLEMENTED");
    }

    // SecurityInformationProvider implemenation ---------------------------------------------------

    public Principal getPrincipal()
    {
        throw new RuntimeException("NOT YET IMPLEMENTED"); //To change body of implemented methods use File | Settings | File Templates.
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
