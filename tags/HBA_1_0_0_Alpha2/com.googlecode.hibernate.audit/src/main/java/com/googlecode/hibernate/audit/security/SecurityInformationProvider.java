package com.googlecode.hibernate.audit.security;

import java.security.Principal;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 */
public interface SecurityInformationProvider 
{
    /**
     * @return the principal associated with the current security context, if any, or null
     *         otherwise.
     */
    Principal getPrincipal();
}
