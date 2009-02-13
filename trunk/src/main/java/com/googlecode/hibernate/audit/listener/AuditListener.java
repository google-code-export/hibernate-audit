/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.listener;

import java.util.Map;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactoryObserver;
import org.hibernate.cfg.Configuration;
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

import com.googlecode.hibernate.audit.HibernateAudit;
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

public class AuditListener implements PostInsertEventListener,
		PostUpdateEventListener, PostDeleteEventListener,
		PreCollectionUpdateEventListener, PreCollectionRemoveEventListener,
		PostCollectionRecreateEventListener, Initializable, Destructible {

	private static final Map<Configuration, AuditConfiguration> CONFIGURATION_MAP = new ConcurrentReferenceHashMap<Configuration, AuditConfiguration>(
			16, ConcurrentReferenceHashMap.ReferenceType.WEAK,
			ConcurrentReferenceHashMap.ReferenceType.STRONG);

	private static final String AUDIT_MODEL_HBM_LOCATION = "com/googlecode/hibernate/audit/model/audit.hbm.xml";

	private AuditConfiguration auditConfiguration;

	public void cleanup() {
		if (auditConfiguration != null
				&& auditConfiguration.getAuditedConfiguration() != null) {
			CONFIGURATION_MAP.remove(auditConfiguration
					.getAuditedConfiguration());
		}
	}

	public void initialize(Configuration conf) {
		if (CONFIGURATION_MAP.containsKey(conf)) {
			// already initialized
			return;
		}
		auditConfiguration = new AuditConfiguration(conf);
		conf.addResource(AUDIT_MODEL_HBM_LOCATION);

		conf.buildMappings();

		SessionFactoryObserver sessionFactoryObserver = new AuditSessionFactoryObserver(
				conf.getSessionFactoryObserver(), auditConfiguration, conf);
		conf.setSessionFactoryObserver(sessionFactoryObserver);

		CONFIGURATION_MAP.put(conf, auditConfiguration);

		processAuditConfigurationObserver(conf);
	}

	private void processAuditConfigurationObserver(Configuration conf) {
		String observerClazzProperty = conf
				.getProperty(HibernateAudit.AUDIT_CONFIGURATION_OBSERVER_PROPERTY);

		if (observerClazzProperty != null) {
			ClassLoader contextClassLoader = Thread.currentThread()
					.getContextClassLoader();
			Class observerClazz = null;
			try {
				observerClazz = contextClassLoader
						.loadClass(observerClazzProperty);
			} catch (ClassNotFoundException ignored) {
			}

			try {
				if (observerClazz == null) {
					observerClazz = AuditListener.class
							.forName(observerClazzProperty);
				}
			} catch (ClassNotFoundException e) {
				throw new HibernateException(
						"Unable to find audit configuration observer class:"
								+ observerClazzProperty, e);
			}

			try {
				AuditConfigurationObserver observer = (AuditConfigurationObserver) observerClazz
						.newInstance();

				observer.auditConfigurationCreated(auditConfiguration);
			} catch (InstantiationException e) {
				throw new HibernateException(
						"Unable to instantiate audit configuration observer from class:"
								+ observerClazzProperty, e);
			} catch (IllegalAccessException e) {
				throw new HibernateException(
						"Unable to instantiate audit configuration observer from class:"
								+ observerClazzProperty, e);
			} catch (ClassCastException e) {
				throw new HibernateException(
						"Audit configuration observer class:"
								+ observerClazzProperty + " should implement "
								+ AuditConfigurationObserver.class.getName(), e);
			}
		}
	}

	public void onPostInsert(PostInsertEvent event) {
		String entityName = event.getPersister().getEntityName();

		if (auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(entityName)) {
			AuditSynchronization sync = auditConfiguration
					.getAuditSynchronizationManager().get(event.getSession());

			AuditWorkUnit workUnit = new InsertAuditWorkUnit(entityName, event
					.getId(), event.getEntity(), event.getPersister());
			sync.addWorkUnit(workUnit);
		}
	}

	public void onPostUpdate(PostUpdateEvent event) {
		String entityName = event.getPersister().getEntityName();

		if (auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(entityName)) {
			AuditSynchronization sync = auditConfiguration
					.getAuditSynchronizationManager().get(event.getSession());

			AuditWorkUnit workUnit = new UpdateAuditWorkUnit(entityName, event
					.getId(), event.getEntity(), event.getPersister(), event
					.getOldState(), event.getState());
			sync.addWorkUnit(workUnit);
		}
	}

	public void onPostDelete(PostDeleteEvent event) {
		String entityName = event.getPersister().getEntityName();

		if (auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(entityName)) {
			AuditSynchronization sync = auditConfiguration
					.getAuditSynchronizationManager().get(event.getSession());

			AuditWorkUnit workUnit = new DeleteAuditWorkUnit(entityName, event
					.getId(), event.getEntity(), event.getPersister());
			sync.addWorkUnit(workUnit);
		}
	}

	public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
		String entityName = event.getAffectedOwnerEntityName();

		if (auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(entityName)) {

			AuditSynchronization sync = auditConfiguration
					.getAuditSynchronizationManager().get(event.getSession());
			AuditWorkUnit workUnit = new InsertCollectionAuditWorkUnit(
					entityName, event.getAffectedOwnerIdOrNull(), event
							.getAffectedOwnerOrNull(), event.getCollection());
			sync.addWorkUnit(workUnit);
		}
	}

	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
		String entityName = event.getAffectedOwnerEntityName();

		if (auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(entityName)) {

			AuditSynchronization sync = auditConfiguration
					.getAuditSynchronizationManager().get(event.getSession());
			AuditWorkUnit workUnit = new UpdateCollectionAuditWorkUnit(
					entityName, event.getAffectedOwnerIdOrNull(), event
							.getAffectedOwnerOrNull(), event.getCollection());

			sync.addWorkUnit(workUnit);
		}
	}

	public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
		String entityName = event.getAffectedOwnerEntityName();

		if (auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(entityName)) {
			AuditSynchronization sync = auditConfiguration
					.getAuditSynchronizationManager().get(event.getSession());
			AuditWorkUnit workUnit = new RemoveCollectionAuditWorkUnit(
					entityName, event.getAffectedOwnerIdOrNull(), event
							.getAffectedOwnerOrNull(), event.getCollection());

			sync.addWorkUnit(workUnit);
		}
	}
}