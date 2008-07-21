package com.googlecode.hibernate.audit.model_obsolete.transaction.record.field;

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
import javax.persistence.Entity;
import javax.persistence.Table;

import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.AuditValue;

@Entity
@Table(name = "AUDIT_TRAN_RECORD_FIELD_VALUE")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "VALUE_TYPE")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_TRAN_REC_FLD_VAL_ID_SEQ")
public abstract class AuditTransactionRecordFieldValue {

	@Id
	@Column(name = "AUDIT_TRAN_RECORD_FIELD_VAL_ID")
    @GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
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
