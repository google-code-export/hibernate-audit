package com.googlecode.hibernate.audit.test.util;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
class InvocationRecord
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String methodName;
    private Object[] args;

    // Constructors --------------------------------------------------------------------------------

    InvocationRecord(String methodName, Object[] args)
    {
        this.methodName = methodName;
        this.args = args;
    }

    // Public --------------------------------------------------------------------------------------

    public String getMethodName()
    {
        return methodName;
    }

    public Object[] getArguments()
    {
        return args;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
