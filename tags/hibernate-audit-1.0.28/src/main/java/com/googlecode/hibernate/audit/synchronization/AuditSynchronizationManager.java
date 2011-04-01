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
package com.googlecode.hibernate.audit.synchronization;

import java.util.Map;

import org.hibernate.Transaction;
import org.hibernate.event.EventSource;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.util.ConcurrentReferenceHashMap;

public final class AuditSynchronizationManager {
    private final Map<Transaction, AuditSynchronization> syncronizations = new ConcurrentReferenceHashMap<Transaction, AuditSynchronization>(16, ConcurrentReferenceHashMap.ReferenceType.WEAK,
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

            auditConfiguration.getExtensionManager().getTransactionSyncronization().registerSynchronization(session, synchronization);
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
