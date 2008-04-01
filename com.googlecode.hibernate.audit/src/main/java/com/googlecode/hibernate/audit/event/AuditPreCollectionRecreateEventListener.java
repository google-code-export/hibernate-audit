package com.googlecode.hibernate.audit.event;

import org.hibernate.StatelessSession;
import org.hibernate.event.PreCollectionRecreateEvent;
import org.hibernate.event.PreCollectionRecreateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import com.googlecode.hibernate.audit.model.AuditOperation;

@SuppressWarnings("serial")
public class AuditPreCollectionRecreateEventListener extends
		AuditAbstractEventListener implements
		PreCollectionRecreateEventListener {

	/*
	 * private Logger LOG = LoggerFactory
	 * .getLogger(AuditPreCollectionRecreateEventListener.class);
	 */
	protected StatelessSession openStatelessSession(Object object) {
		PreCollectionRecreateEvent event = (PreCollectionRecreateEvent) object;
		return event.getSession().getFactory().openStatelessSession(
				event.getSession().connection());
	}

	protected Object getEntity(Object object) {
		PreCollectionRecreateEvent event = (PreCollectionRecreateEvent) object;
		return event.getCollection().getOwner();
	}

	protected EntityPersister getEntityPersister(Object object) {
		PreCollectionRecreateEvent event = (PreCollectionRecreateEvent) object;
		Object entity = event.getCollection().getOwner();
		return event.getSession().getEntityPersister(
				event.getSession().bestGuessEntityName(entity), entity);
	}

	protected AuditOperation getAuditEntityOperation(Object object) {
		return AuditOperation.UPDATE;
	}

/*	protected void doAuditEntityProperties(StatelessSession session,
			Object object, AuditTransaction auditTransaction,
			AuditObject auditEntity) {
		PreCollectionRecreateEvent event = (PreCollectionRecreateEvent) object;
		Object entity = getEntity(object);
		String entityName = entity.getClass().getName().toString();
		EntityPersister persister = getEntityPersister(object);
		EntityMode entityMode = persister.guessEntityMode(entity);

		CollectionPersister collectionPersister = event.getSession()
				.getPersistenceContext().getCollectionEntry(
						event.getCollection()).getCurrentPersister();

		String role = collectionPersister.getCollectionMetadata().getRole();

		String propertyName = role.substring(role.lastIndexOf('.') != -1 ? role
				.lastIndexOf('.') + 1 : 0, role.length());

		AuditEntityProperty auditEntityProperty = createAuditEntityProperty(
				session, auditEntity, entityName, propertyName,
				AuditEntityPropertyOperation.INSERT);

		Type elementType = collectionPersister.getCollectionMetadata()
				.getElementType();

		
		 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit entity property with
		 * id " + auditEntityProperty.getId()); }
		 
		if (elementType.isEntityType()) {
			@SuppressWarnings("unchecked")
			Iterator<? extends Object> iterator = event.getCollection()
					.entries(collectionPersister);

			while (iterator.hasNext()) {
				Object value = iterator.next();
				EntityPersister propertyPersister = event.getSession()
						.getEntityPersister(
								event.getSession().bestGuessEntityName(value),
								value);

				Serializable propertyValueEntityId = propertyPersister
						.getIdentifier(value, entityMode);

				AuditEntityPropertyValue auditEntityPropertyValue = createAuditEntityPropertyValue(
						session,
						propertyValueEntityId,
						AuditEntityPropertyValueOperation.COLLECTION_ADD_ENTITY_REF,
						auditEntityProperty);

				
				 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit entity
				 * property value with id " + auditEntityPropertyValue.getId()); }
				 
			}
		} else if (elementType.isComponentType()) {
			//:TODO 
		} else if (elementType.isCollectionType()) {
			// collection of collection
			// :TODO check if this is possible
		} else {
			Iterator<? extends Object> iterator = event.getCollection()
					.entries(collectionPersister);

			while (iterator.hasNext()) {
				Object value = iterator.next();

				AuditEntityPropertyValue auditEntityPropertyValue = createAuditEntityPropertyValue(
						session, value,
						AuditEntityPropertyValueOperation.COLLECTION_ADD,
						auditEntityProperty);
			}
		}
	}
*/}
