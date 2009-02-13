/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.model.clazz;

public class AuditTypeField {

	protected Long id;
	protected String name;
	protected String label;
	protected AuditType ownerType;
	protected AuditType fieldType;

	public Long getId() {
		return id;
	}

	public void setId(Long newId) {
		id = newId;
	}

	public String getName() {
		return name;
	}

	public void setName(String newName) {
		name = newName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String newLabel) {
		label = newLabel;
	}

	public AuditType getOwnerType() {
		return ownerType;
	}

	public void setOwnerType(AuditType newOwnerType) {
		ownerType = newOwnerType;
	}

	public AuditType getFieldType() {
		return fieldType;
	}

	public void setFieldType(AuditType newFieldType) {
		fieldType = newFieldType;
	}
}