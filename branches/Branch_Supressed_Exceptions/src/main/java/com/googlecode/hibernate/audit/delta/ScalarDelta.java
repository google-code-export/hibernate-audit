package com.googlecode.hibernate.audit.delta;

/**
 * It represent a change occuring to a primitive or entity reference.
 *
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
public interface ScalarDelta<T> extends MemberVariableDelta
{
    boolean isEntityReference();
    boolean isPrimitive();

}
