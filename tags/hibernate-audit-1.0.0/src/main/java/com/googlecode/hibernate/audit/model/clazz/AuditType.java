/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.model.clazz;

import java.util.ArrayList;
import java.util.List;

public class AuditType {
	public static final char ENTITY_TYPE = 'E';
	public static final char COMPONENT_TYPE = 'C';
	public static final char COLLECTION_TYPE = 'A';
	public static final char PRIMITIVE_TYPE = 'P';

	protected Long id;
	
	protected String className;
	protected String label;
	protected char type;
	protected List<AuditTypeField> auditFields;

	public List<AuditTypeField> getAuditFields() {
		if (auditFields == null) {
			auditFields = new ArrayList<AuditTypeField>();
		}
		return auditFields;
	}

	public void setAuditFields(List<AuditTypeField> auditFields) {
		this.auditFields = auditFields;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long newId) {
		id = newId;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String newClassName) {
		className = newClassName;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String newLabel) {
		label = newLabel;
	}

	public char getType() {
		return type;
	}

	public void setType(char type) {
		this.type = type;
	}
}