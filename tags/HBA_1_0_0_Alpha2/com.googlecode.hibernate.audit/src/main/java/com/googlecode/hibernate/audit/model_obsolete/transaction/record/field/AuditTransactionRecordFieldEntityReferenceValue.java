package com.googlecode.hibernate.audit.model_obsolete.transaction.record.field;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.EntityReferenceAuditValue;

@javax.persistence.Entity
@DiscriminatorValue(value = "ENTITY_REF")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AuditTransactionRecordFieldEntityReferenceValue extends AuditTransactionRecordFieldValue {
	@Embedded
	private EntityReferenceAuditValue value;

	public EntityReferenceAuditValue getValue() {
		return value;
	}

	public void setValue(EntityReferenceAuditValue value) {
		this.value = value;
	}
}
