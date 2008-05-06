package com.googlecode.hibernate.audit.model.transaction.record.field.value;

import javax.persistence.Embeddable;

@Embeddable
public abstract class AuditValue {
	public abstract boolean isOfType(Class<? extends AuditValue> valueClass);
}
