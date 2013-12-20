package com.googlecode.hibernate.audit.extension.concurrent;

import java.util.SortedSet;

import org.hibernate.Session;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;

public interface ConcurrentModificationCheckProvider {

	void concurrentModificationCheck(AuditConfiguration auditConfiguration, Session session,
			SortedSet<AuditLogicalGroup> auditLogicalGroups, AuditTransaction auditTransaction,
			Long loadAuditTransactionId);
}
