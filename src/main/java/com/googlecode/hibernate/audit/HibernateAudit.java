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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Hibernate;
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
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;

public final class HibernateAudit {
    private static final Logger log = LoggerFactory.getLogger(HibernateAudit.class);

    public static final String AUDIT_CONFIGURATION_OBSERVER_PROPERTY = "hba.configuration.observer.clazz";
    public static final String AUDIT_SET_DYNAMIC_UPDATE_FOR_AUDITED_MODEL_PROPERTY = "hba.audited-model.dynamic-update";
    public static final String AUDIT_MAPPING_FILE_PROPERTY = "hba.mappingfile";
    public static final String AUDIT_RECORD_EMPTY_COLLECTIONS_ON_INSERT_PROPERTY = "hba.record.empty.collections.on.insert";

    public static final String SELECT_AUDIT_LOCAL_GROUP_BY_AUDIT_TYPE_AND_EXTERNAL_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAuditLogicalGroup";
    public static final String SELECT_AUDIT_TYPE_BY_CLASS_NAME = "com.googlecode.hibernate.audit.HibernateAudit.getAuditType";
    public static final String SELECT_AUDIT_TYPE_FIELD_BY_CLASS_NAME_AND_PROPERTY_NAME = "com.googlecode.hibernate.audit.HibernateAudit.getAuditField";

    public static final String SELECT_LATEST_AUDIT_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionId";
    public static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_AUDIT_LOGICAL_GROUP = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByAuditLogicalGroup";
    public static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_AUDIT_LOGICAL_GROUP_AND_AFTER_AUDIT_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByAuditLogicalGroupAndAfterAuditTransactionId";
    public static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_ENTITY = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByEntity";
    public static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_ENTITY_AND_AFTER_AUDIT_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByEntityAndAfterAuditTransactionId";
    public static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_PROPERTY = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByProperty";
    public static final String SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_PROPERTY_AND_AFTER_AUDIT_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getLatestAuditTransactionIdByPropertyAndAfterAuditTransactionId";

    public static final String SELECT_MODIFIED_AUDIT_OBJECT_PROPERTIES = "com.googlecode.hibernate.audit.HibernateAudit.getModifiedAuditObjectProperties";
    public static final String SELECT_MODIFIED_AUDIT_TYPE_FIELDS = "com.googlecode.hibernate.audit.HibernateAudit.getModifiedAuditTypeFields";
        
    public static final String SELECT_AUDIT_TRANSACTION_BY_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAuditTransaction";
    public static final String SELECT_ALL_AUDIT_TRANSACTIONS_AFTER_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditTransactionsAfterTransactionId";

    public static final String SELECT_AUDIT_TRANSACTIONS_FOR_ENTITY = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditTransactionsForEntity";
    public static final String SELECT_AUDIT_EVENTS_FOR_ENTITY = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditEventsForEntity";
    public static final String SELECT_AUDIT_EVENTS_FOR_ENTITY_UNTIL_TRANSACTION_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAllAuditEventsForEntityUntilTransactionId";

    public static final String AUDIT_META_DATA_QUERY_CACHE_REGION = "com.googlecode.hibernate.audit.model.query";
    public static final String AUDIT_LOGICAL_GROUP_QUERY_CACHE_REGION = "com.googlecode.hibernate.audit.model.AuditLogicalGroup.query";

    private static final Map<String, AuditType> AUDIT_TYPE_CACHE = new ConcurrentHashMap<String, AuditType>();
    private static final Map<String, AuditTypeField> AUDIT_TYPE_FIELD_CACHE = new ConcurrentHashMap<String, AuditTypeField>();
    
    private HibernateAudit() {
    }

