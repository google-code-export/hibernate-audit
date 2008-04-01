package com.googlecode.hibernate.audit.model;

import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "AUDIT_COMPONENT_PROPERTY_VAL")
public class AuditComponentPropertyValue extends AuditObjectPropertyValue {
	
	@ManyToOne
	@JoinColumn(name = "AUDIT_OBJECT_ID")
	private AuditObject auditObject;

	public AuditObject getAuditObject() {
		return auditObject;
	}

	public void setAuditObject(AuditObject auditObject) {
		this.auditObject = auditObject;
	}
}
