package com.googlecode.hibernate.audit.event;

import org.hibernate.StatelessSession;
import org.hibernate.event.PreCollectionUpdateEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.googlecode.hibernate.audit.model.AuditOperation;

@SuppressWarnings("serial")
public class AuditPreCollectionUpdateEventListener extends
		AuditAbstractEventListener {
/*	private Logger LOG = LoggerFactory
			.getLogger(AuditPreCollectionUpdateEventListener.class);
*/
	protected StatelessSession openStatelessSession(Object object) {
		PreCollectionUpdateEvent event = (PreCollectionUpdateEvent) object;
		return event.getSession().getFactory().openStatelessSession(
				event.getSession().connection());
	}

	protected Object getEntity(Object object) {
		PreCollectionUpdateEvent event = (PreCollectionUpdateEvent) object;
		return event.getCollection().getOwner();
	}

	protected EntityPersister getEntityPersister(Object object) {
		PreCollectionUpdateEvent event = (PreCollectionUpdateEvent) object;
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
		PreCollectionUpdateEvent event = (PreCollectionUpdateEvent) object;
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
				AuditEntityPropertyOperation.UPDATE);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Add audit entity property with id "
					+ auditEntityProperty.getId());
		}
		
		@SuppressWarnings("unchecked")
		Iterator<Object> deletesIterator = event.getCollection().getDeletes(
				collectionPersister, true);
		while (deletesIterator.hasNext()) {
			Object value = deletesIterator.next();
			EntityPersister propertyPersister = event.getSession()
					.getEntityPersister(
							event.getSession().bestGuessEntityName(value),
							value);

			Serializable propertyValueEntityId = propertyPersister
					.getIdentifier(value, entityMode);

			AuditEntityPropertyValue auditEntityPropertyValue = createAuditEntityPropertyValue(
					session,
					propertyValueEntityId,
					AuditEntityPropertyValueOperation.COLLECTION_REMOVE_ENTITY_REF,
					auditEntityProperty);

			if (LOG.isDebugEnabled()) {
				LOG.debug("Add audit entity property value with id "
						+ auditEntityPropertyValue.getId());
			}
		}

		@SuppressWarnings("unchecked")
		Iterator<Object> entriesIterator = event.getCollection().entries(
				collectionPersister);

		int i = 1;
		while (entriesIterator.hasNext()) {
			Object value = entriesIterator.next();
			if (event.getCollection().needsInserting(value, i,
					collectionPersister.getElementType())) {
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

				if (LOG.isDebugEnabled()) {
					LOG.debug("Add audit entity property value with id "
							+ auditEntityPropertyValue.getId());
				}
			}
			i++;
		}
	}
*/}
