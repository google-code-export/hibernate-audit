package com.googlecode.hibernate.audit.test.util.data;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface ApplicationLevelImmutable
{
    // we declare this to "taint" the class and make it "mutable" in the common acception
    void setSomething(Object o);
}
