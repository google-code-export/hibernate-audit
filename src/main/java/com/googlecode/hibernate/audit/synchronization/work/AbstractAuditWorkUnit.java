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
package com.googlecode.hibernate.audit.synchronization.work;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.transaction.InvalidTransactionException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.NamedQueryDefinition;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.AuditObject;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;
import com.googlecode.hibernate.audit.model.property.ComponentObjectProperty;
import com.googlecode.hibernate.audit.model.property.EntityObjectProperty;
import com.googlecode.hibernate.audit.model.property.SimpleObjectProperty;

public abstract class AbstractAuditWorkUnit implements AuditWorkUnit {
    private static final Logger log = Logger.getLogger(AbstractAuditWorkUnit.class);

    protected abstract String getEntityName();

    private List<AuditLogicalGroup> auditLogicalGroups = new ArrayList<AuditLogicalGroup>();

    protected void processProperty(Session session, AuditConfiguration auditConfiguration, AuditEvent auditEvent, Object object, String propertyName, Object propertyValue, Type propertyType,
            AuditObject auditObject) {
        if (!auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(getEntityName(), propertyName)) {
            return;
        }
        AuditObjectProperty property = null;

        if (propertyType.isEntityType()) {
            property = processEntityProperty(session, auditConfiguration, object, propertyName, propertyValue, propertyType, auditObject);
        } else if (propertyType.isCollectionType()) {
            // collection will be handled by collection event listeners
        } else if (propertyType.isComponentType()) {
            property = processComponentValue(session, auditConfiguration, auditEvent, auditObject, getEntityName(), object, propertyName, propertyValue, (AbstractComponentType) propertyType);
        } else {
            property = createSimpleValue(session, auditConfiguration, auditObject, getEntityName(), object, propertyName, propertyValue);
        }

        if (property != null) {
            AuditType auditType = null;
            if (propertyValue != null) {
                auditType = HibernateAudit.getAuditType(session, propertyValue.getClass().getName());
                if (auditType == null) {
                    // subclass that was not registered in the audit metadata -
                    // use the base class
                    auditType = property.getAuditField().getFieldType();
                }
            }

            property.setAuditType(auditType);
            auditObject.getAuditObjectProperties().add(property);
        }
    }

    protected EntityObjectProperty processEntityProperty(Session session, AuditConfiguration auditConfiguration, Object object, String propertyName, Object propertyValue, Type propertyType,
            AuditObject auditObject) {
        String entityName = ((EntityType) propertyType).getAssociatedEntityName();

        Serializable id = null;
        AuditTypeField auditField = HibernateAudit.getAuditField(session, object.getClass().getName(), propertyName);

        if (propertyValue != null) {
            id = session.getSessionFactory().getClassMetadata(entityName).getIdentifier(propertyValue, session.getEntityMode());
        }
        EntityObjectProperty property = new EntityObjectProperty();
        property.setAuditObject(auditObject);
        property.setAuditField(auditField);
        property.setIndex(null);
        property.setTargetEntityId(auditConfiguration.getExtensionManager().getPropertyValueConverter().toString(id));

        return property;
    }

    protected ComponentObjectProperty processComponentValue(Session session, AuditConfiguration auditConfiguration, AuditEvent auditEvent, AuditObject auditObject, String entityName, Object entity,
            String propertyName, Object component, AbstractComponentType componentType) {
        AuditTypeField auditField = HibernateAudit.getAuditField(session, entity.getClass().getName(), propertyName);

        ComponentObjectProperty property = new ComponentObjectProperty();
        property.setAuditObject(auditObject);
        property.setAuditField(auditField);
        property.setIndex(null);
        if (component != null) {
            property.setAuditType(HibernateAudit.getAuditType(session, component.getClass().getName()));
        }

        ComponentAuditObject targetComponentAuditObject = null;

        if (component != null) {
            targetComponentAuditObject = new ComponentAuditObject();
            targetComponentAuditObject.setAuditEvent(auditEvent);
            targetComponentAuditObject.setParentAuditObject(auditObject);
            AuditType auditType = HibernateAudit.getAuditType(session, component.getClass().getName());
            targetComponentAuditObject.setAuditType(auditType);

            for (int i = 0; i < componentType.getPropertyNames().length; i++) {
                String componentPropertyName = componentType.getPropertyNames()[i];

                Type componentPropertyType = componentType.getSubtypes()[i];
                Object componentPropertyValue = componentType.getPropertyValue(component, i, (SessionImplementor) session);

                processProperty(session, auditConfiguration, auditEvent, component, componentPropertyName, componentPropertyValue, componentPropertyType, targetComponentAuditObject);
            }
        }
        property.setTargetComponentAuditObject(targetComponentAuditObject);

        return property;
    }

