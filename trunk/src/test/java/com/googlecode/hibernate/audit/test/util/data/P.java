package com.googlecode.hibernate.audit.test.util.data;

/**
 * An interface to be implemented by entities involved with HibernateProxy test cases.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$</tt>
 */
public interface P
{
    Q getQ();
    void setQ(Q q);
}
