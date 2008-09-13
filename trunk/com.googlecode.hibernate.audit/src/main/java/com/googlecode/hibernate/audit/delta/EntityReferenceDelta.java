package com.googlecode.hibernate.audit.delta;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
class EntityReferenceDelta<T> extends MemberVariableDeltaSupport implements ScalarDelta<T> 
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // ScalarDelta implementation ------------------------------------------------------------------

    public boolean isEntity()
    {
        return true;
    }

    public boolean isPrimitive()
    {
        return false;
    }

    // Public --------------------------------------------------------------------------------------

    // MemberVariableDelta implementation ----------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
