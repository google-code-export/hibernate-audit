package com.googlecode.hibernate.audit.model;

import javax.persistence.Column;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

@javax.persistence.Entity
@javax.persistence.Table(name = "AUDIT_SIMPLE_PROPERTY_VAL")
@Inheritance(strategy = InheritanceType.JOINED)
public class AuditSimplePropertyValue extends AuditObjectPropertyValue {

	@Column(name = "VALUE", length = 4000)
	private String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
