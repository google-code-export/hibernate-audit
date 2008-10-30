package com.googlecode.hibernate.audit.delta;

/**
 * Implementations of this interface encasulate changes refering to content of various instances
 * referred to by member variables. The change can affect a primitive, an entity or a collection.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface MemberVariableDelta 
{
    /**
    * @return the name of the class variable that holds the primitive.
     */
    String getName();

}
