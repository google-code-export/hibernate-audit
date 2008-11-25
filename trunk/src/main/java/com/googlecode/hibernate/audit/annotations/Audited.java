package com.googlecode.hibernate.audit.annotations;

import java.lang.annotation.Target;
import java.lang.annotation.Retention;
import static java.lang.annotation.RetentionPolicy.RUNTIME;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Target({FIELD, TYPE})
@Retention(RUNTIME)
public @interface Audited
{
    /**
     * Gives the option to disable auditing the element. By default, audit is enabled when the
     * annotation is specified.
     */
    boolean disabled() default false;

}
