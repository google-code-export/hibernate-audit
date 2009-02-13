/**
 * 
 */
package com.googlecode.hibernate.audit.extension.syncronization;

import javax.transaction.Synchronization;

import org.hibernate.event.EventSource;

public interface TransactionSyncronization {
	void registerSynchronization(EventSource eventSource,
			Synchronization synchronization);
}
