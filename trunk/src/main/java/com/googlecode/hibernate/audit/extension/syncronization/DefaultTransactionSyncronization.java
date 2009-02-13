package com.googlecode.hibernate.audit.extension.syncronization;

import javax.transaction.Synchronization;

import org.hibernate.Transaction;
import org.hibernate.event.EventSource;

public class DefaultTransactionSyncronization implements
		TransactionSyncronization {
	
	public void registerSynchronization(EventSource eventSource,
			Synchronization synchronization) {
		Transaction transaction = eventSource.getTransaction();
		
	}
}
