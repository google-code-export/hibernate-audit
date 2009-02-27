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
package com.googlecode.hibernate.audit;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.metadata.ClassMetadata;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.listener.AuditSessionFactoryObserver;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;

public final class HibernateAudit {
    private static final Logger log = Logger.getLogger(HibernateAudit.class);

    public static final String AUDIT_CONFIGURATION_OBSERVER_PROPERTY = "hba.configuration.observer.clazz";
    public static final String AUDIT_SET_DYNAMIC_UPDATE_FOR_AUDITED_MODEL_PROPERTY = "hba.audited-model.dynamic-update";

    private static final String SELECT_AUDIT_LOCAL_GROUP_BY_AUDIT_TYPE_AND_EXTERNAL_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAuditLogicalGroup";
    private static final String SELECT_AUDIT_TYPE_BY_CLASS_NAME = "com.googlecode.hibernate.audit.HibernateAudit.getAuditType";
    private static final String SELECT_AUDIT_TYPE_FIELD_BY_CLASS_NAME_AND_PROPERTY_NAME = "com.googlecode.hibernate.audit.HibernateAudit.getAuditField";

    private static final String SELECT_LATEST_AUDIT_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionId";
    private static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_AUDIT_LOGICAL_GROUP = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByAuditLogicalGroup";
    private static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_ENTITY = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByEntity";
    private static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_PROPERTY = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByProperty";

    private static final String SELECT_AUDIT_TRANSACTION_BY_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAuditTransaction";
    private static final String SELECT_ALL_AUDIT_TRANSACTIONS_AFTER_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditTransactionsAfterTransactionId";

    private static final String SELECT_AUDIT_TRANSACTIONS_FOR_ENTITY = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditTransactionsForEntity";
    private static final String SELECT_AUDIT_EVENTS_FOR_ENTITY = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditEventsForEntity";
    private static final String SELECT_AUDIT_EVENTS_FOR_ENTITY_UNTIL_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditEventsForEntityUntilTransactionId";

    public static final String AUDIT_META_DATA_QUERY_CACHE_REGION = "com.googlecode.hibernate.audit.model.query";

    private HibernateAudit() {
    }

