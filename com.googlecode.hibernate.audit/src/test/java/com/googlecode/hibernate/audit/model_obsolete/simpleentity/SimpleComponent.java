package com.googlecode.hibernate.audit.model_obsolete.simpleentity;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Embedded;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;

@Embeddable
public class SimpleComponent {
	@Column(name = "FIELD1")
	private String field1;

	@Column(name = "FIELD2")
	private String field2;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name="COMP_REL_SIMPLE_ENTITY_ID")
	private SimpleEntity componentRelatedSimpleEntity;

	@Embedded
	@AttributeOverrides({
        @AttributeOverride(name="field1", column=@Column(name = "FIELD1_INNER")),
        @AttributeOverride(name="field2", column=@Column(name = "FIELD2_INNER"))})
	private SimpleEmbeddedComponent inner;
	
	public String getField1() {
		return field1;
	}
	
	public void setField1(String field1) {
		this.field1 = field1;
	}
	
	public String getField2() {
		return field2;
	}
	
	public void setField2(String field2) {
		this.field2 = field2;
	}
	
	public SimpleEmbeddedComponent getInner() {
		return inner;
	}
	
	public void setInner(SimpleEmbeddedComponent inner) {
		this.inner = inner;
	}

	public SimpleEntity getComponentRelatedSimpleEntity() {
		return componentRelatedSimpleEntity;
	}

	public void setComponentRelatedSimpleEntity(
			SimpleEntity componentRelatedSimpleEntity) {
		this.componentRelatedSimpleEntity = componentRelatedSimpleEntity;
	}
}
