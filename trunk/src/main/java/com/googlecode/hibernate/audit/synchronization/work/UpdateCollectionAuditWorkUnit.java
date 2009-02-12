package com.googlecode.hibernate.audit.synchronization.work;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;
import com.googlecode.hibernate.audit.model.property.ComponentObjectProperty;
import com.googlecode.hibernate.audit.model.property.EntityObjectProperty;
import com.googlecode.hibernate.audit.model.property.SimpleObjectProperty;

public class UpdateCollectionAuditWorkUnit extends AbstractAuditWorkUnit {
	private String entityName;
	private Serializable id;
	private Object entity;
	private PersistentCollection persistentCollection;
	private Iterator deletesIterator;
	private Serializable snapshot;
	private CollectionPersister collectionPersister;
	
	private List<Integer> insertIndexes = new ArrayList<Integer>();
	private List<Integer> updateIndexes = new ArrayList<Integer>();

	private List<AuditEvent> auditEvents = new ArrayList<AuditEvent>();

	public UpdateCollectionAuditWorkUnit(String entityName, Serializable id,
			Object entity, PersistentCollection persistentCollection) {
		this.entityName = entityName;
		this.id = id;
		this.entity = entity;
		this.persistentCollection = persistentCollection;
		this.snapshot = persistentCollection.getStoredSnapshot();
	}

	@Override
	protected String getEntityName() {
		return entityName;
	}

	public void perform(Session session, AuditConfiguration auditConfiguration,
			AuditTransaction auditTransaction) {
		initializeAuditEvents(session, auditConfiguration);
		for (AuditEvent auditEvent : auditEvents) {
			auditTransaction.getEvents().add(auditEvent);
			auditEvent.setAuditTransaction(auditTransaction);
			AuditLogicalGroup logicalGroup = getAuditLogicalGroup(session, auditConfiguration, auditEvent);

			auditEvent.setAuditLogicalGroup(logicalGroup);
		}
	}

	public void init(Session session, AuditConfiguration auditConfiguration) {
		collectionPersister = ((SessionImplementor) session)
				.getPersistenceContext().getCollectionEntry(
						persistentCollection).getCurrentPersister();
		this.deletesIterator = persistentCollection.getDeletes(
				collectionPersister, false);
		Iterator<? extends Object> iterator = persistentCollection
				.entries(collectionPersister);
		Type elementType = collectionPersister.getCollectionMetadata()
				.getElementType();

		for (int i = 0; iterator.hasNext(); i++) {
			Object element = iterator.next();

			AuditEvent auditEvent = null;
			EntityAuditObject auditObject = null;
			if (persistentCollection.needsInserting(element, i, elementType)) {
				insertIndexes.add(new Integer(i));
			} else if (persistentCollection.needsUpdating(element, i,
					elementType)) {
				updateIndexes.add(new Integer(i));
			}
		}
	}

