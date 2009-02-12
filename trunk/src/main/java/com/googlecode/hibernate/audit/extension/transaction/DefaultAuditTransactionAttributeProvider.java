package com.googlecode.hibernate.audit.extension.transaction;

import java.util.List;

import org.hibernate.Session;

import com.googlecode.hibernate.audit.model.AuditTransactionAttribute;

public class DefaultAuditTransactionAttributeProvider implements AuditTransactionAttributeProvider {
	
	public List<AuditTransactionAttribute> getAuditTransactionAttributes(
			Session session) {
		return null;
	}
}
