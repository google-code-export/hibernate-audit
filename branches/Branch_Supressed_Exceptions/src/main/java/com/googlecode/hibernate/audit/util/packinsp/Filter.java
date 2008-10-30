package com.googlecode.hibernate.audit.util.packinsp;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface Filter
{
    /**
     * @return true if the class is to be included in the final result, false otherwise.
     */
    boolean accept(Class c);
}
