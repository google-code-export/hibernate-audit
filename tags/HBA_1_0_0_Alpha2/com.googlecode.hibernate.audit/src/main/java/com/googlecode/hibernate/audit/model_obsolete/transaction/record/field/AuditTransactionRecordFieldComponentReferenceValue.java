package com.googlecode.hibernate.audit.model_obsolete.transaction.record.field;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.ComponentReferenceAuditValue;

@javax.persistence.Entity
@DiscriminatorValue(value = "COMPONENT_REF")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AuditTransactionRecordFieldComponentReferenceValue extends AuditTransactionRecordFieldValue {
	@Embedded
	private ComponentReferenceAuditValue value;

	public ComponentReferenceAuditValue getValue() {
		return value;
	}

	public void setValue(ComponentReferenceAuditValue value) {
		this.value = value;
	}
}
