package com.googlecode.hibernate.audit.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "ENTITY_REF_PROPERTY")
public class AuditEntityRefProperty extends AuditObjectProperty {
	
	public void addValue(AuditEntityRefPropertyValue value) {
		value.setAuditObjectProperty(this);
		values.add(value);
	}
	
}