	private void initializeAuditEvents(Session session,
			AuditConfiguration auditConfiguration) {
		String role = collectionPersister.getCollectionMetadata().getRole();
		String propertyName = role.substring(role.lastIndexOf('.') != -1 ? role
				.lastIndexOf('.') + 1 : 0, role.length());

		if (!auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(entityName,
						propertyName)
				|| !persistentCollection.wasInitialized()) {
			return;
		}

		AuditType auditType = HibernateAudit.getAuditType(session, entity
				.getClass().getName());

		AuditEvent insertAuditEvent = new AuditEvent();
		insertAuditEvent.setType(AuditEvent.ADD_AUDIT_EVENT_TYPE);
		insertAuditEvent.setEntityId(id == null ? null : id.toString());

		AuditEvent updateAuditEvent = new AuditEvent();
		updateAuditEvent.setType(AuditEvent.MODIFY_AUDIT_EVENT_TYPE);
		updateAuditEvent.setEntityId(id == null ? null : id.toString());

		EntityAuditObject insertAuditObject = new EntityAuditObject();
		insertAuditObject.setAuditEvent(insertAuditEvent);
		insertAuditObject.setAuditType(auditType);
		insertAuditObject.setTargetEntityId(id == null ? null : id.toString());
		insertAuditEvent.getAuditObjects().add(insertAuditObject);

		EntityAuditObject updateAuditObject = new EntityAuditObject();
		updateAuditObject.setAuditEvent(updateAuditEvent);
		updateAuditObject.setAuditType(auditType);
		updateAuditObject.setTargetEntityId(id == null ? null : id.toString());
		updateAuditEvent.getAuditObjects().add(updateAuditObject);

		Type elementType = collectionPersister.getCollectionMetadata()
				.getElementType();

		Iterator<? extends Object> iterator = persistentCollection
				.entries(collectionPersister);

		for (int i = 0; iterator.hasNext(); i++) {
			Object element = iterator.next();

			AuditEvent auditEvent = null;
			EntityAuditObject auditObject = null;
			if (insertIndexes.contains(new Integer(i))) {
				auditEvent = insertAuditEvent;
				auditObject = insertAuditObject;
			} else if (updateIndexes.contains(new Integer(i))) {
				auditEvent = updateAuditEvent;
				auditObject = updateAuditObject;
			} else {
				continue;
			}

			processElement(session, auditConfiguration, element, elementType,
					propertyName, i, auditObject, auditEvent);
		}

		if (!insertAuditObject.getAuditObjectProperties().isEmpty()) {
			auditEvents.add(insertAuditEvent);
			insertAuditEvent.setAuditType(auditType);
		}

		if (!updateAuditObject.getAuditObjectProperties().isEmpty()) {
			auditEvents.add(updateAuditEvent);
			updateAuditEvent.setAuditType(auditType);
		}

		if (deletesIterator != null) {
			if (deletesIterator.hasNext()) {
				AuditEvent deleteAuditEvent = new AuditEvent();
				deleteAuditEvent.setType(AuditEvent.REMOVE_AUDIT_EVENT_TYPE);
				deleteAuditEvent.setEntityId(id == null ? null : id.toString());

				auditEvents.add(deleteAuditEvent);
				deleteAuditEvent.setAuditType(auditType);

				EntityAuditObject deleteAuditObject = new EntityAuditObject();
				deleteAuditObject.setAuditEvent(deleteAuditEvent);
				deleteAuditObject.setAuditType(auditType);
				deleteAuditObject.setTargetEntityId(id == null ? null : id
						.toString());
				deleteAuditEvent.getAuditObjects().add(deleteAuditObject);

				long i = 0;
				while (deletesIterator.hasNext()) {
					long index = i;

					Object element = deletesIterator.next();
					if (snapshot instanceof List) {
						// try to get the actual index in case of a List
						index = ((List) snapshot).indexOf(element);
						if (index == -1) {
							// we did not find the element - use the initial
							// value
							index = i;
						}
					}
					processElement(session, auditConfiguration, element,
							elementType, propertyName, index,
							deleteAuditObject, deleteAuditEvent);

					i++;
				}
			}
		}
	}

	private void processElement(Session session,
			AuditConfiguration auditConfiguration, Object element,
			Type elementType, String propertyName, long index,
			EntityAuditObject auditObject, AuditEvent auditEvent) {

		if (elementType.isEntityType()) {
			Serializable entityElementId = null;

			if (element != null) {
				id = session.getSessionFactory().getClassMetadata(
						((EntityType) elementType).getAssociatedEntityName())
						.getIdentifier(element, session.getEntityMode());
			}

			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entity.getClass().getName(), propertyName);

			EntityObjectProperty property = new EntityObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			property.setTargetEntityId(auditConfiguration.getExtensionManager()
					.getPropertyValueConverter().toString(id));
			auditObject.getAuditObjectProperties().add(property);
		} else if (elementType.isComponentType()) {
			AbstractComponentType componentType = (AbstractComponentType) elementType;

			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entity.getClass().getName(), propertyName);

			ComponentObjectProperty property = new ComponentObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			ComponentAuditObject targetComponentAuditObject = null;

			if (element != null) {
				targetComponentAuditObject = new ComponentAuditObject();
				targetComponentAuditObject.setAuditEvent(auditEvent);
				targetComponentAuditObject.setParentAuditObject(auditObject);
				AuditType auditComponentType = HibernateAudit.getAuditType(
						session, element.getClass().getName());
				targetComponentAuditObject.setAuditType(auditComponentType);

				for (int j = 0; j < componentType.getPropertyNames().length; j++) {
					String componentPropertyName = componentType
							.getPropertyNames()[j];

					Type componentPropertyType = componentType.getSubtypes()[j];
					Object componentPropertyValue = componentType
							.getPropertyValue(element, j,
									(SessionImplementor) session);

					processProperty(session, auditConfiguration, auditEvent,
							element, componentPropertyName,
							componentPropertyValue, componentPropertyType,
							targetComponentAuditObject);
				}
			}
			property.setTargetComponentAuditObject(targetComponentAuditObject);
			auditObject.getAuditObjectProperties().add(property);
		} else if (elementType.isCollectionType()) {
			// collection of collections
		} else {
			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entity.getClass().getName(), propertyName);

			SimpleObjectProperty property = new SimpleObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			property.setValue(auditConfiguration.getExtensionManager()
					.getPropertyValueConverter().toString(element));
			auditObject.getAuditObjectProperties().add(property);
		}
	}
}
