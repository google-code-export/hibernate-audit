package com.googlecode.hibernate.audit.model.clazz;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Entity
@Table(name = "AUDIT_CLASS_PROPERTY",
       uniqueConstraints = @UniqueConstraint(columnNames = { "AUDIT_CLASS_ID", "NAME" }))
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_CLASS_PROP_ID_SEQ")
public class AuditClassProperty {

	@Id
	@Column(name = "AUDIT_PROPERTY_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.AUTO)
	private Long id;

	/** the property name. */
	@Column(name = "NAME")
	private String name;

	/** the property class name. */
	@ManyToOne
	@JoinColumn(name = "AUDIT_CLASS_ID")
	private AuditClass auditClass;

	public AuditClassProperty() {
	}

	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name
	 *            the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the auditClass
	 */
	public AuditClass getAuditClass() {
		return auditClass;
	}

	/**
	 * @param auditClass
	 *            the auditClass to set
	 */
	public void setAuditClass(AuditClass auditClass) {
		this.auditClass = auditClass;
	}
}
