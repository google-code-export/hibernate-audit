package com.googlecode.hibernate.audit.model.transaction.record.field;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;

import com.googlecode.hibernate.audit.model.transaction.record.field.value.AuditValue;

@javax.persistence.Entity
@javax.persistence.Table(name = "AUDIT_TRAN_RECORD_FIELD_VALUE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "VALUE_TYPE")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TRAN_REC_FLD_VAL_ID_SEQ")
public abstract class AuditTransactionRecordFieldValue {
	/** the id. */
	@Id
	@Column(name = "AUDIT_TRAN_RECORD_FIELD_VAL_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "AUDIT_TRAN_RECORD_FIELD_ID")
	private AuditTransactionRecordField recordField;
	
	public abstract AuditValue getValue();
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuditTransactionRecordField getRecordField() {
		return recordField;
	}

	public void setRecordField(AuditTransactionRecordField recordField) {
		this.recordField = recordField;
	}
}
