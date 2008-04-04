package com.googlecode.hibernate.audit.model.simpleentity;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SimpleEmbeddedComponent {
	@Column(name = "FIELD1")
	private String field1;

	@Column(name = "FIELD2")
	private String field2;

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

	
}
