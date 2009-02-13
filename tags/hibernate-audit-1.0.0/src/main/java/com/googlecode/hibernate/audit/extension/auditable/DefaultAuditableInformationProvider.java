/**
 * 
 */
package com.googlecode.hibernate.audit.extension.auditable;

public class DefaultAuditableInformationProvider implements
		AuditableInformationProvider {

	private AuditableInformationProvider provider;

	public DefaultAuditableInformationProvider() {

	}

	public DefaultAuditableInformationProvider(
			AuditableInformationProvider provider) {
		this.provider = provider;
	}

	public boolean isAuditable(String entityName) {
		if (entityName.startsWith("com.googlecode.hibernate.audit.")) {
			return false;
		}

		if (provider != null) {
			return provider.isAuditable(entityName);
		}
		return true;
	}

	public boolean isAuditable(String entityName, String propertyName) {
		if (!isAuditable(entityName)) {
			return false;
		}

		if (provider != null) {
			return provider.isAuditable(entityName, propertyName);
		}
		return true;
	}

}
