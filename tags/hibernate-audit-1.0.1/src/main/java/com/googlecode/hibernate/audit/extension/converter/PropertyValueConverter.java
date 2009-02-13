package com.googlecode.hibernate.audit.extension.converter;

public interface PropertyValueConverter {
	String toString(Object obj);

	Object valueOf(Class type, String str);
}
