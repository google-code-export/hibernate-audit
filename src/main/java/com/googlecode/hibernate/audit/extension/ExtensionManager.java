/**
 * Copyright (C) 2009 Krasimir Chobantonov <kchobantonov@yahoo.com>
 * This file is part of Hibernate Audit.

 * Hibernate Audit is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Hibernate Audit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with Hibernate Audit.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.googlecode.hibernate.audit.extension;

import com.googlecode.hibernate.audit.extension.auditable.AuditableInformationProvider;
import com.googlecode.hibernate.audit.extension.auditable.DefaultAuditableInformationProvider;
import com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationCheckProvider;
import com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationProvider;
import com.googlecode.hibernate.audit.extension.concurrent.DefaultConcurrentModificationCheckProvider;
import com.googlecode.hibernate.audit.extension.concurrent.DefaultConcurrentModificationProvider;
import com.googlecode.hibernate.audit.extension.converter.DefaultPropertyValueConverter;
import com.googlecode.hibernate.audit.extension.converter.PropertyValueConverter;
import com.googlecode.hibernate.audit.extension.event.AuditLogicalGroupProvider;
import com.googlecode.hibernate.audit.extension.event.DefaultAuditLogicalGroupProvider;
import com.googlecode.hibernate.audit.extension.security.DefaultSecurityInformationProvider;
import com.googlecode.hibernate.audit.extension.security.SecurityInformationProvider;
import com.googlecode.hibernate.audit.extension.syncronization.DefaultTransactionSyncronization;
import com.googlecode.hibernate.audit.extension.syncronization.TransactionSyncronization;
import com.googlecode.hibernate.audit.extension.transaction.AuditTransactionAttributeProvider;
import com.googlecode.hibernate.audit.extension.transaction.DefaultAuditTransactionAttributeProvider;

public final class ExtensionManager {
    private AuditableInformationProvider auditableInformationProvider = new DefaultAuditableInformationProvider();
    private PropertyValueConverter propertyValueConverter = new DefaultPropertyValueConverter();
    private AuditLogicalGroupProvider auditLogicalGroupProvider = new DefaultAuditLogicalGroupProvider();
    private SecurityInformationProvider securityInformationProvider = new DefaultSecurityInformationProvider();
    private AuditTransactionAttributeProvider auditTransactionAttributeProvider = new DefaultAuditTransactionAttributeProvider();
    private TransactionSyncronization transactionSyncronization = new DefaultTransactionSyncronization();
    private ConcurrentModificationProvider concurrentModificationProvider = new DefaultConcurrentModificationProvider();
    private ConcurrentModificationCheckProvider concurrentModificationCheckProvider = new DefaultConcurrentModificationCheckProvider();

    public SecurityInformationProvider getSecurityInformationProvider() {
        return securityInformationProvider;
    }

    public void setSecurityInformationProvider(SecurityInformationProvider securityInformationProvider) {
        this.securityInformationProvider = securityInformationProvider;
    }

    public AuditableInformationProvider getAuditableInformationProvider() {
        return auditableInformationProvider;
    }

    public void setAuditableInformationProvider(AuditableInformationProvider auditableInformationProvider) {
        // protect the audit framework from circular events coming from
        // modifying the audit model.
        AuditableInformationProvider newProvider = new DefaultAuditableInformationProvider(auditableInformationProvider);
        this.auditableInformationProvider = newProvider;
    }

    public PropertyValueConverter getPropertyValueConverter() {
        return propertyValueConverter;
    }

    public void setPropertyValueConverter(PropertyValueConverter propertyValueConverter) {
        this.propertyValueConverter = propertyValueConverter;
    }

    public AuditLogicalGroupProvider getAuditLogicalGroupProvider() {
        return auditLogicalGroupProvider;
    }

    public void setAuditLogicalGroupProvider(AuditLogicalGroupProvider auditLogicalGroupProvider) {
        this.auditLogicalGroupProvider = auditLogicalGroupProvider;
    }

    public AuditTransactionAttributeProvider getAuditTransactionAttributeProvider() {
        return auditTransactionAttributeProvider;
    }

    public void setAuditTransactionAttributeProvider(AuditTransactionAttributeProvider auditTransactionAttributeProvider) {
        this.auditTransactionAttributeProvider = auditTransactionAttributeProvider;
    }

    public TransactionSyncronization getTransactionSyncronization() {
        return transactionSyncronization;
    }

    public void setTransactionSyncronization(TransactionSyncronization transactionSyncronization) {
        this.transactionSyncronization = transactionSyncronization;
    }

    public ConcurrentModificationProvider getConcurrentModificationProvider() {
        return concurrentModificationProvider;
    }

    public void setConcurrentModificationProvider(ConcurrentModificationProvider concurrentModificationProvider) {
        this.concurrentModificationProvider = concurrentModificationProvider;
    }
    
    public ConcurrentModificationCheckProvider getConcurrentModificationCheckProvider() {
		return concurrentModificationCheckProvider;
	}
    
    public void setConcurrentModificationCheckProvider(
			ConcurrentModificationCheckProvider concurrentModificationCheckProvider) {
		this.concurrentModificationCheckProvider = concurrentModificationCheckProvider;
	}
}
