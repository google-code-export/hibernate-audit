package com.googlecode.hibernate.audit.model.object;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;

public abstract class AuditObject {
	protected Long id;

	protected AuditEvent auditEvent;
	protected AuditType auditType;

	protected List<AuditObjectProperty> auditObjectProperties;

	public Long getId() {
		return id;
	}

	public void setId(Long newId) {
		id = newId;
	}

	public AuditEvent getAuditEvent() {
		return auditEvent;
	}

	public void setAuditEvent(AuditEvent auditEvent) {
		this.auditEvent = auditEvent;
	}

	public AuditType getAuditType() {
		return auditType;
	}

	public void setAuditType(AuditType auditType) {
		this.auditType = auditType;
	}

	public List<AuditObjectProperty> getAuditObjectProperties() {
		if (auditObjectProperties == null) {
			auditObjectProperties = new ArrayList<AuditObjectProperty>();
		}
		return auditObjectProperties;
	}

	public void setAuditObjectProperties(
			List<AuditObjectProperty> auditObjectProperties) {
		this.auditObjectProperties = auditObjectProperties;
	}
}
