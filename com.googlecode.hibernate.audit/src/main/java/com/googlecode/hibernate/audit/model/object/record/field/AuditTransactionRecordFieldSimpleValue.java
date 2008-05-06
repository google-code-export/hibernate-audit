package com.googlecode.hibernate.audit.model.transaction.record.field;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;

import com.googlecode.hibernate.audit.model.transaction.record.field.value.SimpleAuditValue;

@javax.persistence.Entity
@DiscriminatorValue(value = "SIMPLE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
public class AuditTransactionRecordFieldSimpleValue extends AuditTransactionRecordFieldValue {
	@Embedded
	private SimpleAuditValue value;
	
	public SimpleAuditValue getValue() {
		return value;
	}

	public void setValue(SimpleAuditValue value) {
		this.value = value;
	}
}
