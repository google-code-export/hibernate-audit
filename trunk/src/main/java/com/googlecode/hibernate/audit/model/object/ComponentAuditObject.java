package com.googlecode.hibernate.audit.model.object;

public class ComponentAuditObject extends AuditObject {
	protected AuditObject parentAuditObject;

	public AuditObject getParentAuditObject() {
		return parentAuditObject;
	}

	public void setParentAuditObject(AuditObject parentAuditObject) {
		this.parentAuditObject = parentAuditObject;
	}

}
