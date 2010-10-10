package com.googlecode.hibernate.audit.extension.concurrent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.LockMode;
import org.hibernate.Session;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.exception.ConcurrentModificationException;
import com.googlecode.hibernate.audit.exception.ObjectConcurrentModificationException;
import com.googlecode.hibernate.audit.exception.PropertyConcurrentModificationException;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.AuditObject;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;

public class DefaultConcurrentModificationCheckProvider implements ConcurrentModificationCheckProvider {
	protected final Logger log = Logger.getLogger(DefaultConcurrentModificationCheckProvider.class);

	public void concurrentModificationCheck(AuditConfiguration auditConfiguration, Session session, SortedSet<AuditLogicalGroup> auditLogicalGroups,
			AuditTransaction auditTransaction, Long loadAuditTransactionId) {
		if (loadAuditTransactionId != null) {
			for (AuditLogicalGroup storedAuditLogicalGroup : auditLogicalGroups) {
				if (log.isEnabledFor(Level.DEBUG)) {
					log.debug("lock AuditLogicalGroup with id:" + storedAuditLogicalGroup.getId());
				}
				// session.lock(storedAuditLogicalGroup, LockMode.UPGRADE);
				// not the audit logical group is not immutable
				session.refresh(storedAuditLogicalGroup, LockMode.UPGRADE);
			}
			try {
				concurrentModificationCheck(auditConfiguration, session, auditTransaction, loadAuditTransactionId);
			} catch (ConcurrentModificationException ce) {
				if (log.isEnabledFor(Level.DEBUG)) {
					log.debug(ce);
				}
				throw ce;
			}
		}
	}
	
	
	protected void concurrentModificationCheck(AuditConfiguration auditConfiguration, Session session, AuditTransaction auditTransaciton, Long loadAuditTransactionId) {
		// Map<AuditLogicalGroup, Long> auditLogicalGroupToAuditTransactionId =
		// new IdentityHashMap<AuditLogicalGroup, Long>();

/*		Do not hit the database table audit_transaction because it can be large table
  		Long latestTransaction = HibernateAudit.getLatestAuditTransactionId(session);
		if (latestTransaction != null && latestTransaction.equals(loadAuditTransactionId)) {
			// performance optimization - check the global transaction id - not
			// filtered by audit logical group
			return;
		}
*/
		for (AuditEvent e : auditTransaciton.getEvents()) {
			AuditLogicalGroup auditLogicalGroup = e.getAuditLogicalGroup();
			if (auditLogicalGroup != null) {
				/*
				 * if(!auditLogicalGroupToAuditTransactionId.containsKey(
				 * auditLogicalGroup)) {
				 * auditLogicalGroupToAuditTransactionId.put(auditLogicalGroup,
				 * HibernateAudit.
				 * getLatestAuditTransactionIdByAuditLogicalGroupAndAfterAuditTransactionId
				 * (session, auditLogicalGroup, loadAuditTransactionId)); }
				 */
				Long latestLogicalGroupTransactionId = auditLogicalGroup.getLastUpdatedAuditTransactionId();// auditLogicalGroupToAuditTransactionId.get(auditLogicalGroup);

				if (latestLogicalGroupTransactionId == null || latestLogicalGroupTransactionId.equals(loadAuditTransactionId)) {
					// performance optimization
					continue;
				}

			}

			// NOTE: when the auditLogicalGroup is null then no DB locking is
			// performed - we do not have audit logical group to perform the
			// locking..
			concurrentModificationCheck(auditConfiguration, session, e, loadAuditTransactionId);
		}
	}

