package com.googlecode.hibernate.audit.model.property;

import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.AuditObject;

public abstract class AuditObjectProperty {
	protected Long id;
	protected AuditObject auditObject;
	protected AuditTypeField auditField;
	protected Long index;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuditObject getAuditObject() {
		return auditObject;
	}

	public void setAuditObject(AuditObject auditObject) {
		this.auditObject = auditObject;
	}

	public AuditTypeField getAuditField() {
		return auditField;
	}

	public void setAuditField(AuditTypeField auditField) {
		this.auditField = auditField;
	}

	public Long getIndex() {
		return index;
	}

	public void setIndex(Long index) {
		this.index = index;
	}
}
