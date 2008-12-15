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
public enum ChangeType
{
    // Comparator aspect of this enumeration is used, so order matters!
    
    INSERT,
    UPDATE,
    DELETE;

    public static char toCode(ChangeType ct)
    {
        if (INSERT.equals(ct))
        {
            return 'C';
        }
        else if (UPDATE.equals(ct))
        {
            return 'U';
        }
        else if (DELETE.equals(ct))
        {
            return 'D';
        }

        throw new IllegalArgumentException("Unknown change type: " + ct);
    }

}
