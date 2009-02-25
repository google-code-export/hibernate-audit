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
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.transaction.Synchronization;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.exception.ConcurrentModificationException;
import com.googlecode.hibernate.audit.exception.ObjectConcurrentModificationException;
import com.googlecode.hibernate.audit.exception.PropertyConcurrentModificationException;
import com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationLevelCheck;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditTransactionAttribute;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.object.AuditObject;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;
import com.googlecode.hibernate.audit.synchronization.work.AuditWorkUnit;

public class AuditSynchronization implements Synchronization {
    private static final Logger log = Logger.getLogger(AuditSynchronization.class);

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

            List<AuditTransactionAttribute> attributes = auditConfiguration.getExtensionManager().getAuditTransactionAttributeProvider().getAuditTransactionAttributes(session);

            if (attributes != null && !attributes.isEmpty()) {
                for (AuditTransactionAttribute attribute : attributes) {
                    attribute.setAuditTransaction(auditTransaction);
                }
                auditTransaction.getAuditTransactionAttributes().addAll(attributes);
            }

            Long loadAuditTransactionId = auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getLoadAuditTransactionId();
            if (loadAuditTransactionId != null) {
                for (AuditLogicalGroup storedAuditLogicalGroup : auditLogicalGroups) {
                    if (log.isEnabledFor(Level.DEBUG)) {
                        log.debug("lock AuditLogicalGroup with id:" + storedAuditLogicalGroup.getId());
                    }
                    session.lock(storedAuditLogicalGroup, LockMode.UPGRADE);
                }
                try {
                    concurrentModificationCheck(session, auditTransaction, loadAuditTransactionId);
                } catch (ConcurrentModificationException ce) {
                    if (log.isEnabledFor(Level.DEBUG)) {
                        log.debug(ce);
                    }
                    throw ce;
                }
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

    private void concurrentModificationCheck(Session session, AuditTransaction auditTransaciton, Long loadAuditTransactionId) {

        Map<AuditLogicalGroup, Long> auditLogicalGroupToAuditTransactionId = new IdentityHashMap<AuditLogicalGroup, Long>();

        Long latestTransaction = HibernateAudit.getLatestAuditTransactionId(session);
        if (latestTransaction != null && latestTransaction.equals(loadAuditTransactionId)) {
            // performance optimization - check the global transaction id - not
            // filtered by audit logical group
            return;
        }

        for (AuditEvent e : auditTransaciton.getEvents()) {
            AuditLogicalGroup auditLogicalGroup = e.getAuditLogicalGroup();
            if (auditLogicalGroup != null) {
                if (!auditLogicalGroupToAuditTransactionId.containsKey(auditLogicalGroup)) {
                    auditLogicalGroupToAuditTransactionId.put(auditLogicalGroup, HibernateAudit.getLatestAuditTransactionIdByAuditLogicalGroup(session, auditLogicalGroup));
                }

                Long latestLogicalGroupTransactionId = auditLogicalGroupToAuditTransactionId.get(auditLogicalGroup);

                if (latestLogicalGroupTransactionId != null && latestLogicalGroupTransactionId.equals(loadAuditTransactionId)) {
                    // performance optimization
                    continue;
                }

            }

            // NOTE: when the auditLogicalGroup is null then no DB locking is
            // performed - we do not have audit logical group to perform the
            // locking..
            concurrentModificationCheck(session, e, loadAuditTransactionId);
        }
    }

    private void concurrentModificationCheck(Session session, AuditEvent e, Long loadAuditTransactionId) {
        for (AuditObject auditObject : e.getAuditObjects()) {

            if (ConcurrentModificationLevelCheck.OBJECT.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getLevelCheck())) {
                AuditType auditType = null;
                String targetEntityId = null;

                if (auditObject instanceof EntityAuditObject) {
                    // only check the entities - the components are
                    // going to be check on field level check
                    EntityAuditObject entityAuditObject = (EntityAuditObject) auditObject;
                    auditType = entityAuditObject.getAuditType();
                    targetEntityId = entityAuditObject.getTargetEntityId();
                } else {
                    ComponentAuditObject component = (ComponentAuditObject) auditObject;
                    AuditObject entity = component.getParentAuditObject();

                    while (entity != null && entity instanceof ComponentAuditObject) {
                        entity = ((ComponentAuditObject) entity).getParentAuditObject();
                    }

                    auditType = component.getAuditType();
                    targetEntityId = ((EntityAuditObject) entity).getTargetEntityId();
                }

                Long latestEntityTransactionId = HibernateAudit.getLatestAuditTransactionIdByEntity(session, auditType, targetEntityId);
                if (latestEntityTransactionId != null && !latestEntityTransactionId.equals(loadAuditTransactionId)) {

                    if (!ConcurrentModificationLevelCheck.PROPERTY.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getLevelCheck())) {
                        // only object level should be checked
                        if (session.getTransaction().isActive()) {
                            session.getTransaction().rollback();
                        }
                        throw new ObjectConcurrentModificationException(auditType.getClassName(), auditType.getLabel(), targetEntityId);
                    } else {
                        // property level is going to be checked - validate only
                        // if the object was not deleted because in this case no
                        // properties are going to be inserted.

                        AuditTransaction latestEntityTransaction = HibernateAudit.getAuditTransaction(session, latestEntityTransactionId);
                        if (latestEntityTransaction != null) {
                            for (AuditEvent event : latestEntityTransaction.getEvents()) {
                                if (event.getEntityId() != null && event.getEntityId().equals(targetEntityId) && event.getAuditType().getClassName().equals(auditType.getClassName())) {
                                    if (AuditEvent.DELETE_AUDIT_EVENT_TYPE.equals(event.getType())) {
                                        if (session.getTransaction().isActive()) {
                                            session.getTransaction().rollback();
                                        }
                                        throw new ObjectConcurrentModificationException(auditType.getClassName(), auditType.getLabel(), targetEntityId);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (ConcurrentModificationLevelCheck.PROPERTY.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getLevelCheck())) {

                // find if there are fields that were modified
                for (AuditObjectProperty auditObjectProperty : auditObject.getAuditObjectProperties()) {

                    Long latestEntityTransactionId = HibernateAudit.getLatestAuditTransactionIdByProperty(session, auditObjectProperty.getAuditField(), e.getEntityId());

                    if (latestEntityTransactionId != null && !latestEntityTransactionId.equals(loadAuditTransactionId)) {

                        // object level check and we detected that we have
                        // changed object that matches..
                        if (session.getTransaction().isActive()) {
                            session.getTransaction().rollback();
                        }
                        throw new PropertyConcurrentModificationException(auditObjectProperty.getAuditField().getOwnerType().getClassName(), auditObjectProperty.getAuditField().getName(),
                                auditObjectProperty.getAuditField().getOwnerType().getLabel(), auditObjectProperty.getAuditField().getLabel(), e.getEntityId());
                    }
                }
            }
        }
    }
}