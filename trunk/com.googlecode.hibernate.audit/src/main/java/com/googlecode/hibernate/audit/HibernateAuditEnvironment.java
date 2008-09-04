package com.googlecode.hibernate.audit;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public final class HibernateAuditEnvironment
{
    // Constants -----------------------------------------------------------------------------------

    public static final String HBA_PROPERTY_PREFIX = "hba.";

    /**
     * Auto export/update audit schema using hbm2ddl tool. Valid values are <tt>update</tt>,
     * <tt>create</tt>, <tt>create-drop</tt> and <tt>validate</tt>.
     */
    public static final String HBM2DDL_AUTO = HBA_PROPERTY_PREFIX + "hbm2ddl.auto";

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
