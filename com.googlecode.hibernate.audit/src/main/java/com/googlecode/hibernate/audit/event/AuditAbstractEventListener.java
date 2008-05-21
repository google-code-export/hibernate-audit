package com.googlecode.hibernate.audit.event;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.Iterator;
import java.util.HashMap;

import org.hibernate.EntityMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.StatelessSession;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostCollectionRecreateEvent;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostCollectionRemoveEvent;
import org.hibernate.event.PostCollectionRemoveEventListener;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEvent;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.transaction.JTATransaction;

import com.googlecode.hibernate.audit.annotations.Audit;
import com.googlecode.hibernate.audit.model.AuditOperation;
import com.googlecode.hibernate.audit.model.clazz.AuditClass;
import com.googlecode.hibernate.audit.model.clazz.AuditClassProperty;
import com.googlecode.hibernate.audit.model.transaction.AuditTransaction;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionComponentRecord;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionEntityRecord;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionRecord;

public abstract class AuditAbstractEventListener implements
		PostCollectionRecreateEventListener, PostCollectionRemoveEventListener,
		PostCollectionUpdateEventListener, PostDeleteEventListener,
		PostInsertEventListener, PostUpdateEventListener {

	private static ThreadLocal<HashMap<Object, AuditTransaction>> transactionKeyToAuditTransaction = new ThreadLocal<HashMap<Object, AuditTransaction>>();

	public void onPostInsert(PostInsertEvent event) {
		processEvent(event, event.getSession());
	}

	public void onPostUpdate(PostUpdateEvent event) {
		processEvent(event, event.getSession());
	}

	public void onPostDelete(PostDeleteEvent event) {
		processEvent(event, event.getSession());
	}

	public void onPostRecreateCollection(PostCollectionRecreateEvent event) {
		processEvent(event, event.getSession());
	}

	public void onPostUpdateCollection(PostCollectionUpdateEvent event) {
		processEvent(event, event.getSession());
	}
	
	public void onPostRemoveCollection(PostCollectionRemoveEvent event) {
		processEvent(event, event.getSession());
	}

	protected void processEvent(AbstractEvent event, Session originalSession) {
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

	protected abstract StatelessSession openStatelessSession(AbstractEvent event);

	protected abstract Object getEntity(AbstractEvent event);

	protected abstract AuditOperation getAuditEntityOperation(AbstractEvent event);

	protected abstract EntityPersister getEntityPersister(AbstractEvent event);

	protected void doAuditEvent(StatelessSession session, AbstractEvent event,
			Session originalSession) {
		AuditTransaction auditTransaction = doAuditTransaction(session, event,
				originalSession);
		AuditTransactionRecord	auditEntity = doAuditEntity(session, originalSession, event, auditTransaction);
		doAuditEntityProperties(session, event, auditTransaction, auditEntity);
	}

	protected AuditTransaction doAuditTransaction(StatelessSession session,
			AbstractEvent event, Session originalSession) {
		// TODO:get actorId from somewhere
		String actorId = null;
		return getOrCreateAuditTransaction(session, actorId, originalSession);
	}

	protected AuditTransactionRecord doAuditEntity(StatelessSession session, Session originalSession, AbstractEvent event,
			AuditTransaction auditTransaction) {
		Serializable entityId = null;
		Object entity = getEntity(event);
		String entityName = entity.getClass().getName().toString();

		EntityPersister persister = getEntityPersister(event);
		EntityMode entityMode = persister.guessEntityMode(entity);

		if (persister.hasIdentifierProperty()) {
			entityId = persister.getIdentifier(entity, entityMode);
		}
		AuditOperation operation = getAuditEntityOperation(event);
		AuditClass auditClass = getOrCreateAuditClass(session, entityName);
		
		Query getAuditObjectQuery = originalSession.createQuery("from " + AuditTransactionRecord.class.getName() + " as audit where " +
				"auditTransaction = :auditTransaction and " +
				"auditClass = :auditClass and " +
				"operation = :operation and " +
				"audittedEntityId = :audittedEntityId");
		getAuditObjectQuery.setParameter("auditTransaction", auditTransaction);
		getAuditObjectQuery.setParameter("auditClass", auditClass);
		getAuditObjectQuery.setParameter("operation", operation);
		getAuditObjectQuery.setParameter("audittedEntityId", String.valueOf(entityId));
		
		AuditTransactionRecord existingAuditEntity = (AuditTransactionRecord)getAuditObjectQuery.uniqueResult();
		if (existingAuditEntity != null) {
			return existingAuditEntity;
		}
		
		return  createAuditEntity(session, entityId,
						entityName, operation, auditTransaction, auditClass);
	}

	protected void doAuditEntityProperties(StatelessSession session,
			AbstractEvent event, AuditTransaction auditTransaction,
			AuditTransactionRecord auditEntity) {
	}

	protected synchronized AuditTransaction getOrCreateAuditTransaction(
			StatelessSession session, String actorId, Session originalSession) {
		Object transactionKey = getTransactionKey(originalSession);
		AuditTransaction auditTransaction = null;

		// create HashMap if necessary
		HashMap<Object, AuditTransaction> auditTransactions = transactionKeyToAuditTransaction
				.get();
		if (auditTransactions == null) {
			auditTransactions = new HashMap<Object, AuditTransaction>();
			transactionKeyToAuditTransaction.set(auditTransactions);
		} else {
			auditTransaction = auditTransactions.get(transactionKey);
		}

		if (auditTransaction == null) {
			auditTransaction = new AuditTransaction();
			auditTransaction.setTransactionTime(new Date());
			auditTransaction.setUser(actorId);
			session.insert(auditTransaction);
			/*
			 * if (LOG.isDebugEnabled()) { LOG.debug("Add audit transaction with
			 * id " + auditTransaction.getId() + " for user " + actorId); }
			 */
			// clear any previous states
			auditTransactions.clear();
			auditTransactions.put(transactionKey, auditTransaction);
		}
		return auditTransaction;
	}

	protected AuditClass getOrCreateAuditClass(StatelessSession session,
			String entityName) {
		Query auditClassQuery = session.createQuery(
				"from " +  AuditClass.class.getName() + " where name = :entityName").setMaxResults(1);
		auditClassQuery.setString("entityName", entityName);

		@SuppressWarnings("unchecked")
		Iterator<AuditClass> auditClassQueryIterator = auditClassQuery.list()
				.iterator();
		if (auditClassQueryIterator.hasNext()) {
			return auditClassQueryIterator.next();
		} else {
			// create the entity
			AuditClass auditClass = new AuditClass();
			auditClass.setName(entityName);
			session.insert(auditClass);
			
			return auditClass;
		}
	}

	protected AuditClassProperty getOrCreateAuditProperty(
			StatelessSession session, String entityName, String propertyName) {
		Query auditPropertyQuery = session
				.createQuery(
						"from "
								+ AuditClassProperty.class.getName()
								+ " auditProperty where auditProperty.name = :propertyName and auditProperty.auditClass.name = :entityName")
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

	protected AuditTransactionComponentRecord createAuditComponent(StatelessSession session,
			Serializable audittedEntityId, String entityName,
			AuditOperation operation, AuditTransaction auditTransaction) {
		AuditTransactionComponentRecord auditComponent = new AuditTransactionComponentRecord();
		
		AuditClass auditClass = getOrCreateAuditClass(session, entityName);
		persistAuditObject(session, audittedEntityId, entityName,
				operation, auditTransaction, auditComponent, auditClass);
		
		return auditComponent;
	}
	
	protected AuditTransactionRecord createAuditEntity(StatelessSession session,
			Serializable audittedEntityId, String entityName,
			AuditOperation operation, AuditTransaction auditTransaction, AuditClass auditClass) {
		AuditTransactionEntityRecord auditEntity = new AuditTransactionEntityRecord();

		persistAuditObject(session, audittedEntityId, entityName,
				operation, auditTransaction, auditEntity, auditClass);
		
		return auditEntity;
	}

	private void persistAuditObject(StatelessSession session,
			Serializable audittedEntityId, String entityName,
			AuditOperation operation, AuditTransaction auditTransaction,
			AuditTransactionRecord auditEntity, AuditClass auditClass) {
		//auditTransaction.addAuditObject(auditEntity);
		auditEntity.setAuditTransaction(auditTransaction);
		auditEntity
				.setAudittedEntityId(audittedEntityId != null ? audittedEntityId
						.toString()
						: null);
		auditEntity.setAuditClass(auditClass);
		auditEntity.setOperation(operation);
		session.insert(auditEntity);
	}

	protected Object getTransactionKey(Session originalSession) {
		Object transactionKey = null;
		Transaction transaction = originalSession.getTransaction();
		if (transaction instanceof JTATransaction) {
			// try to get transaction ID from TransactionSynchronizationRegistry
			try {
				// we are going to use reflection so the audit don't depend on
				// JNDI or JTA interfaces
				Class transactionSynchronizationRegistryClass = Class
						.forName(
								"javax.transaction.TransactionSynchronizationRegistry",
								true, AuditAbstractEventListener.class
										.getClassLoader());

				// If we came here, we might be on Java EE 5, since the JTA 1.1
				// API is present.
				Class initialContextClass = Class.forName(
						"javax.naming.InitialContext", true,
						AuditAbstractEventListener.class.getClassLoader());
				Object initialContext = initialContextClass.newInstance();
				Method lookupMethod = initialContextClass.getMethod("lookup",
						String.class);
				Object transactionSynchronizationRegistry = lookupMethod
						.invoke(initialContext,
								"java:comp/TransactionSynchronizationRegistry");
				Method method = transactionSynchronizationRegistryClass
						.getMethod("getTransactionKey");
				transactionKey = method
						.invoke(transactionSynchronizationRegistry);
			} catch (Exception ex) {
			}
		}
		if (transactionKey == null) {
			transactionKey = originalSession.getTransaction();
		}
		return transactionKey;
	}

	protected boolean isAuditSuppressed(Class entityClass, String propertyName) {
		try {
			Field field = entityClass.getDeclaredField(propertyName);
			Audit audit = field.getAnnotation(Audit.class);
			if (audit != null) {
				if (audit.suppressAudit()) {
					return true;
				}
			}
		} catch (NoSuchFieldException ignored) {
		}
		return false;
	}
}