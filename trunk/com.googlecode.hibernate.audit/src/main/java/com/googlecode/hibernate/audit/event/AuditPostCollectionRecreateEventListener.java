package com.googlecode.hibernate.audit.event;

import java.io.Serializable;
import java.util.Iterator;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.hibernate.EntityMode;
import org.hibernate.StatelessSession;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostCollectionRecreateEvent;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.model_obsolete.AuditOperation;
import com.googlecode.hibernate.audit.model_obsolete.transaction.AuditTransactionObsolete;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.AuditTransactionRecord;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordField;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordFieldEntityReferenceValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.EntityReferenceAuditValue;

@SuppressWarnings("serial")
public class AuditPostCollectionRecreateEventListener extends
		AuditAbstractEventListener {

	private Logger LOG = Logger
			.getLogger(AuditPostCollectionRecreateEventListener.class);

	@Override
	protected StatelessSession openStatelessSession(AbstractEvent object) {
		PostCollectionRecreateEvent event = (PostCollectionRecreateEvent) object;
		return event.getSession().getFactory().openStatelessSession(
				event.getSession().connection());
	}

	@Override
	protected Object getEntity(AbstractEvent object) {
		PostCollectionRecreateEvent event = (PostCollectionRecreateEvent) object;
		return event.getCollection().getOwner();
	}

	@Override
	protected EntityPersister getEntityPersister(AbstractEvent object) {
		PostCollectionRecreateEvent event = (PostCollectionRecreateEvent) object;
		Object entity = event.getCollection().getOwner();
		return event.getSession().getEntityPersister(
				event.getSession().bestGuessEntityName(entity), entity);
	}

	@Override
	protected AuditOperation getAuditEntityOperation(AbstractEvent object) {
		return AuditOperation.INSERT;
	}

	@Override
	protected void doAuditEntityProperties(StatelessSession session,
			AbstractEvent eventObject, AuditTransactionObsolete auditTransaction,
			AuditTransactionRecord auditEntity) {
		if (LOG.isEnabledFor(Level.DEBUG)) {
			LOG.debug("Invoke PostCollectionRecreateEvent listener");
		}

		PostCollectionRecreateEvent event = (PostCollectionRecreateEvent) eventObject;
		Object entity = getEntity(eventObject);
		String entityName = entity.getClass().getName().toString();
		EntityPersister persister = getEntityPersister(eventObject);
		EntityMode entityMode = event.getSession().getEntityMode();

		CollectionPersister collectionPersister = event.getSession()
				.getPersistenceContext().getCollectionEntry(
						event.getCollection()).getCurrentPersister();

		String role = collectionPersister.getCollectionMetadata().getRole();

		String propertyName = role.substring(role.lastIndexOf('.') != -1 ? role
				.lastIndexOf('.') + 1 : 0, role.length());

		if (isAuditSuppressed(entity.getClass(), propertyName)) {
			return;
		}

		Type elementType = collectionPersister.getCollectionMetadata()
				.getElementType();

		@SuppressWarnings("unchecked")
		Iterator<? extends Object> iterator = event.getCollection().entries(
				collectionPersister);

		if (elementType.isEntityType()) {
			AuditTransactionRecordField entityRefProperty = new AuditTransactionRecordField();
			entityRefProperty.setAuditClassProperty(getOrCreateAuditProperty(
					session, entityName, propertyName));
			entityRefProperty.setOperation(AuditOperation.INSERT);
			//auditEntity.addAuditTransactionRecordField(entityRefProperty);
			entityRefProperty.setAuditTransactionRecord(auditEntity);
			session.insert(entityRefProperty);

			int index = 0;
			while (iterator.hasNext()) {
				Object value = iterator.next();

				createCollectionEntityRef(event, session, entityName, entityMode,
						auditEntity, propertyName, value, index++,
						entityRefProperty);
			}
			//session.insert(entityRefProperty);
		} else if (elementType.isComponentType()) {

		} else if (elementType.isCollectionType()) {
			// collection of collections
		} else {

		}
	}

	private void createCollectionEntityRef(PostCollectionRecreateEvent event,
			StatelessSession session, String entityName, EntityMode entityMode,
			AuditTransactionRecord auditEntity, String propertyName, Object propertyValue,
			int index, AuditTransactionRecordField collectionProperty) {
		EntityPersister propertyPersister = event.getSession()
				.getEntityPersister(
						event.getSession().bestGuessEntityName(propertyValue),
						propertyValue);

		Serializable entityRefId = propertyPersister.getIdentifier(
				propertyValue, entityMode);

		EntityReferenceAuditValue elementValue = new EntityReferenceAuditValue();
		elementValue.setEntityReferenceId(String.valueOf(entityRefId));
		
		AuditTransactionRecordFieldEntityReferenceValue value = new AuditTransactionRecordFieldEntityReferenceValue();
		value.setValue(elementValue);
		
		//collectionProperty.addValue(value);
		value.setRecordField(collectionProperty);
		session.insert(value);
	}

	/*
	 * protected void doAuditEntityProperties(StatelessSession session, Object
	 * transaction, AuditTransaction auditTransaction, AuditObject auditEntity) {
	 * PostCollectionRecreateEvent event = (PostCollectionRecreateEvent) transaction;
	 * Object entity = getEntity(transaction); String entityName =
	 * entity.getClass().getName().toString(); EntityPersister persister =
	 * getEntityPersister(transaction); EntityMode entityMode =
	 * persister.guessEntityMode(entity);
	 * 
	 * CollectionPersister collectionPersister = event.getSession()
	 * .getPersistenceContext().getCollectionEntry(
	 * event.getCollection()).getCurrentPersister();
	 * 
	 * String role = collectionPersister.getCollectionMetadata().getRole();
	 * 
	 * String propertyName = role.substring(role.lastIndexOf('.') != -1 ? role
	 * .lastIndexOf('.') + 1 : 0, role.length());
	 * 
	 * AuditEntityProperty auditEntityProperty = createAuditEntityProperty(
	 * session, auditEntity, entityName, propertyName,
	 * AuditEntityPropertyOperation.INSERT);
	 * 
	 * Type elementType = collectionPersister.getCollectionMetadata()
	 * .getElementType();
	 * 
	 * 
	 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit entity property with id " +
	 * auditEntityProperty.getId()); }
	 * 
	 * if (elementType.isEntityType()) { @SuppressWarnings("unchecked") Iterator<?
	 * extends Object> iterator = event.getCollection()
	 * .entries(collectionPersister);
	 * 
	 * while (iterator.hasNext()) { Object value = iterator.next();
	 * EntityPersister propertyPersister = event.getSession()
	 * .getEntityPersister( event.getSession().bestGuessEntityName(value),
	 * value);
	 * 
	 * Serializable propertyValueEntityId = propertyPersister
	 * .getIdentifier(value, entityMode);
	 * 
	 * AuditEntityPropertyValue auditEntityPropertyValue =
	 * createAuditEntityPropertyValue( session, propertyValueEntityId,
	 * AuditEntityPropertyValueOperation.COLLECTION_ADD_ENTITY_REF,
	 * auditEntityProperty);
	 * 
	 * 
	 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit entity property value
	 * with id " + auditEntityPropertyValue.getId()); } } } else if
	 * (elementType.isComponentType()) { //:TODO } else if
	 * (elementType.isCollectionType()) { // collection of collection // :TODO
	 * check if this is possible } else { Iterator<? extends Object> iterator =
	 * event.getCollection() .entries(collectionPersister);
	 * 
	 * while (iterator.hasNext()) { Object value = iterator.next();
	 * 
	 * AuditEntityPropertyValue auditEntityPropertyValue =
	 * createAuditEntityPropertyValue( session, value,
	 * AuditEntityPropertyValueOperation.COLLECTION_ADD, auditEntityProperty); } } }
	 */
}
