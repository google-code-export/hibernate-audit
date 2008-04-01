package com.googlecode.hibernate.audit.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue(value = "COMPONENT_PROPERTY")
public class AuditComponentProperty extends AuditObjectProperty {
	
	public void addValue(AuditComponentPropertyValue value) {
		value.setAuditObjectProperty(this);
		values.add(value);
	}
}
