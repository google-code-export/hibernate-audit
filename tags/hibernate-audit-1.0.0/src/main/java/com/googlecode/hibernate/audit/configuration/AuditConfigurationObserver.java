package com.googlecode.hibernate.audit.configuration;


public interface AuditConfigurationObserver {

	void auditConfigurationCreated(AuditConfiguration auditConfiguration);
}
