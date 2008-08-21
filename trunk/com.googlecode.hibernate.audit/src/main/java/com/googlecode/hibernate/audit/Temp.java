package com.googlecode.hibernate.audit;

import java.util.List;
import java.util.ArrayList;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Temp
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    public Class c;
    public Long id;
    public List<TempPair> pairs = new ArrayList<TempPair>();

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public void add(String name, Object value)
    {
        TempPair tp = new TempPair();
        tp.name = name;
        tp.value = value;
        pairs.add(tp);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
