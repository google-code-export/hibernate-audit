package com.googlecode.hibernate.audit.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "SIMPLE_PROPERTY")
public class AuditSimpleProperty extends AuditObjectProperty {
	
	public void addValue(AuditSimplePropertyValue value) {
		value.setAuditObjectProperty(this);
		values.add(value);
	}
}
