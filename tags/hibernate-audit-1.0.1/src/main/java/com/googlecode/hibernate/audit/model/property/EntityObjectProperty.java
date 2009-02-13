package com.googlecode.hibernate.audit.model.property;

public class EntityObjectProperty extends AuditObjectProperty {
	protected String targetEntityId;

	public String getTargetEntityId() {
		return targetEntityId;
	}

	public void setTargetEntityId(String targetEntityId) {
		this.targetEntityId = targetEntityId;
	}
}
