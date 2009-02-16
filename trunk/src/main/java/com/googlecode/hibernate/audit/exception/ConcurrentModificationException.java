package com.googlecode.hibernate.audit.exception;

import org.hibernate.HibernateException;

public class ConcurrentModificationException extends HibernateException {

    public ConcurrentModificationException(String s) {
        super(s);
    }
}
