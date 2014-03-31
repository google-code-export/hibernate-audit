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

import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.InvalidTransactionException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.SystemException;
import javax.transaction.TransactionManager;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.NamedQueryDefinition;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.engine.transaction.jta.platform.spi.JtaPlatform;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.mapping.Property;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.stat.Statistics;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.util.ConcurrentReferenceHashMap;

public class AuditSessionFactoryObserver implements SessionFactoryObserver {
    private static final Logger log = LoggerFactory.getLogger(AuditSessionFactoryObserver.class);

    private static final Map<SessionFactory, AuditConfiguration> CONFIGURATION_MAP = new ConcurrentReferenceHashMap<SessionFactory, AuditConfiguration>(16,
            ConcurrentReferenceHashMap.ReferenceType.WEAK, ConcurrentReferenceHashMap.ReferenceType.STRONG);

    private AuditConfiguration auditConfiguration;
    private Configuration configuration;

    public AuditSessionFactoryObserver(AuditConfiguration auditConfiguration, Configuration configuration) {
        this.auditConfiguration = auditConfiguration;
        this.configuration = configuration;
    }

    public void sessionFactoryCreated(SessionFactory sessionfactory) {
        initializeAuditMetatdata(sessionfactory);

        Statistics statistics = sessionfactory.getStatistics();

        CONFIGURATION_MAP.put(sessionfactory, auditConfiguration);
    }

    public void sessionFactoryClosed(SessionFactory sessionfactory) {
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
                JtaPlatform jtaPlatform = ((SessionImplementor)session).getTransactionCoordinator().getTransactionContext().getTransactionEnvironment().getJtaPlatform();
                TransactionManager jtaTransactionManager = null;
                if (jtaPlatform != null) {
                	jtaTransactionManager = jtaPlatform.retrieveTransactionManager();
                }
                
                Transaction tx = null;
                javax.transaction.Transaction jtaTransaction = null;
                
                try {
					if (jtaTransactionManager != null) {
						try {
							jtaTransaction = jtaTransactionManager.suspend();
							jtaTransactionManager.begin();
						} catch (NotSupportedException e) {
							throw new HibernateException(e);
						} catch (SystemException e) {
							throw new HibernateException(e);
						}
					} else {
					    tx = session.beginTransaction();
					}

					for (ClassMetadata classMetadata : allClassMetadata) {
					    String entityName = classMetadata.getEntityName();
					    if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName)) {
					        initializeEntityAuditType(session, entityName, true);
					        session.flush();
					    }
					}
					
					if (jtaTransactionManager != null) {
						try {
							jtaTransactionManager.commit();
						} catch (SecurityException e) {
							throw new HibernateException(e);
						} catch (IllegalStateException e) {
							throw new HibernateException(e);
						} catch (RollbackException e) {
							throw new HibernateException(e);
						} catch (HeuristicMixedException e) {
							throw new HibernateException(e);
						} catch (HeuristicRollbackException e) {
							throw new HibernateException(e);
						} catch (SystemException e) {
							throw new HibernateException(e);
						}
					} else if (tx != null) {
						tx.commit();
					}
				} finally {
	            	if (jtaTransactionManager != null && jtaTransaction != null) {
            			try {
							jtaTransactionManager.resume(jtaTransaction);
						} catch (InvalidTransactionException e) {
							throw new HibernateException(e);
						} catch (IllegalStateException e) {
							throw new HibernateException(e);
						} catch (SystemException e) {
							throw new HibernateException(e);
						}
	            	}
				}
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
        String auditTypeClassName = auditConfiguration.getExtensionManager().getAuditableInformationProvider().getAuditTypeClassName(configuration, entityName);
        
        AuditType auditType = HibernateAudit.getAuditType(session, auditTypeClassName);
        if (auditType == null) {
            auditType = new AuditType();
            auditType.setClassName(auditTypeClassName);
            auditType.setLabel(entityName);
            auditType.setType(AuditType.ENTITY_TYPE);

            session.save(auditType);
            updateMetaModel(session);
        }

        if (initializeProperties) {
            Property identifierProperty = classMapping.getIdentifierProperty();
            if (identifierProperty != null) {
                initializeAuditField(session, auditTypeClassName, auditType, identifierProperty.getName(), identifierProperty.getType());
            }

            for (Iterator propertyIterator = classMapping.getPropertyClosureIterator(); propertyIterator.hasNext();) {
                Property property = (Property) propertyIterator.next();
                initializeAuditField(session, auditTypeClassName, auditType, property.getName(), property.getType());
            }
        }
        return auditType;
    }

    private AuditType initializeComponentAuditType(Session session, CompositeType type) {
        String auditTypeClassName = auditConfiguration.getExtensionManager().getAuditableInformationProvider().getAuditTypeClassName(configuration, type);

        AuditType componentAuditType = HibernateAudit.getAuditType(session, auditTypeClassName);
        if (componentAuditType == null) {
            componentAuditType = new AuditType();
            componentAuditType.setClassName(auditTypeClassName);
            componentAuditType.setType(AuditType.COMPONENT_TYPE);
            session.save(componentAuditType);
            updateMetaModel(session);
        }

        String[] componentPropertyNames = type.getPropertyNames();
        if (componentPropertyNames != null) {
            for (int i = 0; i < componentPropertyNames.length; i++) {
                AuditTypeField componentAuditField = initializeAuditField(session, auditTypeClassName, componentAuditType, componentPropertyNames[i], type.getSubtypes()[i]);

            }
        }
        return componentAuditType;
    }

    private AuditType initializePrimitiveAuditType(Session session, Type type) {
    	String auditTypeClassName = auditConfiguration.getExtensionManager().getAuditableInformationProvider().getAuditTypeClassName(configuration, type);
    	AuditType auditType = HibernateAudit.getAuditType(session, auditTypeClassName);

        if (auditType == null) {
            auditType = new AuditType();
            auditType.setClassName(auditTypeClassName);
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

    private AuditTypeField initializeAuditField(Session session, String auditTypeClassName, AuditType auditType, String propertyName, Type type) {

        AuditTypeField auditField = HibernateAudit.getAuditField(session, auditTypeClassName, propertyName);

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