    public static Long getLatestAuditTransactionId(Session session) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID);
        Long auditTransactionId = (Long) query.uniqueResult();
        return auditTransactionId;

    }

    public static Long getLatestAuditTransactionIdByAuditLogicalGroup(Session session, AuditLogicalGroup auditLogicalGroup) {
/*        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_AUDIT_LOGICAL_GROUP);
        query.setParameter("auditLogicalGroup", auditLogicalGroup);
        Long auditTransactionId = (Long) query.uniqueResult();
*/        
        return auditLogicalGroup.getLastUpdatedAuditTransactionId();
    }

    public static Long getLatestAuditTransactionIdByAuditLogicalGroupAndAfterAuditTransactionId(Session session, AuditLogicalGroup auditLogicalGroup, Long afterAuditTransactionId) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_AUDIT_LOGICAL_GROUP_AND_AFTER_AUDIT_TRANSACTION_ID);
        query.setParameter("auditLogicalGroup", auditLogicalGroup);
        query.setParameter("afterAuditTransactionId", afterAuditTransactionId);
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

    public static Long getLatestAuditTransactionIdByEntityAndAfterAuditTransactionId(Session session, AuditType auditType, String targetEntityId, Long afterAuditTransactionId) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_ENTITY_AND_AFTER_AUDIT_TRANSACTION_ID);
        query.setParameter("auditType", auditType);
        query.setParameter("targetEntityId", targetEntityId);
        query.setParameter("afterAuditTransactionId", afterAuditTransactionId);

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

    public static Long getLatestAuditTransactionIdByPropertyAndAfterAuditTransactionId(Session session, AuditTypeField auditTypeField, String targetEntityId, Long afterAuditTransactionId) {
        Query query = session.getNamedQuery(SELECT_LATEST_AUDIT_TRANSACTION_ID_BY_PROPERTY_AND_AFTER_AUDIT_TRANSACTION_ID);
        query.setParameter("auditTypeField", auditTypeField);
        query.setParameter("targetEntityId", targetEntityId);
        query.setParameter("afterAuditTransactionId", afterAuditTransactionId);

        Long auditTransactionId = (Long) query.uniqueResult();
        return auditTransactionId;
    }

    public static List<AuditObjectProperty> getModifiedAuditObjectProperties(Session session, List<AuditTypeField> auditTypeFieldsToCheck, String targetEntityId, Long afterAuditTransactionId) {
        Query query = session.getNamedQuery(SELECT_MODIFIED_AUDIT_OBJECT_PROPERTIES);
        query.setParameterList("auditTypeFields", auditTypeFieldsToCheck);
        query.setParameter("targetEntityId", targetEntityId);
        query.setParameter("afterAuditTransactionId", afterAuditTransactionId);

        List<AuditObjectProperty> result = (List<AuditObjectProperty>) query.list();
        return result;
    }
    
    public static List<AuditTypeField> getModifiedAuditTypeFields(Session session, List<AuditTypeField> auditTypeFieldsToCheck, String targetEntityId, Long afterAuditTransactionId) {
        Query query = session.getNamedQuery(SELECT_MODIFIED_AUDIT_TYPE_FIELDS);
        query.setParameterList("auditTypeFields", auditTypeFieldsToCheck);
        query.setParameter("targetEntityId", targetEntityId);
        query.setParameter("afterAuditTransactionId", afterAuditTransactionId);

        List<AuditTypeField> result = (List<AuditTypeField>) query.list();
        return result;
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
        
        //query.setCacheable(true);
        //query.setCacheRegion(AUDIT_LOGICAL_GROUP_QUERY_CACHE_REGION);
        
        AuditLogicalGroup storedAuditLogicalGroup = (AuditLogicalGroup) query.uniqueResult();
        return storedAuditLogicalGroup;
    }

    public static AuditType getAuditType(Session session, String className) {
    	AuditType result = AUDIT_TYPE_CACHE.get(className);
    	if (result != null) {
    		return result;
    	} else {
	        Query query = session.getNamedQuery(SELECT_AUDIT_TYPE_BY_CLASS_NAME);
	        query.setParameter("className", className);
	
	        //query.setCacheable(true);
	        //query.setCacheRegion(AUDIT_META_DATA_QUERY_CACHE_REGION);
	
	        result = (AuditType) query.uniqueResult();
	        
	        if (result != null) {
	        	// eager load all collections because of the internal cache
	        	for (AuditTypeField f: result.getAuditFields()) {
		        	if (!Hibernate.isInitialized(f.getAuditTypeFieldAttributes())) {
		        		Hibernate.initialize(f.getAuditTypeFieldAttributes());
		        	}
	        		AUDIT_TYPE_FIELD_CACHE.put(className + ":" + f.getName(), f);
	        	}
	        	if (!Hibernate.isInitialized(result.getAuditTypeAttributes())) {
	        		Hibernate.initialize(result.getAuditTypeAttributes());
	        	}
	        	AUDIT_TYPE_CACHE.put(className, result);
	        }
	        return result;
    	}
    }

    public static AuditTypeField getAuditField(Session session, String className, String propertyName) {
    	AuditTypeField result = AUDIT_TYPE_FIELD_CACHE.get(className + ":" + propertyName);
    	if (result != null) {
    		return result;
    	} else {
	        Query query = session.getNamedQuery(SELECT_AUDIT_TYPE_FIELD_BY_CLASS_NAME_AND_PROPERTY_NAME);
	
	        query.setParameter("className", className);
	        query.setParameter("name", propertyName);
	
	        //query.setCacheable(true);
	        //query.setCacheRegion(AUDIT_META_DATA_QUERY_CACHE_REGION);
	
	        result = (AuditTypeField) query.uniqueResult();
	        if (result != null) {
	        	// eager load all collections because of the internal cache
	        	if (!Hibernate.isInitialized(result.getAuditTypeFieldAttributes())) {
	        		Hibernate.initialize(result.getAuditTypeFieldAttributes());
	        	}
	        	AUDIT_TYPE_FIELD_CACHE.put(className + ":" + propertyName, result);
	        }
	        return result;
    	}
    }

    public static String getEntityName(AuditConfiguration configuration, Session session, String implementationClass) {
        Collection<ClassMetadata> allClassMetadata = session.getSessionFactory().getAllClassMetadata().values();
        for (ClassMetadata classMetadata : allClassMetadata) {
            String entityName = classMetadata.getEntityName();
            PersistentClass classMapping = configuration.getAuditedConfiguration().getClassMapping(entityName);
            Class mappedClass = classMapping.getMappedClass();
            if (mappedClass == null) {
            	mappedClass = classMapping.getProxyInterface();
            }
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
