package com.googlecode.hibernate.audit.util;

import java.lang.reflect.Method;

/**
 * Miscellaneous static utilities.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Util
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    public static String methodToString(Method m)
    {
        Class c = m.getDeclaringClass();

        String className = c.getName();

        int i = className.lastIndexOf(".");

        if (i != -1)
        {
            className = className.substring(i + 1);
        }

        return className + "." + m.getName() + "()";
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
