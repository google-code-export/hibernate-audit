package com.googlecode.hibernate.audit.event;

import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.StatelessSession;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.model_obsolete.AuditOperation;
import com.googlecode.hibernate.audit.model_obsolete.transaction.AuditTransactionObsolete;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.AuditTransactionComponentRecord;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.AuditTransactionRecord;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordField;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordFieldComponentReferenceValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordFieldEntityReferenceValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordFieldSimpleValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.ComponentReferenceAuditValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.EntityReferenceAuditValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.SimpleAuditValue;

@SuppressWarnings("serial")
public class AuditPostUpdateEventListener extends AuditAbstractEventListener {

	private Logger LOG = Logger
			.getLogger(AuditPostUpdateEventListener.class);

	@Override
	protected AuditOperation getAuditEntityOperation(AbstractEvent event) {
		return AuditOperation.UPDATE;
	}

	@Override
	protected Object getEntity(AbstractEvent object) {
		PostUpdateEvent event = (PostUpdateEvent) object;
		return event.getEntity();
	}

	@Override
	protected EntityPersister getEntityPersister(AbstractEvent object) {
		PostUpdateEvent event = (PostUpdateEvent) object;
		return event.getPersister();
	}

	@Override
	protected StatelessSession openStatelessSession(AbstractEvent object) {
		PostUpdateEvent event = (PostUpdateEvent) object;
		return event.getSession().getFactory().openStatelessSession(
				event.getSession().connection());
	}

	protected void doAuditEntityProperties(StatelessSession session,
			AbstractEvent object, AuditTransactionObsolete auditTransaction,
			AuditTransactionRecord auditEntity) {
		if (LOG.isEnabledFor(Level.DEBUG)) {
			LOG.debug("Invoke PostUpdateEvent listener");
		}
		PostUpdateEvent event = (PostUpdateEvent) object;
		Object entity = getEntity(object);
		EntityMode entityMode = event.getSession().getEntityMode();
		
		EntityPersister persister = getEntityPersister(object);
		Serializable entityId = event.getId();
		String entityName = entity.getClass().getName().toString();

/*		if (persister.hasIdentifierProperty()) {
			entityId = persister.getIdentifier(entity, entityMode);
		}
*/		processProperties(event, session, persister, entity, entityId,
				entityName, entityMode, auditEntity, auditTransaction);
	}

	private void processProperties(PostUpdateEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			Serializable entityId, String entityName, EntityMode entityMode,
			AuditTransactionRecord auditEntity, AuditTransactionObsolete auditTransaction) {

		int[] changedPropertyIndexes = persister.findDirty(event.getOldState(),
				event.getState(), entity, event.getSession());

		String[] propertyNames = persister.getPropertyNames();

		for (int i = 0; i < changedPropertyIndexes.length; i++) {
			String propertyName = propertyNames[changedPropertyIndexes[i]];
			if (isAuditSuppressed(entity.getClass(), propertyName)) {
				continue;
			}

			Object propertyValue = persister.getPropertyValue(entity,
					propertyName, entityMode);

			Type propertyType = persister.getPropertyType(propertyName);

			if (propertyType.isEntityType()) {
				createEntityRef(event, session, entityName, entityMode,
						auditEntity, propertyName, propertyValue);
			} else if (propertyType.isCollectionType()) {
				// collection event listener will process that
			} else if (propertyType.isComponentType()) {
				createComponent(event, session, persister, entity, entityName,
						entityMode, auditEntity, auditTransaction,
						propertyName, propertyName, propertyValue,
						(ComponentType) propertyType);
			} else {
				createValue(session, entityName, auditEntity, propertyName,
						propertyValue);
			}
		}
	}

	private void createComponent(PostUpdateEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			String entityName, EntityMode entityMode, AuditTransactionRecord auditEntity,
			AuditTransactionObsolete auditTransaction, String propertyAccessPath,
			String propertyName, Object propertyValue,
			ComponentType propertyType) {
		AuditTransactionRecordField componentObjectProperty = new AuditTransactionRecordField();
		componentObjectProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		componentObjectProperty.setOperation(AuditOperation.UPDATE);

		//auditEntity.addAuditTransactionRecordField(componentObjectProperty);
		componentObjectProperty.setAuditTransactionRecord(auditEntity);
		
		AuditTransactionRecordFieldComponentReferenceValue componentObjectValue = new AuditTransactionRecordFieldComponentReferenceValue();
		AuditTransactionComponentRecord component = null;
		if (propertyValue != null) {
			component = persistComponent(event, session, persister, entity,
					auditEntity, propertyValue, propertyAccessPath,
					propertyName, propertyType, auditTransaction, entityMode);
		}
		componentObjectValue.setValue(new ComponentReferenceAuditValue(component));
		componentObjectValue.setRecordField(componentObjectProperty);
		//componentObjectProperty.setValue(componentObjectValue);
		componentObjectValue.setRecordField(componentObjectProperty);
		
		session.insert(componentObjectProperty);
		session.insert(componentObjectValue);
	}

