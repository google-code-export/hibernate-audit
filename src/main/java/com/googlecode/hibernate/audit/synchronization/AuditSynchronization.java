/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.synchronization;

import java.security.Principal;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.Synchronization;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditTransactionAttribute;
import com.googlecode.hibernate.audit.synchronization.work.AuditWorkUnit;

public class AuditSynchronization implements Synchronization {
	private final AuditSynchronizationManager manager;
	private final Session auditedSession;
	private final Transaction transaction;
	private LinkedList<AuditWorkUnit> workUnits = new LinkedList<AuditWorkUnit>();
	private AuditConfiguration auditConfiguration;
	private static final Logger log = Logger
			.getLogger(AuditSynchronization.class);

	public AuditSynchronization(AuditSynchronizationManager manager,
			Session session) {
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
		executeInSession(auditedSession);
	}

	public void afterCompletion(int arg0) {
		manager.remove(transaction);
	}

	private void executeInSession(Session session) {
		if (log.isEnabledFor(Level.DEBUG)) {
			log.debug("executeInSession begin");
		}

		try {
			AuditWorkUnit workUnit;
			SortedSet<AuditLogicalGroup> auditLogicalGroups = new TreeSet<AuditLogicalGroup>(new Comparator<AuditLogicalGroup>() {
				// sort audit logical groups in order to minimize database dead lock conditions. 
				public int compare(AuditLogicalGroup o1, AuditLogicalGroup o2) {
					// note that both entities should already be persistent so they must have ids
					return o1.getId().compareTo(o2.getId());
				};
			});
			
			AuditTransaction auditTransaction = new AuditTransaction();
			auditTransaction.setTimestamp(new Date());
			Principal principal = auditConfiguration.getExtensionManager()
					.getSecurityInformationProvider().getPrincipal();
			auditTransaction.setUsername(principal == null ? null : principal
					.getName());

			if (log.isEnabledFor(Level.DEBUG)) {
				log.debug("start workUnits perform");
			}
			while ((workUnit = workUnits.poll()) != null) {
				workUnit.perform(session, auditConfiguration, auditTransaction);
				auditLogicalGroups.addAll(workUnit.getAuditLogicalGroups());
			}
			if (log.isEnabledFor(Level.DEBUG)) {
				log.debug("end workUnits perform");
			}

			List<AuditTransactionAttribute> attributes = auditConfiguration
					.getExtensionManager()
					.getAuditTransactionAttributeProvider()
					.getAuditTransactionAttributes(session);

			if (attributes != null && !attributes.isEmpty()) {
				for (AuditTransactionAttribute attribute : attributes) {
					attribute.setAuditTransaction(auditTransaction);
				}
				auditTransaction.getAuditTransactionAttributes().addAll(
						attributes);
			}

			if (auditConfiguration.isConcurrentModificationCheckEnabled()) {
				for (AuditLogicalGroup storedAuditLogicalGroup : auditLogicalGroups) {
					if (log.isEnabledFor(Level.DEBUG)) {
						log.debug("lock AuditLogicalGroup with id:"
								+ storedAuditLogicalGroup.getId());
					}
					session.lock(storedAuditLogicalGroup, LockMode.UPGRADE);
				}
				// TODO: Add concurrent modification check..
			}
			
			session.save(auditTransaction);

			if (!FlushMode.isManualFlushMode(session.getFlushMode())) {
				session.flush();
			}
		} finally {
			if (log.isEnabledFor(Level.DEBUG)) {
				log.debug("executeInSession end");
			}
		}
	}
}