package com.googlecode.hibernate.audit.synchronization;

import java.util.Map;

import org.hibernate.Transaction;
import org.hibernate.event.EventSource;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.util.ConcurrentReferenceHashMap;

public final class AuditSynchronizationManager {
	private final Map<Transaction, AuditSynchronization> syncronizations = new ConcurrentReferenceHashMap<Transaction, AuditSynchronization>(
			16, ConcurrentReferenceHashMap.ReferenceType.WEAK,
			ConcurrentReferenceHashMap.ReferenceType.STRONG);
	
	private AuditConfiguration auditConfiguration;

	public AuditSynchronizationManager(AuditConfiguration auditConfiguration) {
		this.auditConfiguration = auditConfiguration;
	}

	public AuditSynchronization get(EventSource session) {
		Transaction transaction = session.getTransaction();

		AuditSynchronization synchronization = syncronizations.get(transaction);
		if (synchronization == null) {
			synchronization = new AuditSynchronization(this, session);
			syncronizations.put(transaction, synchronization);

			transaction.registerSynchronization(synchronization);
		}

		return synchronization;
	}

	public void remove(Transaction transaction) {
		syncronizations.remove(transaction);
	}

	public AuditConfiguration getAuditConfiguration() {
		return auditConfiguration;
	}
}