	private void createValue(StatelessSession session, String entityName,
			AuditTransactionRecord auditEntity, String propertyName, Object propertyValue) {
		AuditTransactionRecordField simpleObjectProperty = new AuditTransactionRecordField();
		simpleObjectProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		simpleObjectProperty.setOperation(AuditOperation.UPDATE);

		//auditEntity.addAuditTransactionRecordField(simpleObjectProperty);
		simpleObjectProperty.setAuditTransactionRecord(auditEntity);
		
		AuditTransactionRecordFieldSimpleValue simpleObjectValue = new AuditTransactionRecordFieldSimpleValue();
		simpleObjectValue.setValue(propertyValue == null ? null : new SimpleAuditValue(String
				.valueOf(propertyValue)));
		simpleObjectValue.setRecordField(simpleObjectProperty);
		//simpleObjectProperty.setValue(simpleObjectValue);
		simpleObjectValue.setRecordField(simpleObjectProperty);
		
		session.insert(simpleObjectProperty);
		session.insert(simpleObjectValue);
	}

	private void createEntityRef(PostUpdateEvent event,
			StatelessSession session, String entityName, EntityMode entityMode,
			AuditTransactionRecord auditEntity, String propertyName, Object propertyValue) {
		Serializable entityRefId = null;
		if (propertyValue != null) {
			EntityPersister propertyPersister = event.getSession()
					.getEntityPersister(
							event.getSession().bestGuessEntityName(
									propertyValue), propertyValue);

			entityRefId = propertyPersister.getIdentifier(propertyValue,
					entityMode);
		}

		AuditTransactionRecordField entityRefProperty = new AuditTransactionRecordField();
		entityRefProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		entityRefProperty.setOperation(AuditOperation.UPDATE);
		//auditEntity.addAuditTransactionRecordField(entityRefProperty);
		entityRefProperty.setAuditTransactionRecord(auditEntity);
		
		AuditTransactionRecordFieldEntityReferenceValue entityRefPropertyValue = new AuditTransactionRecordFieldEntityReferenceValue();
		entityRefPropertyValue.setValue(new EntityReferenceAuditValue(String.valueOf(entityRefId)));
		entityRefPropertyValue.setRecordField(entityRefProperty);
		//entityRefProperty.setValue(entityRefPropertyValue);
		entityRefPropertyValue.setRecordField(entityRefProperty);
		
		session.insert(entityRefProperty);
		session.insert(entityRefPropertyValue);
	}

	private AuditTransactionComponentRecord persistComponent(PostUpdateEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			AuditTransactionRecord auditEntity, Object component,
			String parentPropertyAccessPath, String parentPropertyName,
			ComponentType componentType, AuditTransactionObsolete auditTransaction,
			EntityMode entityMode) {

		String componentName = componentType.getReturnedClass().getName();
		AuditTransactionComponentRecord result = createAuditComponent(session, auditEntity
				.getAuditedEntityId(), componentName, AuditOperation.UPDATE,
				auditTransaction);

		String[] propertyNames = componentType.getPropertyNames();
		for (int i = 0; i < propertyNames.length; i++) {
			String propertyName = propertyNames[i];

			Type componentPropertyType = persister
					.getPropertyType(parentPropertyAccessPath + "."
							+ propertyName);

			Object componentPropertyValue = null;

			if (componentPropertyType.isEntityType()) {
				componentPropertyValue = persister.getPropertyValue(entity,
						parentPropertyAccessPath + "." + propertyName,
						entityMode);
			} else if (componentPropertyType.isCollectionType()) {
				// collection ....
			} else if (componentPropertyType.isComponentType()) {
				componentPropertyValue = componentType.getPropertyValue(
						component, i, entityMode);
			} else {
				componentPropertyValue = componentType.getPropertyValue(
						component, i, entityMode);
			}

			if (componentPropertyValue != null) {
				if (componentPropertyType.isEntityType()) {
					createEntityRef(event, session, componentName, entityMode,
							result, propertyName, componentPropertyValue);
				} else if (componentPropertyType.isCollectionType()) {
					// see if we need to handle collections inside components -
					// e.g. if the collection listener will process that.
				} else if (componentPropertyType.isComponentType()) {
					String childComponentName = componentPropertyType
							.getReturnedClass().getName();

					createComponent(event, session, persister, result,
							childComponentName, entityMode, result,
							auditTransaction, parentPropertyAccessPath + "."
									+ propertyName, propertyName,
							componentPropertyValue,
							(ComponentType) componentPropertyType);
				} else {
					createValue(session, componentName, result, propertyName,
							componentPropertyValue);
				}
			}
		}

		return result;
	}
}
