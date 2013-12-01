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

import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.Synchronization;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.util.JTAHelper;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditTransactionAttribute;
import com.googlecode.hibernate.audit.synchronization.work.AuditWorkUnit;

public class AuditSynchronization implements Synchronization {
	private static final Logger log = LoggerFactory.getLogger(AuditSynchronization.class);

	private final AuditSynchronizationManager manager;
	private final Session auditedSession;
	private final Transaction transaction;
	private LinkedList<AuditWorkUnit> workUnits = new LinkedList<AuditWorkUnit>();
	private AuditConfiguration auditConfiguration;

	public AuditSynchronization(AuditSynchronizationManager manager, Session session) {
		this.manager = manager;
		this.auditedSession = session;
		this.transaction = session.getTransaction();
		this.auditConfiguration = manager.getAuditConfiguration();
	}

	public void addWorkUnit(AuditWorkUnit workUnit) {
		workUnit.init(auditedSession, auditConfiguration);
		workUnits.add(workUnit);
	}

	public void beforeCompletion() {
		if (workUnits.size() == 0) {
			return;
		}
		if (!isMarkedForRollback(auditedSession)) {
			try {
				if (!FlushMode.isManualFlushMode(auditedSession.getFlushMode())) {
					auditedSession.flush();
				}
				executeInSession(auditedSession);
			} catch (RuntimeException e) {
				if (log.isErrorEnabled()) {
					log.error("RuntimeException occurred in beforeCompletion, will rollback and re-throw exception", e);
				}
				rollback();
				throw e;
			}
		}
	}

	private boolean isMarkedForRollback(Session session) {
		TransactionManager manager = ((SessionFactoryImplementor) session.getSessionFactory()).getTransactionManager();
		if (manager != null) {
			try {
				if (JTAHelper.isRollback(manager.getStatus())) {
					return true;
				}
			} catch (SystemException e) {
				throw new TransactionException("Unable to get the transaction status.", e);
			}
		} else {
			return session.getTransaction().wasRolledBack();
		}

		return false;
	}

	private void rollback() {
		try {
			if (auditedSession != null && auditedSession.getTransaction() != null && auditedSession.getTransaction().isActive()) {
				auditedSession.getTransaction().rollback();
			} else if (auditedSession != null && ((SessionFactoryImplementor) auditedSession.getSessionFactory()).getTransactionManager() != null) {
				((SessionFactoryImplementor) auditedSession.getSessionFactory()).getTransactionManager().setRollbackOnly();
			}
		} catch (Exception se) {
			if (log.isWarnEnabled()) {
				// this is the best that we can do - we've tried to mark
				// the transaction as rolled back but we failed - the
				// only thing left if to log the exception
				log.warn("Exception occured during rollback, only logging the exception", se);
			}
		}
	}

	public void afterCompletion(int arg0) {
		manager.remove(transaction);
	}

	private void executeInSession(Session session) {
		if (log.isDebugEnabled()) {
			log.debug("executeInSession begin");
		}

		try {
			AuditWorkUnit workUnit;
			SortedSet<AuditLogicalGroup> auditLogicalGroups = new TreeSet<AuditLogicalGroup>(new Comparator<AuditLogicalGroup>() {
				// sort audit logical groups in order to minimize
				// database dead lock conditions.
				public int compare(AuditLogicalGroup o1, AuditLogicalGroup o2) {
					// note that both entities should already be
					// persistent so they must have ids
					return o1.getId().compareTo(o2.getId());
				};
			});

			AuditTransaction auditTransaction = new AuditTransaction();
			auditTransaction.setTimestamp(new Date());
			Principal principal = auditConfiguration.getExtensionManager().getSecurityInformationProvider().getPrincipal();
			auditTransaction.setUsername(principal == null ? null : principal.getName());

			if (log.isDebugEnabled()) {
				log.debug("start workUnits perform");
			}
			while ((workUnit = workUnits.poll()) != null) {
				workUnit.perform(session, auditConfiguration, auditTransaction);
				auditLogicalGroups.addAll(workUnit.getAuditLogicalGroups());
			}
			if (log.isDebugEnabled()) {
				log.debug("end workUnits perform");
			}

			List<AuditTransactionAttribute> attributes = auditConfiguration.getExtensionManager().getAuditTransactionAttributeProvider().getAuditTransactionAttributes(session);

			if (attributes != null && !attributes.isEmpty()) {
				for (AuditTransactionAttribute attribute : attributes) {
					attribute.setAuditTransaction(auditTransaction);
				}
				auditTransaction.getAuditTransactionAttributes().addAll(attributes);
			}

			concurrencyModificationCheck(session, auditLogicalGroups, auditTransaction);

			session.save(auditTransaction);
			for (AuditLogicalGroup storedAuditLogicalGroup : auditLogicalGroups) {
				storedAuditLogicalGroup.setLastUpdatedAuditTransactionId(auditTransaction.getId());
			}

			if (!FlushMode.isManualFlushMode(session.getFlushMode())) {
				session.flush();
			}
		} finally {
			if (log.isDebugEnabled()) {
				log.debug("executeInSession end");
			}
		}
	}

	private void concurrencyModificationCheck(Session session, SortedSet<AuditLogicalGroup> auditLogicalGroups,
			AuditTransaction auditTransaction) {
		Long loadAuditTransactionId = auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getLoadAuditTransactionId();
		auditConfiguration.getExtensionManager().getConcurrentModificationCheckProvider().concurrentModificationCheck(auditConfiguration, session, auditLogicalGroups, auditTransaction, loadAuditTransactionId);
	}


}