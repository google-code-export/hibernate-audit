package com.googlecode.hibernate.audit.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "AUDIT_CLASS")
@SequenceGenerator(name = "sequence", sequenceName = "AUDIT_CLASS_ID_SEQ")
public class AuditClass {
	/** the id. */
	@Id
	@Column(name = "AUDIT_CLASS_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	/** the entity class name. */
	@Column(name = "NAME", unique=true)
	private String name;

	public AuditClass() {
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
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
}
