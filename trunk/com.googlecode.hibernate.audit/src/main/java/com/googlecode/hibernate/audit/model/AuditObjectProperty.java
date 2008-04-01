package com.googlecode.hibernate.audit.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "AUDIT_OBJECT_PROPERTY")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "PROP_TYPE")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_OBJECT_PROPERTY_ID_SEQ")
public abstract class AuditObjectProperty {
	/** the id. */
	@Id
	@Column(name = "AUDIT_OBJECT_PROPERTY_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column(name = "OPERATION")
	@Enumerated(value = EnumType.STRING)
	private AuditOperation operation;

	@ManyToOne
	@JoinColumn(name = "AUDIT_CLASS_PROPERTY_ID")
	private AuditClassProperty auditClassProperty;
	
	@ManyToOne
	@JoinColumn(name = "AUDIT_OBJECT_ID")
	private AuditObject auditObject;

	@OneToMany(mappedBy = "auditObjectProperty")
	protected Collection<AuditObjectPropertyValue> values = new ArrayList<AuditObjectPropertyValue>();

	public Collection<AuditObjectPropertyValue> getValues() {
		return Collections.unmodifiableCollection(values);
	}

	public AuditObject getAuditObject() {
		return auditObject;
	}

	void setAuditObject(AuditObject auditObject) {
		this.auditObject = auditObject;
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
}