    public static Long getLatestAuditTransactionId(Session session) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID);
        Long auditTransactionId = (Long) query.uniqueResult();
        return auditTransactionId;

    }

    public static Long getLatestAuditTransactionIdByAuditLogicalGroup(Session session, AuditLogicalGroup auditLogicalGroup) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_AUDIT_LOGICAL_GROUP);
        query.setParameter("auditLogicalGroup", auditLogicalGroup);
        Long auditTransactionId = (Long) query.uniqueResult();
        return auditTransactionId;
    }

    public static Long getLatestAuditTransactionIdByEntity(Session session, AuditType auditType, String targetEntityId) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_ENTITY);
        query.setParameter("auditType", auditType);
        query.setParameter("targetEntityId", targetEntityId);
        Long auditTransactionId = (Long) query.uniqueResult();
        return auditTransactionId;
    }

    public static Long getLatestAuditTransactionIdByProperty(Session session, AuditTypeField auditTypeField, String targetEntityId) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_PROPERTY);
        query.setParameter("auditTypeField", auditTypeField);
        query.setParameter("targetEntityId", targetEntityId);
        Long auditTransactionId = (Long) query.uniqueResult();
        return auditTransactionId;
    }

    public static AuditTransaction getAuditTransaction(Session session, Long transactionId) {
        Query query = session.getNamedQuery(SELECT_AUDIT_TRANSACTION_BY_TRANSACTION_ID);

        query.setParameter("transactionId", transactionId);
        AuditTransaction auditTransaction = (AuditTransaction) query.uniqueResult();
        return auditTransaction;
    }

    public static List<AuditTransaction> getAllAuditTransactionsAfterTransactionId(Session session, Long transactionId) {
        Query query = session.getNamedQuery(SELECT_ALL_AUDIT_TRANSACTIONS_AFTER_TRANSACTION_ID);

        query.setParameter("transactionId", transactionId);
        List<AuditTransaction> auditTransactions = (List<AuditTransaction>) query.list();
        return auditTransactions;
    }

    /**
     * Return all audit transactions that have the entity modified in any way.
     * The result is sorted in reverse, e.g. the most recent transactions first.
     * 
     * @param session
     * @param auditType
     * @param externalId
     * @return
     */
    public static List<AuditTransaction> getAllAuditTransactionsForEntity(Session session, AuditType auditType, String externalId) {
        Query query = session.getNamedQuery(SELECT_AUDIT_TRANSACTIONS_FOR_ENTITY);
        query.setParameter("auditType", auditType);
        query.setParameter("externalId", externalId);
        List<AuditTransaction> auditTransactions = (List<AuditTransaction>) query.list();
        return auditTransactions;
    }

    /**
     * Return all audit events that have the entity modified in any way. The
     * result is sorted in asc order, e.g. the oldest entries first.
     * 
     * @param session
     * @param auditType
     * @param externalId
     * @return
     */
    public static List<AuditEvent> getAllAuditEventsForEntity(Session session, AuditType auditType, String externalId) {
        Query query = session.getNamedQuery(SELECT_AUDIT_EVENTS_FOR_ENTITY);
        query.setParameter("auditType", auditType);
        query.setParameter("externalId", externalId);
        List<AuditEvent> auditEvents = (List<AuditEvent>) query.list();
        return auditEvents;
    }

    /**
     * Return all audit events that have the entity modified in any way until
     * specified transactionId (The transactionId is included). The result is
     * sorted in asc order, e.g. the oldest entries first.
     * 
     * @param session
     * @param auditType
     * @param externalId
     * @return
     */
    public static List<AuditEvent> getAllAuditEventsForEntityUntilTransactionId(Session session, AuditType auditType, String externalId, Long transactionId) {
        Query query = session.getNamedQuery(SELECT_AUDIT_EVENTS_FOR_ENTITY_UNTIL_TRANSACTION_ID);
        query.setParameter("auditType", auditType);
        query.setParameter("externalId", externalId);
        query.setParameter("transactionId", transactionId);
        List<AuditEvent> auditEvents = (List<AuditEvent>) query.list();
        return auditEvents;
    }

    public static AuditLogicalGroup getAuditLogicalGroup(Session session, AuditType auditType, String externalId) {
        Query query = session.getNamedQuery(SELECT_AUDIT_LOCAL_GROUP_BY_AUDIT_TYPE_AND_EXTERNAL_ID);

        query.setParameter("auditType", auditType);
        query.setParameter("externalId", externalId);
        AuditLogicalGroup storedAuditLogicalGroup = (AuditLogicalGroup) query.uniqueResult();
        return storedAuditLogicalGroup;
    }

    public static AuditType getAuditType(Session session, String className) {
        Query query = session.getNamedQuery(SELECT_AUDIT_TYPE_BY_CLASS_NAME);
        query.setParameter("className", className);

        query.setCacheable(true);
        query.setCacheRegion(AUDIT_META_DATA_QUERY_CACHE_REGION);

        AuditType auditType = (AuditType) query.uniqueResult();
        return auditType;
    }

    public static AuditTypeField getAuditField(Session session, String className, String propertyName) {
        Query query = session.getNamedQuery(SELECT_AUDIT_TYPE_FIELD_BY_CLASS_NAME_AND_PROPERTY_NAME);

        query.setParameter("className", className);
        query.setParameter("name", propertyName);

        query.setCacheable(true);
        query.setCacheRegion(AUDIT_META_DATA_QUERY_CACHE_REGION);

        AuditTypeField auditField = (AuditTypeField) query.uniqueResult();
        return auditField;
    }

    public static String getEntityName(AuditConfiguration configuration, Session session, String implementationClass) {
        Collection<ClassMetadata> allClassMetadata = session.getSessionFactory().getAllClassMetadata().values();
        for (ClassMetadata classMetadata : allClassMetadata) {
            String entityName = classMetadata.getEntityName();
            PersistentClass classMapping = configuration.getAuditedConfiguration().getClassMapping(entityName);
            Class mappedClass = classMapping.getMappedClass();
            if (mappedClass.getName().equals(implementationClass)) {
                return entityName;
            }
        }
        return implementationClass;
    }

    public static AuditConfiguration getAuditConfiguration(Session session) {
        AuditConfiguration auditConfiguration = AuditSessionFactoryObserver.getAuditConfiguration(session.getSessionFactory());
        return auditConfiguration;
    }

}
