package com.googlecode.hibernate.audit.event;

import org.hibernate.StatelessSession;
import org.hibernate.event.PreCollectionRemoveEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.googlecode.hibernate.audit.model.AuditOperation;

@SuppressWarnings("serial")
public class AuditPreCollectionRemoveEventListener extends
		AuditAbstractEventListener {
/*	private Logger LOG = LoggerFactory
			.getLogger(AuditPreCollectionRemoveEventListener.class);
*/
	protected StatelessSession openStatelessSession(Object object) {
		PreCollectionRemoveEvent event = (PreCollectionRemoveEvent) object;
		return event.getSession().getFactory().openStatelessSession(
				event.getSession().connection());

	}

	protected Object getEntity(Object object) {
		PreCollectionRemoveEvent event = (PreCollectionRemoveEvent) object;
		return event.getCollection().getOwner();
	}

	protected EntityPersister getEntityPersister(Object object) {
		PreCollectionRemoveEvent event = (PreCollectionRemoveEvent) object;
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
		PreCollectionRemoveEvent event = (PreCollectionRemoveEvent) object;
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

		Object propertyValue = persister.getPropertyValue(entity, propertyName,
				entityMode);

		AuditEntityProperty auditEntityProperty = createAuditEntityProperty(
				session,
				auditEntity,
				entityName,
				propertyName,
				propertyValue == null ? AuditEntityPropertyOperation.SET_NULL_COLLECTION
						: AuditEntityPropertyOperation.SET_EMPTY_COLLECTION);

		if (LOG.isDebugEnabled()) {
			LOG.debug("Add audit entity property with id "
					+ auditEntityProperty.getId());
		}
	}
*/}
