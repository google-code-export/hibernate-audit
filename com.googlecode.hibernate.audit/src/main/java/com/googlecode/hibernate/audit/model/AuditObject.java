package com.googlecode.hibernate.audit.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "AUDIT_OBJECT")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_OBJECT_ID_SEQ")
public class AuditObject {
	/** the id. */
	@Id
	@Column(name = "AUDIT_OBJECT_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	/** the transaction id. */
	@ManyToOne
	@JoinColumn(name = "AUDIT_TRANSACTION_ID")
	private AuditTransaction auditTransaction;

	@Column(name = "OPERATION")
	@Enumerated(value = EnumType.STRING)
	private AuditOperation operation;

	/** the property class name. */
	@ManyToOne
	@JoinColumn(name = "AUDIT_CLASS_ID")
	private AuditClass auditClass;

	@Column(name = "AUDITTED_ENTITY_ID")
	private String audittedEntityId;

	@OneToMany(mappedBy = "auditObject")
	private Set<AuditObjectProperty> auditProperties = new HashSet<AuditObjectProperty>(); 

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

	public String getAudittedEntityId() {
		return audittedEntityId;
	}

	public void setAudittedEntityId(String audittedEntityId) {
		this.audittedEntityId = audittedEntityId;
	}

	public void addAuditObjectProperty(AuditObjectProperty property) {
		property.setAuditObject(this);
		auditProperties.add(property);
	}
	
	public Set<AuditObjectProperty> getAuditProperties() {
		return Collections.unmodifiableSet(auditProperties);
	}

	/**
	 * @return the auditTransaction
	 */
	public AuditTransaction getAuditTransaction() {
		return auditTransaction;
	}

	/**
	 * @param auditTransaction the auditTransaction to set
	 */
	public void setAuditTransaction(AuditTransaction auditTransaction) {
		this.auditTransaction = auditTransaction;
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

	/**
	 * @return the auditClass
	 */
	public AuditClass getAuditClass() {
		return auditClass;
	}

	/**
	 * @param auditClass the auditClass to set
	 */
	public void setAuditClass(AuditClass auditClass) {
		this.auditClass = auditClass;
	}
}
