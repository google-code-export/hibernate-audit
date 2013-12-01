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
package com.googlecode.hibernate.audit.listener;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.NamedQueryDefinition;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.stat.Statistics;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.util.ConcurrentReferenceHashMap;

public class AuditSessionFactoryObserver implements SessionFactoryObserver {
    private static final Logger log = LoggerFactory.getLogger(AuditSessionFactoryObserver.class);

    private static final Map<SessionFactory, AuditConfiguration> CONFIGURATION_MAP = new ConcurrentReferenceHashMap<SessionFactory, AuditConfiguration>(16,
            ConcurrentReferenceHashMap.ReferenceType.WEAK, ConcurrentReferenceHashMap.ReferenceType.STRONG);

    private SessionFactoryObserver observer;
    private AuditConfiguration auditConfiguration;
    private Configuration configuration;

    public AuditSessionFactoryObserver(SessionFactoryObserver observer, AuditConfiguration auditConfiguration, Configuration configuration) {
        this.observer = observer;
        this.auditConfiguration = auditConfiguration;
        this.configuration = configuration;
    }

    public void sessionFactoryCreated(SessionFactory sessionfactory) {
        initializeAuditMetatdata(sessionfactory);

        Statistics statistics = sessionfactory.getStatistics();

        if (observer != null) {
            observer.sessionFactoryCreated(sessionfactory);
        }
        
        CONFIGURATION_MAP.put(sessionfactory, auditConfiguration);
    }

    public void sessionFactoryClosed(SessionFactory sessionfactory) {
        if (observer != null) {
            observer.sessionFactoryClosed(sessionfactory);
        }
        CONFIGURATION_MAP.remove(sessionfactory);
    }

    public static AuditConfiguration getAuditConfiguration(SessionFactory sessionFactory) {
        return CONFIGURATION_MAP.get(sessionFactory);
    }
    
