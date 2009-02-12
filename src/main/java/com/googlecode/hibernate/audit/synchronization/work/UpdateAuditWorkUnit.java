/**
 * 
 */
package com.googlecode.hibernate.audit.synchronization.work;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.object.AuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;

public class UpdateAuditWorkUnit extends AbstractAuditWorkUnit {
	private String entityName;
	private Serializable id;
	private Object entity;
	private EntityPersister entityPersister;
	private Object[] oldState;
	private Object[] newState;
	private AuditEvent auditEvent;

	public UpdateAuditWorkUnit(String entityName, Serializable id,
			Object entity, EntityPersister entityPersister, Object[] oldState,
			Object[] newState) {
		this.entityName = entityName;
		this.id = id;
		this.entity = entity;
		this.entityPersister = entityPersister;
		this.oldState = oldState;
		this.newState = newState;
	}

	@Override
	protected String getEntityName() {
		return entityName;
	}

	public void perform(Session session, AuditConfiguration auditConfiguration,
			AuditTransaction auditTransaction) {
		initializeAuditEvents(session, auditConfiguration);

		if (auditEvent != null) {
			auditEvent.setAuditTransaction(auditTransaction);
			auditTransaction.getEvents().add(auditEvent);
			AuditLogicalGroup logicalGroup = getAuditLogicalGroup(session, auditConfiguration, auditEvent);

			auditEvent.setAuditLogicalGroup(logicalGroup);
		}
	}

	public void init(Session session, AuditConfiguration auditConfiguration) {

	}

	private void initializeAuditEvents(Session session,
			AuditConfiguration auditConfiguration) {
		AuditType auditType = HibernateAudit.getAuditType(session, entity
				.getClass().getName());
		auditEvent = new AuditEvent();
		auditEvent.setAuditType(auditType);
		auditEvent.setType(AuditEvent.UPDATE_AUDIT_EVENT_TYPE);
		auditEvent.setEntityId(id == null ? null : id.toString());

		EntityAuditObject auditObject = new EntityAuditObject();
		auditObject.setAuditEvent(auditEvent);
		auditObject.setAuditType(auditType);
		auditObject.setTargetEntityId(id == null ? null : id.toString());
		auditEvent.getAuditObjects().add(auditObject);

		processProperties(session, auditConfiguration, auditEvent,
				entityPersister, entity, auditObject);
	}

	private void processProperties(Session session,
			AuditConfiguration auditConfiguration, AuditEvent auditEvent,
			EntityPersister persister, Object entity, AuditObject auditObject) {

		int[] changedPropertyIndexes = persister.findDirty(oldState, newState,
				entity, (SessionImplementor) session);

		String[] propertyNames = persister.getPropertyNames();

		for (int i = 0; i < changedPropertyIndexes.length; i++) {
			String propertyName = propertyNames[changedPropertyIndexes[i]];

			if (auditConfiguration.getExtensionManager()
					.getAuditableInformationProvider().isAuditable(entityName,
							propertyName)) {

				Type propertyType = persister.getPropertyType(propertyName);
				Object propertyValue = persister.getPropertyValue(entity,
						propertyName, persister.guessEntityMode(entity));

				processProperty(session, auditConfiguration, auditEvent,
						entity, propertyName, propertyValue, propertyType,
						auditObject);
			}
		}
	}
}
