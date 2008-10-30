package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import java.util.Set;
import java.util.Collections;
import java.lang.annotation.Annotation;

import com.googlecode.hibernate.audit.util.packinsp.PackageInspector;
import com.googlecode.hibernate.audit.util.packinsp.Filter;

/**
 * Audit entities manipulation utility.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Entities
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    private static Set<Class> auditEntities;

    /**
     * @return all current audit entities.
     *
     * @exception Exception if anything goes wrong while rummaging through the filesystem.
     */
    public static Set<Class> getAuditEntities() throws Exception
    {
        synchronized(Entities.class)
        {
            if (auditEntities == null)
            {
                PackageInspector pi = new PackageInspector(Entities.class);
                Set<Class> result = pi.inspect(new Filter()
                {
                    public boolean accept(Class c)
                    {
                        for(Annotation a: c.getAnnotations())
                        {
                            if (Entity.class.equals(a.annotationType()))
                            {
                                return true;
                            }
                        }

                        return false;
                    }
                });

                auditEntities = Collections.unmodifiableSet(result);
            }

            return auditEntities;
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
