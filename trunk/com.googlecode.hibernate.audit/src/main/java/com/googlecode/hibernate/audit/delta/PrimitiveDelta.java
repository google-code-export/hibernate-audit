package com.googlecode.hibernate.audit.delta;

/**
 * An instance of this type is created with Deltas.createPrimitiveType(...);
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface PrimitiveDelta<T>
{
    /**
    * @return the name of the class variable that holds the primitive.
     */
    String getName();

    T getValue();

    /**
     * @return the actual instance of the parameterized type, at runtime, or null if the type
     *         cannot determined (the primitive value is null).
     */
    Class getType();

}
