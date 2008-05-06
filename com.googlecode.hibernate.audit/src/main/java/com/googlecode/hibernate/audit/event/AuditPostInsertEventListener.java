package com.googlecode.hibernate.audit.event;

import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.StatelessSession;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.model.AuditOperation;
import com.googlecode.hibernate.audit.model.transaction.AuditTransaction;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionComponentRecord;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionRecord;
import com.googlecode.hibernate.audit.model.transaction.record.field.AuditTransactionRecordField;
import com.googlecode.hibernate.audit.model.transaction.record.field.AuditTransactionRecordFieldComponentReferenceValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.AuditTransactionRecordFieldEntityReferenceValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.AuditTransactionRecordFieldSimpleValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.value.ComponentReferenceAuditValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.value.EntityReferenceAuditValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.value.SimpleAuditValue;

@SuppressWarnings("serial")
public class AuditPostInsertEventListener extends AuditAbstractEventListener {

	private Logger LOG = Logger
			.getLogger(AuditPostInsertEventListener.class);

	@Override
	protected AuditOperation getAuditEntityOperation(AbstractEvent event) {
		return AuditOperation.INSERT;
	}

	@Override
	protected Object getEntity(AbstractEvent object) {
		PostInsertEvent event = (PostInsertEvent) object;
		return event.getEntity();
	}

	@Override
	protected EntityPersister getEntityPersister(AbstractEvent object) {
		PostInsertEvent event = (PostInsertEvent) object;
		return event.getPersister();
	}

	@Override
	protected StatelessSession openStatelessSession(AbstractEvent object) {
		PostInsertEvent event = (PostInsertEvent) object;
		return event.getSession().getFactory().openStatelessSession(
				event.getSession().connection());
	}

	protected void doAuditEntityProperties(StatelessSession session,
			AbstractEvent object, AuditTransaction auditTransaction,
			AuditTransactionRecord auditEntity) {
		if (LOG.isEnabledFor(Level.DEBUG)) {
			LOG.debug("Invoke PostInsertEvent listener");
		}
		PostInsertEvent event = (PostInsertEvent) object;
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

	private void processProperties(PostInsertEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			Serializable entityId, String entityName, EntityMode entityMode,
			AuditTransactionRecord auditEntity, AuditTransaction auditTransaction) {

		for (String propertyName : persister.getPropertyNames()) {
			Object propertyValue = persister.getPropertyValue(entity,
					propertyName, entityMode);
			if (propertyValue != null) {
				if (isAuditSuppressed(entity.getClass(), propertyName)) {
					continue;
				}
				Type propertyType = persister.getPropertyType(propertyName);

				if (propertyType.isEntityType()) {
					createEntityRef(event, session, entityName, entityMode,
							auditEntity, propertyName, propertyValue);
				} else if (propertyType.isCollectionType()) {
					// collection event listener will process that
				} else if (propertyType.isComponentType()) {
					createComponent(event, session, persister, entity,
							entityName, entityMode, auditEntity,
							auditTransaction, propertyName, propertyName,
							propertyValue, (ComponentType) propertyType);
				} else {
					createValue(session, entityName, auditEntity, propertyName,
							propertyValue);
				}
			}
		}
	}

	private void createComponent(PostInsertEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			String entityName, EntityMode entityMode, AuditTransactionRecord auditEntity,
			AuditTransaction auditTransaction, String propertyAccessPath,
			String propertyName, Object propertyValue,
			ComponentType propertyType) {
		AuditTransactionRecordField componentObjectProperty = new AuditTransactionRecordField();
		componentObjectProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		componentObjectProperty.setOperation(AuditOperation.INSERT);

		//auditEntity.addAuditTransactionRecordField(componentObjectProperty);
		componentObjectProperty.setAuditTransactionRecord(auditEntity);
		
		AuditTransactionRecordFieldComponentReferenceValue componentObjectValue = new AuditTransactionRecordFieldComponentReferenceValue();
		AuditTransactionComponentRecord component = persistComponent(event, session, persister,
				entity, auditEntity, propertyValue, propertyAccessPath,
				propertyName, propertyType, auditTransaction, entityMode);

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
		simpleObjectProperty.setOperation(AuditOperation.INSERT);

		//auditEntity.addAuditTransactionRecordField(simpleObjectProperty);
		simpleObjectProperty.setAuditTransactionRecord(auditEntity);
		
		AuditTransactionRecordFieldSimpleValue simpleObjectValue = new AuditTransactionRecordFieldSimpleValue();
		simpleObjectValue.setValue(new SimpleAuditValue(String.valueOf(propertyValue)));
		simpleObjectValue.setRecordField(simpleObjectProperty);
		//simpleObjectProperty.setValue(simpleObjectValue);
		simpleObjectValue.setRecordField(simpleObjectProperty);
		
		session.insert(simpleObjectProperty);
		session.insert(simpleObjectValue);
	}

	private void createEntityRef(PostInsertEvent event,
			StatelessSession session, String entityName, EntityMode entityMode,
			AuditTransactionRecord auditEntity, String propertyName, Object propertyValue) {
		EntityPersister propertyPersister = event.getSession()
				.getEntityPersister(
						event.getSession().bestGuessEntityName(propertyValue),
						propertyValue);

		Serializable entityRefId = propertyPersister.getIdentifier(
				propertyValue, entityMode);

		AuditTransactionRecordField entityRefProperty = new AuditTransactionRecordField();
		entityRefProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		entityRefProperty.setOperation(AuditOperation.INSERT);
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

	private AuditTransactionComponentRecord persistComponent(PostInsertEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			AuditTransactionRecord auditEntity, Object component,
			String parentPropertyAccessPath, String parentPropertyName,
			ComponentType componentType, AuditTransaction auditTransaction,
			EntityMode entityMode) {

		String componentName = componentType.getReturnedClass().getName();
		AuditTransactionComponentRecord result = createAuditComponent(session, auditEntity
				.getAudittedEntityId(), componentName, AuditOperation.INSERT,
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
