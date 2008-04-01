package com.googlecode.hibernate.audit.test.model.simpleentity;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embedded;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@javax.persistence.Entity
@Table(name = "SIMPLE_ENTITY")
@SequenceGenerator(name = "sequence", sequenceName = "SIMPLE_ENTITY_ID_SEQ")
public class SimpleEntity {
	@Id
	@Column(name = "SIMPLE_ENTITY_ID")
	@GeneratedValue(generator = "sequence", strategy = GenerationType.SEQUENCE)
	private Long id;
	
	@Column(name = "string")
	private String string;
	
	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="RELATED_SIMPLE_ENTITY_ID")
	private SimpleEntity relatedSimpleEntity;
	
	@Embedded
	private SimpleComponent innerComponent;
	
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getString() {
		return string;
	}

	public void setString(String string) {
		this.string = string;
	}

	public SimpleEntity getRelatedSimpleEntity() {
		return relatedSimpleEntity;
	}

	public void setRelatedSimpleEntity(SimpleEntity relatedSimpleEntity) {
		this.relatedSimpleEntity = relatedSimpleEntity;
	}

	public SimpleComponent getInnerComponent() {
		return innerComponent;
	}

	public void setInnerComponent(SimpleComponent innerComponent) {
		this.innerComponent = innerComponent;
	}
	
	
}