    protected SimpleObjectProperty createSimpleValue(Session session, AuditConfiguration auditConfiguration, AuditObject auditObject, String entityName, Object entity, String propertyName,
            Object propertyValue) {
        AuditTypeField auditField = HibernateAudit.getAuditField(session, entity.getClass().getName(), propertyName);

        SimpleObjectProperty property = new SimpleObjectProperty();
        property.setAuditObject(auditObject);
        property.setAuditField(auditField);
        property.setIndex(null);
        property.setValue(auditConfiguration.getExtensionManager().getPropertyValueConverter().toString(propertyValue));

        return property;
    }

    public List<AuditLogicalGroup> getAuditLogicalGroups() {
        return auditLogicalGroups;
    }

    protected AuditLogicalGroup getAuditLogicalGroup(Session session, AuditConfiguration auditConfiguration, AuditEvent auditEvent) {

        AuditLogicalGroup logicalGroup = auditConfiguration.getExtensionManager().getAuditLogicalGroupProvider().getAuditLogicalGroup(session, auditEvent);

        AuditLogicalGroup result = null;

        if (logicalGroup != null) {
            AuditType auditType = HibernateAudit.getAuditType(session, logicalGroup.getAuditType().getClassName());
            String externalId = logicalGroup.getExternalId();

            result = HibernateAudit.getAuditLogicalGroup(session, auditType, externalId);

            HibernateException createAuditLogicalGroupException = null;
            if (result == null) {
            	createAuditLogicalGroupException = createAuditLogicalGroup(session, logicalGroup, auditType);
                // remove the cached query (possibly null) results so that the result after that is not null. 
                NamedQueryDefinition namedQueryDefinition = ((SessionFactoryImplementor) session.getSessionFactory()).getNamedQuery(HibernateAudit.SELECT_AUDIT_LOCAL_GROUP_BY_AUDIT_TYPE_AND_EXTERNAL_ID);
                if (namedQueryDefinition.isCacheable()) {
                    String cacheRegion = ((SessionFactoryImplementor) session.getSessionFactory()).getNamedQuery(HibernateAudit.SELECT_AUDIT_LOCAL_GROUP_BY_AUDIT_TYPE_AND_EXTERNAL_ID).getCacheRegion();
                    if (cacheRegion != null) {
                        session.getSessionFactory().evictQueries(cacheRegion);
                    } else {
                        session.getSessionFactory().evictQueries();
                    }
                }
                
                result = HibernateAudit.getAuditLogicalGroup(session, auditType, externalId);

            }
            if (result == null) {
            	if (createAuditLogicalGroupException != null) {
            		throw createAuditLogicalGroupException;
            	} else {
            		throw new HibernateException("Unable to create and then retrieve AuditLogicalGroup: className=" + logicalGroup.getAuditType().getClassName() + ",externalId=" + logicalGroup.getExternalId());
            	}
            }
            auditLogicalGroups.add(result);
        }

        return result;
    }

    private HibernateException createAuditLogicalGroup(Session session, AuditLogicalGroup logicalGroup, AuditType auditType) {
        Session newSession = null;

        TransactionManager txManager = null;
        javax.transaction.Transaction suspendedTransaction = null;

        try {
            txManager = ((SessionFactoryImplementor) session.getSessionFactory()).getTransactionManager();
            if (txManager != null) {
                try {
                    suspendedTransaction = txManager.suspend();
                } catch (SystemException e) {
                    throw new HibernateException(e);
                }
            }
            Transaction tx = null;
            try {
                newSession = session.getSessionFactory().openSession();
                tx = newSession.beginTransaction();
                logicalGroup.setAuditType(auditType);
                // when we are creating the audit logical group for the first time we set it to 0, this is before even we may have transaction record - this is executed in separate transaction
                logicalGroup.setLastUpdatedAuditTransactionId(Long.valueOf(0)); 
                newSession.save(logicalGroup);
                tx.commit();
                
                return null;
            } catch (HibernateException e) {
                if (log.isEnabledFor(Level.DEBUG)) {
                    // log the exception is debug level because this most likely
                    // will indicate that there was a concurrent insert and we
                    // are prepared to handle such calls. If this is not the
                    // case and we want to troubleshoot where is the problem
                    // then at least log the exception is DEBUG level so we can
                    // see it.
                    log.debug(e);
                }
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (HibernateException ignored) {
                    }
                }
                
                return e;
            } finally {
                if (newSession != null && newSession.isOpen()) {
                    try {
                        newSession.close();
                    } catch (HibernateException ignored) {
                    }
                }
            }
        } finally {
            if (txManager != null && suspendedTransaction != null) {
                try {
                    txManager.resume(suspendedTransaction);
                } catch (SystemException e) {
                    throw new HibernateException(e);
                } catch (InvalidTransactionException e) {
                    throw new HibernateException(e);
                }
            }
        }
    }

}
