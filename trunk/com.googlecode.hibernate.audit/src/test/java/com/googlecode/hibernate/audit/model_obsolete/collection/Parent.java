package com.googlecode.hibernate.audit.model_obsolete.collection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "PARENT")
@SequenceGenerator(name = "sequence", sequenceName = "PARENT_ID_SEQ")
public class Parent {
	@Id
	@Column(name = "PARENT_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;

	@Column(name = "PARENT_PROPERTY")
	private String parentProperty;
	
	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
	private List<Child> children = new ArrayList<Child>();

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getParentProperty() {
		return parentProperty;
	}

	public void setParentProperty(String parentProperty) {
		this.parentProperty = parentProperty;
	}

	public List<Child> getChildren() {
		return Collections.unmodifiableList(children);
	}

	public void addChild(Child child) {
		child.setParent(this);
		children.add(child);
	}
	
	
}
