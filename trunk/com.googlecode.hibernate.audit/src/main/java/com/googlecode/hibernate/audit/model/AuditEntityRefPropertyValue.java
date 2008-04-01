package com.googlecode.hibernate.audit.model;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@javax.persistence.Entity
@javax.persistence.Table(name = "AUDIT_ENTITY_REF_PROPERTY_VAL")
@Inheritance(strategy = InheritanceType.JOINED)
public class AuditEntityRefPropertyValue extends AuditObjectPropertyValue {
	@Column(name = "ENTITY_REF_ID", length = 100)
	private String entityRefId;
	
	public String getEntityRefId() {
		return entityRefId;
	}

	public void setEntityRefId(String entityRefId) {
		this.entityRefId = entityRefId;
	}
}
