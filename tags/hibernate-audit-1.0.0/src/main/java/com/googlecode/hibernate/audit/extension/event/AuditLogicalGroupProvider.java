package com.googlecode.hibernate.audit.extension.event;

import org.hibernate.Session;

import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;

public interface AuditLogicalGroupProvider {

	AuditLogicalGroup getAuditLogicalGroup(Session session, AuditEvent auditEvent);
}
