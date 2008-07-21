package com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value;

import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

import com.googlecode.hibernate.audit.model_obsolete.transaction.record.AuditTransactionComponentRecord;

@Embeddable
public class ComponentReferenceAuditValue extends AuditValue {
	@OneToOne
	@JoinColumn(name = "COMPONENT_REFERENCE_ID")
	private AuditTransactionComponentRecord componentAuditObject;

	public boolean isOfType(Class<? extends AuditValue> valueClass) {
		if (ComponentReferenceAuditValue.class.equals(valueClass)) {
			return true;
		}
		return false;
	}

	public ComponentReferenceAuditValue() {
	}
	
	public ComponentReferenceAuditValue(AuditTransactionComponentRecord componentAuditObject) {
		this.componentAuditObject = componentAuditObject;
	}
	
	public AuditTransactionComponentRecord getComponentAuditObject() {
		return componentAuditObject;
	}

	public void setComponentAuditObject(AuditTransactionComponentRecord componentAuditObject) {
		this.componentAuditObject = componentAuditObject;
	}
}