	protected void concurrentModificationCheck(AuditConfiguration auditConfiguration, Session session, AuditEvent e, Long loadAuditTransactionId) {
		for (AuditObject auditObject : e.getAuditObjects()) {

			if (ConcurrentModificationLevelCheck.OBJECT.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getLevelCheck())) {
				entityConcurrentModificationCheck(auditConfiguration, session, loadAuditTransactionId, auditObject);
			}

			if (ConcurrentModificationLevelCheck.PROPERTY.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getLevelCheck())) {
				// find if there are fields that were modified
				propertyConcurrentModificationCheck(auditConfiguration, session, e, loadAuditTransactionId, auditObject);
			}
		}
	}

	protected void propertyConcurrentModificationCheck(AuditConfiguration auditConfiguration, Session session, AuditEvent e, Long loadAuditTransactionId, AuditObject auditObject) {
		List<AuditTypeField> fieldsToCheck = new ArrayList<AuditTypeField>();
		for (AuditObjectProperty auditObjectProperty : auditObject.getAuditObjectProperties()) {
			fieldsToCheck.add(auditObjectProperty.getAuditField());
		}

		if (fieldsToCheck.isEmpty()) {
			// there are no fields that we are going to modify - this means that the event is a delete event for the whole object - include all properties
			fieldsToCheck.addAll(auditObject.getAuditType().getAuditFields());
		}
		
		List<AuditTypeField> modifiedAuditTypeFields = getModifiedAuditTypeFields(session, e, loadAuditTransactionId,
				fieldsToCheck);
		Iterator<AuditTypeField> modifiedAuditTypeFieldsIterator = modifiedAuditTypeFields.iterator();

		if (modifiedAuditTypeFieldsIterator.hasNext()) {
			AuditTypeField firstDetectedmodifiedAuditTypeField = modifiedAuditTypeFieldsIterator.next();

			if (ConcurrentModificationBehavior.THROW_EXCEPTION.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getCheckBehavior())) {
				if (session.getTransaction().isActive()) {
					session.getTransaction().rollback();
				}
				throw new PropertyConcurrentModificationException(firstDetectedmodifiedAuditTypeField.getOwnerType().getClassName(), firstDetectedmodifiedAuditTypeField.getName(),
						firstDetectedmodifiedAuditTypeField.getOwnerType().getLabel(), firstDetectedmodifiedAuditTypeField.getLabel(), e.getEntityId());
			} else if (ConcurrentModificationBehavior.LOG.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getCheckBehavior())) {
				if (log.isEnabledFor(Level.WARN)) {
					log.warn("Concurrent modification detected: className=" + firstDetectedmodifiedAuditTypeField.getOwnerType().getClassName() + ",field name="
							+ firstDetectedmodifiedAuditTypeField.getName() + ",class label=" + firstDetectedmodifiedAuditTypeField.getOwnerType().getLabel() + ",field label="
							+ firstDetectedmodifiedAuditTypeField.getLabel() + ",entity id=" + e.getEntityId());
				}
			}
		}
	}


	protected List<AuditTypeField> getModifiedAuditTypeFields(Session session, AuditEvent e, Long loadAuditTransactionId,
			List<AuditTypeField> fieldsToCheck) {
		List<AuditTypeField> modifiedAuditTypeFields = HibernateAudit.getModifiedAuditTypeFields(session, fieldsToCheck, e.getEntityId(), loadAuditTransactionId);
		return modifiedAuditTypeFields;
	}

	protected void entityConcurrentModificationCheck(AuditConfiguration auditConfiguration, Session session, Long loadAuditTransactionId, AuditObject auditObject) {
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

		Long latestEntityTransactionId = HibernateAudit.getLatestAuditTransactionIdByEntityAndAfterAuditTransactionId(session, auditType, targetEntityId, loadAuditTransactionId);
		if (latestEntityTransactionId != null && !latestEntityTransactionId.equals(loadAuditTransactionId)) {
			if (ConcurrentModificationBehavior.THROW_EXCEPTION.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getCheckBehavior())) {
				if (session.getTransaction().isActive()) {
					session.getTransaction().rollback();
				}
				throw new ObjectConcurrentModificationException(auditType.getClassName(), auditType.getLabel(), targetEntityId);
			} else if (ConcurrentModificationBehavior.LOG.equals(auditConfiguration.getExtensionManager().getConcurrentModificationProvider().getCheckBehavior())) {
				if (log.isEnabledFor(Level.WARN)) {
					log.warn("Concurrent modification detected: className=" + auditType.getClassName() + ",label=" + auditType.getLabel() + ",targetEntityId=" + targetEntityId);
				}
			}
		}
	}


}
