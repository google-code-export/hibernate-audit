package com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SimpleAuditValue extends AuditValue {
	@Column(name = "SIMPLE_VALUE", length = 4000)
	private String value;

	public SimpleAuditValue() {
	}

	public SimpleAuditValue(String value) {
		this.value = value;
	}

	public boolean isOfType(Class<? extends AuditValue> valueClass) {
		if (SimpleAuditValue.class.equals(valueClass)) {
			return true;
		}
		return false;
	}
	
	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
}
