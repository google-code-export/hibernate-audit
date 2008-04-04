package com.googlecode.hibernate.audit.event;

import java.io.Serializable;

import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.StatelessSession;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.ComponentType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.model.AuditComponentProperty;
import com.googlecode.hibernate.audit.model.AuditComponentPropertyValue;
import com.googlecode.hibernate.audit.model.AuditEntityRefProperty;
import com.googlecode.hibernate.audit.model.AuditEntityRefPropertyValue;
import com.googlecode.hibernate.audit.model.AuditObject;
import com.googlecode.hibernate.audit.model.AuditOperation;
import com.googlecode.hibernate.audit.model.AuditSimpleProperty;
import com.googlecode.hibernate.audit.model.AuditSimplePropertyValue;
import com.googlecode.hibernate.audit.model.AuditTransaction;

@SuppressWarnings("serial")
public class AuditPostInsertEventListener extends AuditAbstractEventListener {

	private Logger LOG = Logger
			.getLogger(AuditPostInsertEventListener.class);

	@Override
	protected AuditOperation getAuditEntityOperation(Object event) {
		return AuditOperation.INSERT;
	}

	@Override
	protected Object getEntity(Object object) {
		PostInsertEvent event = (PostInsertEvent) object;
		return event.getEntity();
	}

	@Override
	protected EntityPersister getEntityPersister(Object object) {
		PostInsertEvent event = (PostInsertEvent) object;
		return event.getPersister();
	}

	@Override
	protected StatelessSession openStatelessSession(Object object) {
		PostInsertEvent event = (PostInsertEvent) object;
		return event.getSession().getFactory().openStatelessSession(
				event.getSession().connection());
	}

	protected void doAuditEntityProperties(StatelessSession session,
			Object object, AuditTransaction auditTransaction,
			AuditObject auditEntity) {
		PostInsertEvent event = (PostInsertEvent) object;
		Object entity = getEntity(object);
		EntityMode entityMode = getEntityPersister(object).guessEntityMode(
				entity);
		EntityPersister persister = getEntityPersister(object);
		Serializable entityId = null;
		String entityName = entity.getClass().getName().toString();

		if (persister.hasIdentifierProperty()) {
			entityId = persister.getIdentifier(entity, entityMode);
		}
		processProperties(event, session, persister, entity, entityId,
				entityName, entityMode, auditEntity, auditTransaction);
	}

	private void processProperties(PostInsertEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			Serializable entityId, String entityName, EntityMode entityMode,
			AuditObject auditEntity, AuditTransaction auditTransaction) {

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
			String entityName, EntityMode entityMode, AuditObject auditEntity,
			AuditTransaction auditTransaction, String propertyAccessPath,
			String propertyName, Object propertyValue,
			ComponentType propertyType) {
		AuditComponentProperty componentObjectProperty = new AuditComponentProperty();
		componentObjectProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		componentObjectProperty.setOperation(AuditOperation.INSERT);

		auditEntity.addAuditObjectProperty(componentObjectProperty);

		AuditComponentPropertyValue componentObjectValue = new AuditComponentPropertyValue();
		AuditObject component = persistComponent(event, session, persister,
				entity, auditEntity, propertyValue, propertyAccessPath,
				propertyName, propertyType, auditTransaction, entityMode);

		componentObjectValue.setAuditObject(component);
		componentObjectProperty.addValue(componentObjectValue);

		session.insert(componentObjectProperty);
		session.insert(componentObjectValue);
	}

	private void createValue(StatelessSession session, String entityName,
			AuditObject auditEntity, String propertyName, Object propertyValue) {
		AuditSimpleProperty simpleObjectProperty = new AuditSimpleProperty();
		simpleObjectProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		simpleObjectProperty.setOperation(AuditOperation.INSERT);

		auditEntity.addAuditObjectProperty(simpleObjectProperty);

		AuditSimplePropertyValue simpleObjectValue = new AuditSimplePropertyValue();
		simpleObjectValue.setValue(String.valueOf(propertyValue));
		simpleObjectProperty.addValue(simpleObjectValue);

		session.insert(simpleObjectProperty);
		session.insert(simpleObjectValue);
	}

	private void createEntityRef(PostInsertEvent event,
			StatelessSession session, String entityName, EntityMode entityMode,
			AuditObject auditEntity, String propertyName, Object propertyValue) {
		EntityPersister propertyPersister = event.getSession()
				.getEntityPersister(
						event.getSession().bestGuessEntityName(propertyValue),
						propertyValue);

		Serializable entityRefId = propertyPersister.getIdentifier(
				propertyValue, entityMode);

		AuditEntityRefProperty entityRefProperty = new AuditEntityRefProperty();
		entityRefProperty.setAuditClassProperty(getOrCreateAuditProperty(
				session, entityName, propertyName));
		entityRefProperty.setOperation(AuditOperation.INSERT);
		auditEntity.addAuditObjectProperty(entityRefProperty);

		AuditEntityRefPropertyValue entityRefPropertyValue = new AuditEntityRefPropertyValue();
		entityRefPropertyValue.setEntityRefId(String.valueOf(entityRefId));
		entityRefProperty.addValue(entityRefPropertyValue);

		session.insert(entityRefProperty);
		session.insert(entityRefPropertyValue);
	}

	private AuditObject persistComponent(PostInsertEvent event,
			StatelessSession session, EntityPersister persister, Object entity,
			AuditObject auditEntity, Object component,
			String parentPropertyAccessPath, String parentPropertyName,
			ComponentType componentType, AuditTransaction auditTransaction,
			EntityMode entityMode) {

		String componentName = componentType.getReturnedClass().getName();
		AuditObject result = createAuditEntity(session, auditEntity
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
