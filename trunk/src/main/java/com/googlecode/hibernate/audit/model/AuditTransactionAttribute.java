package com.googlecode.hibernate.audit.model;

public class AuditTransactionAttribute {
	protected Long id;
	protected AuditTransaction auditTransaction;
	protected Long attributeId;
	protected String attributeValue;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuditTransaction getAuditTransaction() {
		return auditTransaction;
	}

	public void setAuditTransaction(AuditTransaction auditTransaction) {
		this.auditTransaction = auditTransaction;
	}

	public Long getAttributeId() {
		return attributeId;
	}

	public void setAttributeId(Long attributeId) {
		this.attributeId = attributeId;
	}

	public String getAttributeValue() {
		return attributeValue;
	}

	public void setAttributeValue(String attributeValue) {
		this.attributeValue = attributeValue;
	}
}
