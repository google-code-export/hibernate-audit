package com.googlecode.hibernate.audit.model;

import org.hibernate.Session;

/**
 * A test helper that allow access to package protected methods for testing.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class TestAccessHelper
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static AuditType AuditType_getInstanceFromDatabase(Class c,
                                                              boolean create,
                                                              Session s)
    {
        return AuditType.getInstanceFromDatabase(c, create, s);
    }

    public static AuditEntityType AuditEntityType_getInstanceFromDatabase(Class c,
                                                                          Class c2,
                                                                          boolean create,
                                                                          Session s)
    {
        return AuditEntityType.getInstanceFromDatabase(c, c2, create, s);
    }

    public static AuditCollectionType AuditCollectionType_getInstanceFromDatabase(Class c,
                                                                                  Class c2,
                                                                                  boolean create,
                                                                                  Session s)
    {
        return AuditCollectionType.getInstanceFromDatabase(c, c2, create, s);
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
