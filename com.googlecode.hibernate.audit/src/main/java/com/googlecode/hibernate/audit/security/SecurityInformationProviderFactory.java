package com.googlecode.hibernate.audit.security;

import org.apache.log4j.Logger;

/**
 * Creates SecurityInformationProvider instance corresponding to the environment it is called from.
 * 
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 */
public class SecurityInformationProviderFactory
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(SecurityInformationProviderFactory.class);

    // Static --------------------------------------------------------------------------------------

    /**
     * @return a valid SecurityInformationProvider instance. It must never return null.
     *
     * @throws Exception if such an instance cannot be created. It is recommended to configure
     *         the exception with a human readable error message.
     */
    public static SecurityInformationProvider getSecurityInformationProvider() throws Exception
    {
        // these security information providers are mutually exclusive, so no danger of overlap

        try
        {
            return new JBossSecurityInformationProvider();
        }
        catch(Exception e)
        {
            // ok, log and try next
            log.debug("jboss provider not available: " + e.getMessage());
        }

        try
        {
            return new WeblogicSecurityInformationProvider();
        }
        catch(Exception e)
        {
            // ok, log and try next
            log.debug("weblogic provider not available" + e.getMessage());
        }

        log.warn("could not instantiate an AS-specific security information provider, " +
                 "falling back to default NullSecurityInformationProvider");

        return new NullSecurityInformationProvider();

    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
