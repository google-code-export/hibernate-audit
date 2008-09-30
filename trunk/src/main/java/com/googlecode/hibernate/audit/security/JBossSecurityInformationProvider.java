package com.googlecode.hibernate.audit.security;

import org.apache.log4j.Logger;

import java.security.Principal;
import java.lang.reflect.Method;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
final class JBossSecurityInformationProvider implements SecurityInformationProvider
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(JBossSecurityInformationProvider.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Method getPrincipal;

        // Constructors --------------------------------------------------------------------------------

    JBossSecurityInformationProvider() throws Exception
    {
        Class securityAssociationClass = Class.forName("org.jboss.security.SecurityAssociation");
        getPrincipal = securityAssociationClass.getMethod("getPrincipal");
    }

    // SecurityInformationProvider implemenation ---------------------------------------------------

    public Principal getPrincipal()
    {
        try
        {
            return (Principal)getPrincipal.invoke(null);

        }
        catch(Exception e)
        {
            log.debug("could not obtain principal", e);
        }

        return null;
    }

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
