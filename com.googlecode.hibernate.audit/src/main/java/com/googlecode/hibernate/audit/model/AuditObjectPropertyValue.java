package com.googlecode.hibernate.audit.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "AUDIT_OBJECT_PROPERTY_VAL")
@Inheritance(strategy = InheritanceType.JOINED)
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_OBJECT_PROP_VAL_ID_SEQ")
public abstract class AuditObjectPropertyValue {
	/** the id. */
	@Id
	@Column(name = "AUDIT_OBJECT_PROP_VAL_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	protected Long id;

	@ManyToOne
	@JoinColumn(name = "AUDIT_OBJECT_PROPERTY_ID")
	private AuditObjectProperty auditObjectProperty;

	@Column(name = "KEY")
	private String key;

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public AuditObjectProperty getAuditObjectProperty() {
		return auditObjectProperty;
	}

	void setAuditObjectProperty(AuditObjectProperty auditObjectProperty) {
		this.auditObjectProperty = auditObjectProperty;
	}
}
