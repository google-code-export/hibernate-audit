package com.googlecode.hibernate.audit.model.transaction.record.field.value;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class EntityReferenceAuditValue extends AuditValue {
	@Column(name = "ENTITY_REFERENCE_ID", length = 100)
	private String entityReferenceId;

	public boolean isOfType(Class<? extends AuditValue> valueClass) {
		if (EntityReferenceAuditValue.class.equals(valueClass)) {
			return true;
		}
		return false;
	}

	public EntityReferenceAuditValue() {
	}
	
	public EntityReferenceAuditValue(String entityReferenceId) {
		this.entityReferenceId = entityReferenceId;
	}
	
	public String getEntityReferenceId() {
		return entityReferenceId;
	}

	public void setEntityReferenceId(String entityReferenceId) {
		this.entityReferenceId = entityReferenceId;
	}
}
