package com.googlecode.hibernate.audit.model.property;

public class SimpleObjectProperty extends AuditObjectProperty {
	protected String value;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

}
