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
package com.googlecode.hibernate.audit.extension.syncronization;

import javax.transaction.Synchronization;

import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.hibernate.event.spi.EventSource;

public class DefaultTransactionSyncronization implements TransactionSyncronization {

	public void registerSynchronization(EventSource eventSource, Synchronization synchronization) {
		Transaction transaction = eventSource.getTransaction();
		checkForActiveTransaction(eventSource);

		transaction.registerSynchronization(synchronization);
	}

	private void checkForActiveTransaction(EventSource eventSource) {
		if (!eventSource.isTransactionInProgress()) {
			throw new HibernateException("No active transaction.");

		}
	}
}