    private void initializeAuditMetatdata(SessionFactory sessionFactory) {
        Collection<ClassMetadata> allClassMetadata = sessionFactory.getAllClassMetadata().values();

        if (log.isInfoEnabled()) {
            log.info("Start building audit log metadata.");
        }
        Session session = null;

        try {
            try {
                session = sessionFactory.openSession();
                Transaction tx = session.beginTransaction();

                for (ClassMetadata classMetadata : allClassMetadata) {
                    String entityName = classMetadata.getEntityName();

                    if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName)) {

                        initializeEntityAuditType(session, entityName, true);
                        session.flush();
                    }
                }

                tx.commit();
            } finally {
                if (session != null && session.isOpen()) {
                    session.close();
                }
            }
        } finally {
            if (log.isInfoEnabled()) {
                log.info("End building audit log metadata.");
            }
        }
    }

    private AuditType initializeEntityAuditType(Session session, String entityName, boolean initializeProperties) {
        PersistentClass classMapping = configuration.getClassMapping(entityName);
        Class mappedClass = classMapping.getMappedClass();
        
        if (mappedClass == null) {
        	mappedClass = classMapping.getProxyInterface();
        }
        
        AuditType auditType = HibernateAudit.getAuditType(session, mappedClass.getName());
        if (auditType == null) {
            auditType = new AuditType();
            auditType.setClassName(mappedClass.getName());
            auditType.setLabel(entityName);
            auditType.setType(AuditType.ENTITY_TYPE);

            session.save(auditType);
            updateMetaModel(session);
        }

        if (initializeProperties) {
            Property identifierProperty = classMapping.getIdentifierProperty();
            if (identifierProperty != null) {
                initializeAuditField(session, mappedClass, auditType, identifierProperty.getName(), identifierProperty.getType());
            }

            for (Iterator propertyIterator = classMapping.getPropertyClosureIterator(); propertyIterator.hasNext();) {
                Property property = (Property) propertyIterator.next();
                initializeAuditField(session, mappedClass, auditType, property.getName(), property.getType());
            }
        }
        return auditType;
    }

    private AuditType initializeComponentAuditType(Session session, CompositeType type) {
        Class returnedClass = type.getReturnedClass();

        AuditType componentAuditType = HibernateAudit.getAuditType(session, returnedClass.getName());
        if (componentAuditType == null) {
            componentAuditType = new AuditType();
            componentAuditType.setClassName(returnedClass.getName());
            componentAuditType.setType(AuditType.COMPONENT_TYPE);
            session.save(componentAuditType);
            updateMetaModel(session);
        }

        String[] componentPropertyNames = type.getPropertyNames();
        if (componentPropertyNames != null) {
            for (int i = 0; i < componentPropertyNames.length; i++) {
                AuditTypeField componentAuditField = initializeAuditField(session, returnedClass, componentAuditType, componentPropertyNames[i], type.getSubtypes()[i]);

            }
        }
        return componentAuditType;
    }

    private AuditType initializePrimitiveAuditType(Session session, Type type) {
        AuditType auditType = HibernateAudit.getAuditType(session, type.getReturnedClass().getName());

        if (auditType == null) {
            auditType = new AuditType();
            auditType.setClassName(type.getReturnedClass().getName());
            if (type.isCollectionType()) {
                auditType.setType(AuditType.COLLECTION_TYPE);
            } else {
                auditType.setType(AuditType.PRIMITIVE_TYPE);
            }
            session.save(auditType);
            updateMetaModel(session);
        }

        return auditType;
    }

    private AuditTypeField initializeAuditField(Session session, Class ownerClass, AuditType auditType, String propertyName, Type type) {

        AuditTypeField auditField = HibernateAudit.getAuditField(session, ownerClass.getName(), propertyName);

        if (auditField == null) {
            auditField = new AuditTypeField();
            auditField.setName(propertyName);
            auditField.setOwnerType(auditType);
            auditType.getAuditFields().add(auditField);

            AuditType auditFieldType = null;

            if (type.isCollectionType()) {
                auditFieldType = initializePrimitiveAuditType(session, type);

                Type elementType = ((CollectionType) type).getElementType((SessionFactoryImplementor) session.getSessionFactory());

                if (elementType.isCollectionType()) {
                    // do nothing..
                } else if (elementType.isComponentType()) {
                    initializeComponentAuditType(session, (ComponentType) elementType);
                } else if (elementType.isEntityType()) {
                    // do nothing .. it will be handled during the entity model
                    // traverse
                } else {
                    initializePrimitiveAuditType(session, elementType);
                }
            } else if (type.isComponentType()) {
                auditFieldType = initializeComponentAuditType(session, (CompositeType) type);
            } else if (type.isEntityType()) {
                auditFieldType = initializeEntityAuditType(session, ((EntityType) type).getName(), false);
            } else {
                auditFieldType = initializePrimitiveAuditType(session, type);
            }

            auditField.setFieldType(auditFieldType);
            session.save(auditField);

            updateMetaModel(session);
        }

        return auditField;
    }

    private void updateMetaModel(Session session) {
        session.flush();
        NamedQueryDefinition selectAuditTypeNamedQueryDefinition = ((SessionFactoryImplementor) session.getSessionFactory()).getNamedQuery(HibernateAudit.SELECT_AUDIT_TYPE_BY_CLASS_NAME);
        NamedQueryDefinition selectAuditTypeFieldNamedQueryDefinition = ((SessionFactoryImplementor) session.getSessionFactory()).getNamedQuery(HibernateAudit.SELECT_AUDIT_TYPE_FIELD_BY_CLASS_NAME_AND_PROPERTY_NAME);
        
        if (selectAuditTypeNamedQueryDefinition.isCacheable() && selectAuditTypeNamedQueryDefinition.getCacheRegion() != null) {
            session.getSessionFactory().evictQueries(selectAuditTypeNamedQueryDefinition.getCacheRegion());
        }
        
        if (selectAuditTypeFieldNamedQueryDefinition.isCacheable() && selectAuditTypeFieldNamedQueryDefinition.getCacheRegion() != null) {
            session.getSessionFactory().evictQueries(selectAuditTypeFieldNamedQueryDefinition.getCacheRegion());
        }
        
        session.getSessionFactory().evictEntity(AuditType.class.getName());
        session.getSessionFactory().evictEntity(AuditTypeField.class.getName());
    }
}
