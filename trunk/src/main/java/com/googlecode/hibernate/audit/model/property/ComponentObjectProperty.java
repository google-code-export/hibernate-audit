package com.googlecode.hibernate.audit.model.property;

import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;

public class ComponentObjectProperty extends AuditObjectProperty {
	protected ComponentAuditObject targetComponentAuditObject;

	public ComponentAuditObject getTargetComponentAuditObject() {
		return targetComponentAuditObject;
	}

	public void setTargetComponentAuditObject(
			ComponentAuditObject targetComponentAuditObject) {
		this.targetComponentAuditObject = targetComponentAuditObject;
	}
}
