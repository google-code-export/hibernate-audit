package com.googlecode.hibernate.audit.extension.auditable;

public interface AuditableInformationProvider {

	boolean isAuditable(String entityName);

	boolean isAuditable(String entityName, String propertyName);
}
