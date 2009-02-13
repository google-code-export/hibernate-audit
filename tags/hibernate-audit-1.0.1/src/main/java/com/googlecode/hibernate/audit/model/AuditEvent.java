/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.model;

import java.util.ArrayList;
import java.util.List;

import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.object.AuditObject;

public class AuditEvent {
	public static final String INSERT_AUDIT_EVENT_TYPE = "I";
	public static final String UPDATE_AUDIT_EVENT_TYPE = "U";
	public static final String DELETE_AUDIT_EVENT_TYPE = "D";

	public static final String ADD_AUDIT_EVENT_TYPE = "A";
	public static final String MODIFY_AUDIT_EVENT_TYPE = "M";
	public static final String REMOVE_AUDIT_EVENT_TYPE = "R";

	protected Long id;

	protected AuditTransaction auditTransaction;
	protected AuditType auditType;
	protected AuditLogicalGroup auditLogicalGroup;

	protected List<AuditObject> auditObjects;
	protected String type;
	protected String entityId;

	public Long getId() {
		return id;
	}

	public void setId(Long newId) {
		id = newId;
	}

	public AuditTransaction getAuditTransaction() {
		return auditTransaction;
	}

	public void setAuditTransaction(AuditTransaction newTransaction) {
		auditTransaction = newTransaction;
	}

	public AuditType getAuditType() {
		return auditType;
	}

	public void setAuditType(AuditType newAuditType) {
		auditType = newAuditType;
	}

	public String getEntityId() {
		return entityId;
	}

	public void setEntityId(String newEntityId) {
		entityId = newEntityId;
	}

	public String getType() {
		return type;
	}

	public void setType(String newType) {
		type = newType;
	}

	public List<AuditObject> getAuditObjects() {
		if (auditObjects == null) {
			auditObjects = new ArrayList<AuditObject>();
		}
		return auditObjects;
	}

	public void setAuditObjects(List<AuditObject> auditObjects) {
		this.auditObjects = auditObjects;
	}

	public AuditLogicalGroup getAuditLogicalGroup() {
		return auditLogicalGroup;
	}

	public void setAuditLogicalGroup(AuditLogicalGroup auditLogicalGroup) {
		this.auditLogicalGroup = auditLogicalGroup;
	}

}