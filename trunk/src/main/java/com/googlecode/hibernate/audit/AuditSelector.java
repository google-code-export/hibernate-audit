package com.googlecode.hibernate.audit;

/**
 * Implemented by the application to specify the persistent entitites to be audited.
 *
 * An alternative to @Audited annotation. If both present (annotations and AuditSelector), the AuditSelector takes
 * precedence.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 */
public interface AuditSelector
{
    boolean isAuditEnabled(Class c);
}
