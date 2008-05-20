package com.googlecode.hibernate.audit.model.transaction.record.field;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import com.googlecode.hibernate.audit.model.AuditOperation;
import com.googlecode.hibernate.audit.model.clazz.AuditClassProperty;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionRecord;

@Entity
@Table(name = "AUDIT_TRANSACTION_RECORD_FIELD")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TRAN_RECORD_FIELD_ID_SEQ")
public class AuditTransactionRecordField {
	/** the id. */
	@Id
	@Column(name = "AUDIT_TRAN_RECORD_FIELD_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "AUDIT_CLASS_PROP_ID")
	private AuditClassProperty auditClassProperty;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "AUDIT_TRANSACTION_RECORD_ID")
	private AuditTransactionRecord auditTransactionRecord;
	
	@Column(name = "OPERATION")
	@Enumerated(value = EnumType.STRING)
	private AuditOperation operation;

/*	@OneToMany(mappedBy = "recordField")
	private Set<AuditTransactionRecordFieldValue> values = new HashSet<AuditTransactionRecordFieldValue>();
*/	
	public AuditTransactionRecord getAuditTransactionRecord() {
		return auditTransactionRecord;
	}

	public void setAuditTransactionRecord(AuditTransactionRecord auditTransactionRecord) {
		this.auditTransactionRecord = auditTransactionRecord;
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the operation
	 */
	public AuditOperation getOperation() {
		return operation;
	}

	/**
	 * @param operation the operation to set
	 */
	public void setOperation(AuditOperation operation) {
		this.operation = operation;
	}

	public AuditClassProperty getAuditClassProperty() {
		return auditClassProperty;
	}

	public void setAuditClassProperty(AuditClassProperty auditClassProperty) {
		this.auditClassProperty = auditClassProperty;
	}

/*	public Set<AuditTransactionRecordFieldValue> getValues() {
		return Collections.unmodifiableSet(values);
	}

	public void addValue(AuditTransactionRecordFieldValue value) {
		value.setRecordField(this);
		values.add(value);
	}
	
	public AuditTransactionRecordFieldValue getValue() {
		if (!values.isEmpty()) {
			return values.iterator().next();
		}
		
		return null;
	}
	
	public void setValue(AuditTransactionRecordFieldValue value) {
		values.clear();
		value.setRecordField(this);
		values.add(value);
	}
*/
}
