package com.googlecode.hibernate.audit.model.collection;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "CHILD")
@SequenceGenerator(name = "sequence", sequenceName = "CHILD_ID_SEQ")
public class Child {
	@Id
	@Column(name = "CHILD_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;
	
	@Column(name = "CHILD_PROPERTY")
	private String childProperty;

	@ManyToOne(optional = false)
	@JoinColumn(name = "PARENT_ID")
	private Parent parent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Parent getParent() {
		return parent;
	}

	public void setParent(Parent parent) {
		this.parent = parent;
	}

	public String getChildProperty() {
		return childProperty;
	}

	public void setChildProperty(String childProperty) {
		this.childProperty = childProperty;
	}
	
	
}
