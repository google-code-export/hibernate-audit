/**
 * 
 */
package com.googlecode.hibernate.audit.model;

import com.googlecode.hibernate.audit.model.clazz.AuditType;

public class AuditLogicalGroup {
	protected Long id;
	protected AuditType auditType;
	protected String externalId;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuditType getAuditType() {
		return auditType;
	}

	public void setAuditType(AuditType auditType) {
		this.auditType = auditType;
	}

	public String getExternalId() {
		return externalId;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
}
