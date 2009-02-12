package com.googlecode.hibernate.audit.extension;

import com.googlecode.hibernate.audit.extension.auditable.AuditableInformationProvider;
import com.googlecode.hibernate.audit.extension.auditable.DefaultAuditableInformationProvider;
import com.googlecode.hibernate.audit.extension.converter.DefaultPropertyValueConverter;
import com.googlecode.hibernate.audit.extension.converter.PropertyValueConverter;
import com.googlecode.hibernate.audit.extension.event.AuditLogicalGroupProvider;
import com.googlecode.hibernate.audit.extension.event.DefaultAuditLogicalGroupProvider;
import com.googlecode.hibernate.audit.extension.security.DefaultSecurityInformationProvider;
import com.googlecode.hibernate.audit.extension.security.SecurityInformationProvider;
import com.googlecode.hibernate.audit.extension.transaction.AuditTransactionAttributeProvider;
import com.googlecode.hibernate.audit.extension.transaction.DefaultAuditTransactionAttributeProvider;

public final class ExtensionManager {
	private AuditableInformationProvider auditableInformationProvider = new DefaultAuditableInformationProvider();
	private PropertyValueConverter propertyValueConverter = new DefaultPropertyValueConverter();
	private AuditLogicalGroupProvider auditLogicalGroupProvider = new DefaultAuditLogicalGroupProvider();
	private SecurityInformationProvider securityInformationProvider = new DefaultSecurityInformationProvider();
	private AuditTransactionAttributeProvider auditTransactionAttributeProvider = new DefaultAuditTransactionAttributeProvider();

	public SecurityInformationProvider getSecurityInformationProvider() {
		return securityInformationProvider;
	}

	public void setSecurityInformationProvider(
			SecurityInformationProvider securityInformationProvider) {
		this.securityInformationProvider = securityInformationProvider;
	}

	public AuditableInformationProvider getAuditableInformationProvider() {
		return auditableInformationProvider;
	}

	public void setAuditableInformationProvider(
			AuditableInformationProvider auditableInformationProvider) {
		// protect the audit framework from circular events coming from
		// modifying the audit model.
		AuditableInformationProvider newProvider = new DefaultAuditableInformationProvider(
				auditableInformationProvider);
		this.auditableInformationProvider = newProvider;
	}

	public PropertyValueConverter getPropertyValueConverter() {
		return propertyValueConverter;
	}

	public void setPropertyValueConverter(
			PropertyValueConverter propertyValueConverter) {
		this.propertyValueConverter = propertyValueConverter;
	}

	public AuditLogicalGroupProvider getAuditLogicalGroupProvider() {
		return auditLogicalGroupProvider;
	}

	public void setAuditLogicalGroupProvider(
			AuditLogicalGroupProvider auditLogicalGroupProvider) {
		this.auditLogicalGroupProvider = auditLogicalGroupProvider;
	}

	public AuditTransactionAttributeProvider getAuditTransactionAttributeProvider() {
		return auditTransactionAttributeProvider;
	}

	public void setAuditTransactionAttributeProvider(
			AuditTransactionAttributeProvider auditTransactionAttributeProvider) {
		this.auditTransactionAttributeProvider = auditTransactionAttributeProvider;
	}

}
