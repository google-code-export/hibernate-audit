package com.googlecode.hibernate.audit.event;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.WeakHashMap;

import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreCollectionRecreateEvent;
import org.hibernate.event.PreCollectionRecreateEventListener;
import org.hibernate.event.PreCollectionRemoveEvent;
import org.hibernate.event.PreCollectionRemoveEventListener;
import org.hibernate.event.PreCollectionUpdateEvent;
import org.hibernate.event.PreCollectionUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;

import com.googlecode.hibernate.audit.model.AuditClass;
import com.googlecode.hibernate.audit.model.AuditClassProperty;
import com.googlecode.hibernate.audit.model.AuditObject;
import com.googlecode.hibernate.audit.model.AuditOperation;
import com.googlecode.hibernate.audit.model.AuditTransaction;

public abstract class AuditAbstractEventListener implements
		PreCollectionRecreateEventListener, PreCollectionRemoveEventListener,
		PreCollectionUpdateEventListener, /*PreDeleteEventListener,*/
		PostDeleteEventListener, /*PreInsertEventListener,*/
		PostInsertEventListener, /*PreUpdateEventListener,*/
		PostUpdateEventListener {
	
	private DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:MM:ss");

	/*
	 * private Logger LOG = LoggerFactory
	 * .getLogger(AuditAbstractEventListener.class);
	 */private static WeakHashMap<Transaction, AuditTransaction> hibernateTransactionToAuditTransaction = new WeakHashMap<Transaction, AuditTransaction>();

	public void onPreRemoveCollection(PreCollectionRemoveEvent event) {
		processEvent(event, event.getSession());
	}

/*	public boolean onPreInsert(PreInsertEvent event) {
		processEvent(event);
		return false;
	}
*/
	public void onPostInsert(PostInsertEvent event) {
		processEvent(event, event.getSession());
	}

/*	public boolean onPreUpdate(PreUpdateEvent event) {
		processEvent(event);
		return false;
	}
*/
	public void onPostUpdate(PostUpdateEvent event) {
		processEvent(event, event.getSession());
	}

/*	public boolean onPreDelete(PreDeleteEvent event) {
		processEvent(event);
		return false;
	}
*/
	public void onPostDelete(PostDeleteEvent event) {
		processEvent(event, event.getSession());
	}

	public void onPreRecreateCollection(PreCollectionRecreateEvent event) {
		processEvent(event, event.getSession());
	}

	public void onPreUpdateCollection(PreCollectionUpdateEvent event) {
		processEvent(event, event.getSession());
	}

	protected void processEvent(Object event, Session originalSession) {
		StatelessSession session = null;
		try {
			session = openStatelessSession(event);
			doAuditEvent(session, event, originalSession);
			// the flush method on StatelessSession is empty and
			// we are not invoking the commit method on the
			// session which invokes getBatcher().excuteBatch();
			((SessionImplementor) session).getBatcher().executeBatch();

		} finally {
			if (session != null) {
				session.close();
			}
		}

	}

	protected abstract StatelessSession openStatelessSession(Object event);

	protected abstract Object getEntity(Object event);

	protected abstract AuditOperation getAuditEntityOperation(Object event);

	protected abstract EntityPersister getEntityPersister(Object event);

	protected void doAuditEvent(StatelessSession session, Object event, Session originalSession) {
		AuditTransaction auditTransaction = doAuditTransaction(session, event, originalSession);
		AuditObject auditEntity = doAuditEntity(session, event,
				auditTransaction);
		doAuditEntityProperties(session, event, auditTransaction, auditEntity);
	}

	protected AuditTransaction doAuditTransaction(StatelessSession session,
			Object event, Session originalSession) {
		// TODO:get actorId from somewhere
		String actorId = null;
		AuditTransaction auditTransaction = getOrCreateAuditTransaction(
				session, actorId, originalSession);
		return auditTransaction;
	}

	protected AuditObject doAuditEntity(StatelessSession session, Object event,
			AuditTransaction auditTransaction) {
		Serializable entityId = null;
		Object entity = getEntity(event);
		String entityName = entity.getClass().getName().toString();

		EntityPersister persister = getEntityPersister(event);
		EntityMode entityMode = persister.guessEntityMode(entity);

		if (persister.hasIdentifierProperty()) {
			entityId = persister.getIdentifier(entity, entityMode);
		}
		// add audit entity
		AuditObject auditEntity = createAuditEntity(session, entityId,
				entityName, getAuditEntityOperation(event), auditTransaction);

		/*
		 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit entity with id " +
		 * auditEntity.getId() + " for object entity '" + entityName + "' with
		 * id " + entityId); }
		 */return auditEntity;
	}

	protected void doAuditEntityProperties(StatelessSession session,
			Object event, AuditTransaction auditTransaction,
			AuditObject auditEntity) {
	}

	protected synchronized AuditTransaction getOrCreateAuditTransaction(
			StatelessSession session, String actorId, Session originalSession) {
		AuditTransaction auditTransaction = hibernateTransactionToAuditTransaction
				.get(originalSession.getTransaction());

		if (auditTransaction == null) {
			auditTransaction = new AuditTransaction();
			auditTransaction.setTransactionTime(new Date());
			auditTransaction.setUser(actorId);
			session.insert(auditTransaction);
			/*
			 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit transaction with
			 * id " + auditTransaction.getId() + " for user " + actorId); }
			 */hibernateTransactionToAuditTransaction.put(originalSession
					.getTransaction(), auditTransaction);
		}
		return auditTransaction;
	}

	protected AuditClass getOrCreateAuditClass(StatelessSession session,
			String entityName) {
		Query auditClassQuery = session.createQuery(
				"from AuditClass where name = :entityName").setMaxResults(1);
		auditClassQuery.setString("entityName", entityName);

		@SuppressWarnings("unchecked")
		Iterator<AuditClass> auditClassQueryIterator = auditClassQuery.list()
				.iterator();
		if (auditClassQueryIterator.hasNext()) {
			AuditClass auditClass = auditClassQueryIterator.next();
			return auditClass;
		} else {
			// create the entity
			AuditClass auditClass = new AuditClass();
			auditClass.setName(entityName);
			session.insert(auditClass);
			/*
			 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit class '" +
			 * entityName + "' with id " + auditClass.getId()); }
			 */return auditClass;
		}
	}

	protected AuditClassProperty getOrCreateAuditProperty(StatelessSession session,
			String entityName, String propertyName) {
		Query auditPropertyQuery = session
				.createQuery(
						"from " + AuditClassProperty.class.getName() + " auditProperty where auditProperty.name = :propertyName and auditProperty.auditClass.name = :entityName")
				.setMaxResults(1);
		auditPropertyQuery.setString("propertyName", propertyName);
		auditPropertyQuery.setString("entityName", entityName);

		@SuppressWarnings("unchecked")
		Iterator<AuditClassProperty> auditPropertyQueryIterator = auditPropertyQuery
				.list().iterator();
		if (auditPropertyQueryIterator.hasNext()) {
			return auditPropertyQueryIterator.next();
		} else {
			AuditClass auditClass = getOrCreateAuditClass(session, entityName);
			AuditClassProperty auditProperty = new AuditClassProperty();
			auditProperty.setAuditClass(auditClass);
			auditProperty.setName(propertyName);
			session.insert(auditProperty);
			/*
			 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit property '" +
			 * propertyName + "' with id " + auditProperty.getId() + " for class " +
			 * auditClass.getName()); }
			 */return auditProperty;
		}
	}

	protected AuditObject createAuditEntity(StatelessSession session,
			Serializable audittedEntityId, String entityName,
			AuditOperation operation, AuditTransaction auditTransaction) {
		AuditObject auditEntity = new AuditObject();
		
		auditEntity.setAuditTransaction(auditTransaction);
		auditEntity.setAudittedEntityId(audittedEntityId != null ? audittedEntityId.toString() : null);
		auditEntity.setAuditClass(getOrCreateAuditClass(session, entityName));
		auditEntity.setOperation(operation);
		session.insert(auditEntity);
		return auditEntity;
	}
	
/*
	protected AuditEntityProperty createAuditEntityProperty(
			StatelessSession session, AuditObject auditEntity,
			String entityName, String propertyName,
			AuditEntityPropertyOperation operation) {
		AuditEntityProperty auditEntityProperty = new AuditEntityProperty();
		auditEntityProperty.setAuditEntity(auditEntity);
		auditEntityProperty.setAuditProperty(getOrCreateAuditProperty(session,
				entityName, propertyName));
		auditEntityProperty.setOperation(operation);
		session.insert(auditEntityProperty);
		return auditEntityProperty;
	}

	protected AuditEntityPropertyValue createAuditEntityPropertyValue(
			StatelessSession session, Object propertyValue,
			AuditEntityPropertyValueOperation operation,
			AuditEntityProperty auditEntityProperty) {
		AuditEntityPropertyValue auditEntityPropertyValue = new AuditEntityPropertyValue();
		auditEntityPropertyValue.setAuditEntityProperty(auditEntityProperty);
		auditEntityPropertyValue.setOperation(operation);
		
		if (propertyValue instanceof Date) {
			auditEntityPropertyValue.setValue(formatter.format(propertyValue));
		} else {
			auditEntityPropertyValue
					.setValue(propertyValue != null ? propertyValue.toString()
							: null);
		}
		session.insert(auditEntityPropertyValue);
		return auditEntityPropertyValue;
	}
*/}
