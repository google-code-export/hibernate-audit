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

import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.MappingNotFoundException;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.cfg.Configuration;
import org.hibernate.dialect.Cache71Dialect;
import org.hibernate.dialect.DB2Dialect;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.FrontBaseDialect;
import org.hibernate.dialect.H2Dialect;
import org.hibernate.dialect.HSQLDialect;
import org.hibernate.dialect.InformixDialect;
import org.hibernate.dialect.IngresDialect;
import org.hibernate.dialect.InterbaseDialect;
import org.hibernate.dialect.JDataStoreDialect;
import org.hibernate.dialect.MckoiDialect;
import org.hibernate.dialect.MimerSQLDialect;
import org.hibernate.dialect.MySQLDialect;
import org.hibernate.dialect.Oracle8iDialect;
import org.hibernate.dialect.Oracle9Dialect;
import org.hibernate.dialect.PointbaseDialect;
import org.hibernate.dialect.PostgreSQLDialect;
import org.hibernate.dialect.ProgressDialect;
import org.hibernate.dialect.RDMSOS2200Dialect;
import org.hibernate.dialect.SAPDBDialect;
import org.hibernate.dialect.SQLServerDialect;
import org.hibernate.dialect.Sybase11Dialect;
import org.hibernate.dialect.SybaseASE15Dialect;
import org.hibernate.dialect.SybaseAnywhereDialect;
import org.hibernate.dialect.SybaseDialect;
import org.hibernate.dialect.TeradataDialect;
import org.hibernate.dialect.TimesTenDialect;
import org.hibernate.event.Destructible;
import org.hibernate.event.Initializable;
import org.hibernate.event.PostCollectionRecreateEvent;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreCollectionRemoveEvent;
import org.hibernate.event.PreCollectionRemoveEventListener;
import org.hibernate.event.PreCollectionUpdateEvent;
import org.hibernate.event.PreCollectionUpdateEventListener;
import org.hibernate.mapping.PersistentClass;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.Version;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.configuration.AuditConfigurationObserver;
import com.googlecode.hibernate.audit.synchronization.AuditSynchronization;
import com.googlecode.hibernate.audit.synchronization.work.AuditWorkUnit;
import com.googlecode.hibernate.audit.synchronization.work.DeleteAuditWorkUnit;
import com.googlecode.hibernate.audit.synchronization.work.InsertAuditWorkUnit;
import com.googlecode.hibernate.audit.synchronization.work.InsertCollectionAuditWorkUnit;
import com.googlecode.hibernate.audit.synchronization.work.RemoveCollectionAuditWorkUnit;
import com.googlecode.hibernate.audit.synchronization.work.UpdateAuditWorkUnit;
import com.googlecode.hibernate.audit.synchronization.work.UpdateCollectionAuditWorkUnit;
import com.googlecode.hibernate.audit.util.ConcurrentReferenceHashMap;

public class AuditListener implements PostInsertEventListener, PostUpdateEventListener, PostDeleteEventListener, PreCollectionUpdateEventListener, PreCollectionRemoveEventListener,
        PostCollectionRecreateEventListener, Initializable, Destructible {

    private static final Logger log = Logger.getLogger(AuditListener.class);

    private static final Map<Configuration, AuditConfiguration> CONFIGURATION_MAP = new ConcurrentReferenceHashMap<Configuration, AuditConfiguration>(16,
            ConcurrentReferenceHashMap.ReferenceType.WEAK, ConcurrentReferenceHashMap.ReferenceType.STRONG);
    
    private static final String DEFAULT_AUDIT_MODEL_HBM_PATH = "com/googlecode/hibernate/audit/model/";
    private static final String DEFAULT_AUDIT_MODEL_HBM_NAME = "audit.hbm.xml";
    private static final String DEFAULT_AUDIT_MODEL_HBM_LOCATION = DEFAULT_AUDIT_MODEL_HBM_PATH + DEFAULT_AUDIT_MODEL_HBM_NAME;

    private AuditConfiguration auditConfiguration;
    private boolean recordEmptyCollectionsOnInsert = true;
    
    public void cleanup() {
        if (auditConfiguration != null && auditConfiguration.getAuditedConfiguration() != null) {
            CONFIGURATION_MAP.remove(auditConfiguration.getAuditedConfiguration());
        }
    }

    public void initialize(Configuration conf) {
        try {
            if (CONFIGURATION_MAP.containsKey(conf)) {
                // already initialized
                return;
            }
            Version.touch();
            auditConfiguration = new AuditConfiguration(conf);

            processDynamicUpdate(conf);

            if (conf.getProperty(HibernateAudit.AUDIT_MAPPING_FILE_PROPERTY) != null) {
                conf.addResource(conf.getProperty(HibernateAudit.AUDIT_MAPPING_FILE_PROPERTY));
    			if (log.isInfoEnabled()) {
    				log.info("Loaded Hibernate Audit mapping file from:" + conf.getProperty(HibernateAudit.AUDIT_MAPPING_FILE_PROPERTY));
    			}
            } else {
            	try {
            		String dialectName = getDialectName(Dialect.getDialect(conf.getProperties()));
            		try {
            			conf.addResource(DEFAULT_AUDIT_MODEL_HBM_PATH + dialectName + "-" + DEFAULT_AUDIT_MODEL_HBM_NAME);
            			if (log.isInfoEnabled()) {
            				log.info("Loaded Hibernate Audit mapping file from:" + DEFAULT_AUDIT_MODEL_HBM_PATH + dialectName + "-" + DEFAULT_AUDIT_MODEL_HBM_NAME);
            			}
            		} catch (MappingNotFoundException e) {
                        conf.addResource(DEFAULT_AUDIT_MODEL_HBM_LOCATION);
            			if (log.isInfoEnabled()) {
            				log.info("Loaded Hibernate Audit mapping file from:" + DEFAULT_AUDIT_MODEL_HBM_LOCATION);
            			}
            		}
            	} catch (HibernateException e) {
            		// can't find the dialect - procceed as usual.
                    conf.addResource(DEFAULT_AUDIT_MODEL_HBM_LOCATION);
        			if (log.isInfoEnabled()) {
        				log.info("Loaded Hibernate Audit mapping file from:" + DEFAULT_AUDIT_MODEL_HBM_LOCATION);
        			}
            	}
            }
            conf.buildMappings();

            SessionFactoryObserver sessionFactoryObserver = new AuditSessionFactoryObserver(conf.getSessionFactoryObserver(), auditConfiguration, conf);
            conf.setSessionFactoryObserver(sessionFactoryObserver);

            CONFIGURATION_MAP.put(conf, auditConfiguration);

            processAuditConfigurationObserver(conf);
            
            if (conf.getProperty(HibernateAudit.AUDIT_RECORD_EMPTY_COLLECTIONS_ON_INSERT_PROPERTY) != null) {
            	recordEmptyCollectionsOnInsert = Boolean.valueOf(conf.getProperty(HibernateAudit.AUDIT_RECORD_EMPTY_COLLECTIONS_ON_INSERT_PROPERTY)).booleanValue();
            }
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e);
            }
            throw e;
        }
    }

	protected String getDialectName(Dialect d) {
		if (d instanceof Oracle8iDialect || d instanceof Oracle9Dialect) {
			return "oracle";
		} else if (d instanceof SQLServerDialect) {
			return "sqlserver";
		} else if (d instanceof MySQLDialect) {
			return "mysql";
		} else if (d instanceof Sybase11Dialect || d instanceof SybaseAnywhereDialect || d instanceof SybaseASE15Dialect || d instanceof SybaseDialect) {
			return "sybase";
		} else if (d instanceof Cache71Dialect) {
			return "cache71";
		} else if (d instanceof DB2Dialect) {
			return "db2";
		} else if (d instanceof FrontBaseDialect) {
			return "frontbase";
		} else if (d instanceof H2Dialect) {
			return "h2";
		} else if (d instanceof HSQLDialect) {
			return "hsql";
		} else if (d instanceof InformixDialect) {
			return "informix";
		} else if (d instanceof IngresDialect) {
			return "ingres";
		} else if (d instanceof InterbaseDialect) {
			return "interbase";
		} else if (d instanceof JDataStoreDialect) {
			return "jdatastore";
		} else if (d instanceof MckoiDialect) {
			return "mckoi";
		} else if (d instanceof MimerSQLDialect) {
			return "mimersql";
		} else if (d instanceof PointbaseDialect) {
			return "pointbase";
		} else if (d instanceof PostgreSQLDialect) {
			return "postgresql";
		} else if (d instanceof PostgreSQLDialect || d instanceof ProgressDialect) {
			return "postgresql";
		} else if (d instanceof RDMSOS2200Dialect) {
			return "rdmsos2200";
		} else if (d instanceof SAPDBDialect) {
			return "sapdb";
		} else if (d instanceof TeradataDialect) {
			return "teradata";
		} else if (d instanceof TimesTenDialect) {
			return "timesten";
		}
		
		return d.getClass().getSimpleName();
	}

    private void processDynamicUpdate(Configuration conf) {
        if (conf.getProperty(HibernateAudit.AUDIT_SET_DYNAMIC_UPDATE_FOR_AUDITED_MODEL_PROPERTY) != null
                && Boolean.valueOf(conf.getProperty(HibernateAudit.AUDIT_SET_DYNAMIC_UPDATE_FOR_AUDITED_MODEL_PROPERTY)).booleanValue()) {
            Iterator<PersistentClass> auditedPersistentClassesIterator = conf.getClassMappings();
            while (auditedPersistentClassesIterator.hasNext()) {
                PersistentClass persistentClass = auditedPersistentClassesIterator.next();
                persistentClass.setDynamicUpdate(true);
                if (log.isInfoEnabled()) {
                    log.info("Set dynamic-update to true for entity: " + persistentClass.getEntityName());
                }
            }
        }
    }

    private void processAuditConfigurationObserver(Configuration conf) {
        String observerClazzProperty = conf.getProperty(HibernateAudit.AUDIT_CONFIGURATION_OBSERVER_PROPERTY);

        if (observerClazzProperty != null) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            Class observerClazz = null;
            try {
                observerClazz = contextClassLoader.loadClass(observerClazzProperty);
            } catch (ClassNotFoundException ignored) {
            }

            try {
                if (observerClazz == null) {
                    observerClazz = AuditListener.class.forName(observerClazzProperty);
                }
            } catch (ClassNotFoundException e) {
                throw new HibernateException("Unable to find audit configuration observer class:" + observerClazzProperty, e);
            }

            try {
                AuditConfigurationObserver observer = (AuditConfigurationObserver) observerClazz.newInstance();

                observer.auditConfigurationCreated(auditConfiguration);
            } catch (InstantiationException e) {
                throw new HibernateException("Unable to instantiate audit configuration observer from class:" + observerClazzProperty, e);
            } catch (IllegalAccessException e) {
                throw new HibernateException("Unable to instantiate audit configuration observer from class:" + observerClazzProperty, e);
            } catch (ClassCastException e) {
                throw new HibernateException("Audit configuration observer class:" + observerClazzProperty + " should implement " + AuditConfigurationObserver.class.getName(), e);
            }
        }
    }

    public void onPostInsert(PostInsertEvent event) {
        try {
            String entityName = event.getPersister().getEntityName();

            if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName)) {
                AuditSynchronization sync = auditConfiguration.getAuditSynchronizationManager().get(event.getSession());

                AuditWorkUnit workUnit = new InsertAuditWorkUnit(entityName, event.getId(), event.getEntity(), event.getPersister());
                sync.addWorkUnit(workUnit);
            }
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e);
            }
            throw e;
        }
    }

    public void onPostUpdate(PostUpdateEvent event) {
        try {
            String entityName = event.getPersister().getEntityName();

            if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName)) {
                AuditSynchronization sync = auditConfiguration.getAuditSynchronizationManager().get(event.getSession());

                AuditWorkUnit workUnit = new UpdateAuditWorkUnit(entityName, event.getId(), event.getEntity(), event.getPersister(), event.getOldState(), event.getState());
                sync.addWorkUnit(workUnit);
            }
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e);
            }
            throw e;
        }
    }

    public void onPostDelete(PostDeleteEvent event) {
        try {
            String entityName = event.getPersister().getEntityName();

            if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName)) {
                AuditSynchronization sync = auditConfiguration.getAuditSynchronizationManager().get(event.getSession());

                AuditWorkUnit workUnit = new DeleteAuditWorkUnit(entityName, event.getId(), event.getEntity(), event.getPersister());
                sync.addWorkUnit(workUnit);
            }
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e);
            }
            throw e;
        }
    }

    public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
        try {
            String entityName = event.getAffectedOwnerEntityName();

            if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName) && (recordEmptyCollectionsOnInsert || !event.getCollection().empty())) {
                AuditSynchronization sync = auditConfiguration.getAuditSynchronizationManager().get(event.getSession());
                AuditWorkUnit workUnit = new InsertCollectionAuditWorkUnit(entityName, event.getAffectedOwnerIdOrNull(), event.getAffectedOwnerOrNull(), event.getCollection());
                sync.addWorkUnit(workUnit);
            }
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e);
            }
            throw e;
        }
    }

    public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
        try {
            String entityName = event.getAffectedOwnerEntityName();

            if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName)) {

                AuditSynchronization sync = auditConfiguration.getAuditSynchronizationManager().get(event.getSession());
                AuditWorkUnit workUnit = new UpdateCollectionAuditWorkUnit(entityName, event.getAffectedOwnerIdOrNull(), event.getAffectedOwnerOrNull(), event.getCollection());

                sync.addWorkUnit(workUnit);
            }
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e);
            }
            throw e;
        }
    }

    public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
        try {
            String entityName = event.getAffectedOwnerEntityName();

            if (auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName)) {
                AuditSynchronization sync = auditConfiguration.getAuditSynchronizationManager().get(event.getSession());
                AuditWorkUnit workUnit = new RemoveCollectionAuditWorkUnit(entityName, event.getAffectedOwnerIdOrNull(), event.getAffectedOwnerOrNull(), event.getCollection());

                sync.addWorkUnit(workUnit);
            }
        } catch (RuntimeException e) {
            if (log.isEnabledFor(Level.ERROR)) {
                log.error(e);
            }
            throw e;
        }
    }
}