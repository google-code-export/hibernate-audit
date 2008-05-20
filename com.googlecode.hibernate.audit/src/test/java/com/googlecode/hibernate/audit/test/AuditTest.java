package com.googlecode.hibernate.audit.test;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.googlecode.hibernate.audit.model.transaction.AuditTransaction;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionComponentRecord;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionEntityRecord;
import com.googlecode.hibernate.audit.model.transaction.record.AuditTransactionRecord;
import com.googlecode.hibernate.audit.model.transaction.record.field.AuditTransactionRecordField;
import com.googlecode.hibernate.audit.model.transaction.record.field.AuditTransactionRecordFieldValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.value.AuditValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.value.ComponentReferenceAuditValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.value.EntityReferenceAuditValue;
import com.googlecode.hibernate.audit.model.transaction.record.field.value.SimpleAuditValue;
import com.googlecode.hibernate.audit.util.HibernateUtils;

public abstract class AuditTest /* implements Synchronization */{
	/*
	 * private static final String IDENT_DELIMITER = "\t";
	 */private Logger LOG = Logger.getLogger(AuditTest.class);

	/*
	 * public void afterCompletion(int status) { if (status != 0) {
	 * logTransactionModifications(LOG); } else { if (LOG.isDebugEnabled()) {
	 * LOG.debug("Transaction rolledback"); } } }
	 * 
	 * public void beforeCompletion() { }
	 */
	@BeforeSuite
	public void doBeforeTests() {
		DOMConfigurator configurator = new DOMConfigurator();
		InputStream is = null;
		try {
			is = AuditTest.class.getResourceAsStream("/log4j.xml");
			assertNotNull(is);
			configurator.doConfigure(is, new Hierarchy(new RootLogger(
					Level.DEBUG)));
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException ignored) {
			}
		}
	}

	@AfterSuite(alwaysRun = true)
	public void doAfterTests() {
		HibernateUtils.closeCurrentSession();
	}

	/*
	 * protected void logTransactionModifications(Logger log) {
	 * logTransactionModifications(log, 1); }
	 * 
	 * protected void logTransactionModifications(Logger log, int
	 * transactionHistoryLevel) { logTransactionModifications(log,
	 * transactionHistoryLevel, null); }
	 * 
	 * protected void logTransactionModifications(Logger log, int
	 * transactionHistoryLevel, Object entity) { if (log.isDebugEnabled()) { log
	 * .debug("Begin transaction modification dump [transactionHistoryLevel=" +
	 * transactionHistoryLevel + ", entity=" + entity + "]"); try { Session
	 * session = HibernateUtils.getCurrentSession(); Query transactionQuery =
	 * session.createQuery("from " + AuditTransaction.class.getName() + " order
	 * by id desc");
	 * 
	 * List<AuditTransaction> transactions = transactionQuery.list(); Iterator<AuditTransaction>
	 * iterator = transactions.iterator(); int stopLevel =
	 * transactionHistoryLevel; while (iterator.hasNext() && stopLevel > 0) {
	 * AuditTransaction transaction = iterator.next();
	 * 
	 * log.debug("Transaction [id=" + transaction.getId() + ", time=" +
	 * transaction.getTransactionTime() + "]"); Query query = null; if (entity !=
	 * null) { query = session .createQuery("from " +
	 * AuditObject.class.getName() + " where auditClass.name = :className and
	 * auditTransaction.id = :transactionId and" + " audittedEntityId =
	 * :audittedEntityId"); query.setParameter("className", getEntityClass()
	 * .getName()); query .setParameter("transactionId", transaction .getId());
	 * query.setParameter("audittedEntityId", String .valueOf(entity.getId())); }
	 * else { query = session .createQuery("from " + AuditObject.class.getName() + "
	 * where auditClass.name = :className and auditTransaction.id =
	 * :transactionId"); query.setParameter("className", getEntityClass()
	 * .getName()); query .setParameter("transactionId", transaction .getId());
	 *  } List<AuditObject> auditObjectList = query.list();
	 * 
	 * for (AuditObject auditObject : auditObjectList) { debugAuditObject(log,
	 * auditObject, IDENT_DELIMITER); } stopLevel--; } } finally { log
	 * .debug("End transaction modification dump [transactionHistoryLevel=" +
	 * transactionHistoryLevel + ", entity=" + entity + "]"); } } }
	 * 
	 * private void debugAuditObject(Logger log, AuditObject auditObject, String
	 * indent) { log.debug(indent + auditObject.getAuditClass().getName() + "[" +
	 * auditObject.getAudittedEntityId() + "]");
	 * 
	 * for (AuditProperty property : auditObject.getAuditProperties()) { for
	 * (AuditObjectPropertyValue value : property.getValues()) {
	 * Hibernate.initialize(value);
	 * 
	 * if (value instanceof AuditSimplePropertyValue) { log.debug(indent +
	 * property.getAuditClassProperty().getName() + "=" +
	 * ((AuditSimplePropertyValue) value).getValue());
	 * 
	 * Session session = HibernateUtils.getCurrentSession(); Query
	 * previousValueQuery = session .createQuery("from " +
	 * AuditSimplePropertyValue.class.getName() + " where
	 * auditObjectProperty.auditClassProperty = :classProperty and " + "
	 * auditObjectProperty.auditObject.audittedEntityId = :audittedEntityId and " + "
	 * auditObjectProperty.auditObject.auditTransaction.id < :transactionId
	 * order by auditObjectProperty.auditObject.auditTransaction.id desc");
	 * 
	 * previousValueQuery.setParameter("classProperty", property
	 * .getAuditClassProperty());
	 * previousValueQuery.setParameter("audittedEntityId",
	 * property.getAuditObject().getAudittedEntityId());
	 * previousValueQuery.setParameter("transactionId", property
	 * .getAuditObject().getAuditTransaction().getId()); List<AuditSimplePropertyValue>
	 * previousValues = previousValueQuery .list(); for
	 * (AuditSimplePropertyValue previousValue : previousValues) {
	 * log.debug(indent + indent + "oldValue=" + previousValue.getValue() + "," +
	 * "transactionId=" + previousValue.getAuditObjectProperty()
	 * .getAuditObject().getAuditTransaction() .getId()); } } else if (value
	 * instanceof AuditComponentReferencePropertyValue) { AuditObject component =
	 * ((AuditComponentReferencePropertyValue) value) .getAuditObject();
	 * log.debug(indent + property.getAuditClassProperty().getName() + "[" +
	 * component.getAudittedEntityId() + "," + component.getId() + "]");
	 * debugAuditObject(log, component, indent + IDENT_DELIMITER); } else if
	 * (value instanceof AuditEntityRefPropertyValue) { Serializable entityId =
	 * ((AuditEntityRefPropertyValue) value) .getEntityRefId(); log.debug(indent +
	 * property.getAuditClassProperty().getName() + "[" + entityId + "," +
	 * ((AuditEntityRefPropertyValue) value).getId() + "]"); } } } }
	 * 
	 * protected Transaction getTransaction(Session session) { Transaction
	 * transaction = session.beginTransaction();
	 * transaction.registerSynchronization(this); return transaction; }
	 */
	protected Session getSession() {
		return HibernateUtils.getCurrentSession();
	}

	protected void dumpAuditLog() {
		Session session = HibernateUtils.getCurrentSession();
		try {

			Query getAuditTransactionQuery = session.createQuery("from "
					+ AuditTransaction.class.getName()
					+ " order by transactionTime desc");
			List<AuditTransaction> auditTransactions = getAuditTransactionQuery
					.list();
			for (AuditTransaction auditTransaction : auditTransactions) {
				LOG.debug("AuditTransaction[id=" + auditTransaction.getId()
						+ ",time=" + auditTransaction.getTransactionTime()
						+ "] {");
				List<AuditTransactionEntityRecord> entities = getEntityObjects(session,
						auditTransaction);
				for (AuditTransactionEntityRecord entity : entities) {
					dumpAuditEntity("\t", '\t', entity);
					LOG.debug("}");
				}
			}
		} finally {
			session.close();
		}
	}

	private void dumpAuditEntity(String indent, char indentChar, AuditTransactionEntityRecord entity) {
		LOG.debug(indent + "AuditEntityObject[id=" + entity.getId()
				+ ",className=" + entity.getAuditClass().getName()
				+ ",audittedEntityId=" + entity.getAudittedEntityId()
				+ ",operation=" + entity.getOperation() + "] {");
		dumpAuditObjectProperties(indent, indentChar, entity);
		LOG.debug(indent + "}");
	}

	private void dumpAuditComponent(String indent, char indentChar, AuditTransactionComponentRecord component) {
		LOG.debug(indent + "AuditComponentObject[id=" + component.getId()
				+ ",className=" + component.getAuditClass().getName()
				+ ",audittedEntityId=" + component.getAudittedEntityId()
				+ ",operation=" + component.getOperation() + "] {");
		dumpAuditObjectProperties(indent, indentChar, component);
		LOG.debug(indent + "}");
	}
	
	private void dumpAuditObjectProperties(String indent, char indentChar,
			AuditTransactionRecord auditObject) {
		for (AuditTransactionRecordField auditProperty : getRecordFields(auditObject)) {
			LOG.debug(indent + indentChar + "AuditTransactionRecordField[id="
					+ auditProperty.getId() + ",propertyName="
					+ auditProperty.getAuditClassProperty().getName() 
					+ ",operation=" + auditProperty.getOperation() 
					+ "] {");

			for (AuditTransactionRecordFieldValue recordFieldValue : getRecordFieldValues(auditProperty)) {
				AuditValue value = recordFieldValue.getValue();
				if (value.isOfType(SimpleAuditValue.class)) {
					LOG.debug(indent + indentChar + ((SimpleAuditValue)value).getValue());
				} else if (value.isOfType(ComponentReferenceAuditValue.class)) {
					LOG.debug(indent + indentChar + "component{");
					dumpAuditComponent(indent + indentChar, indentChar, ((ComponentReferenceAuditValue)value).getComponentAuditObject());
					LOG.debug(indent + indentChar + "}");
				} else if (value.isOfType(EntityReferenceAuditValue.class)) {
					LOG.debug(indent + indentChar + "entityRef[" + ((EntityReferenceAuditValue)value).getEntityReferenceId() + "]");
				} else {
					LOG.debug(indent + indentChar + "UNKNOWN VALUE");
				}
				
				LOG.debug(indent + indentChar + ",");
			}

			LOG.debug(indent + indentChar + "}");
		}
	}

	private List<AuditTransactionEntityRecord> getEntityObjects(Session session,
			AuditTransaction transaction) {
		Query getEntityObjectsQuery = session.createQuery("from "
				+ AuditTransactionEntityRecord.class.getName() + " where "
				+ "auditTransaction = :auditTransaction");

		getEntityObjectsQuery.setParameter("auditTransaction", transaction);
		return getEntityObjectsQuery.list();
	}
	/*
	 * protected abstract Class getEntityClass();
	 */
	
	private List<AuditTransactionRecordField> getRecordFields(AuditTransactionRecord record) {
		Session session = HibernateUtils.getCurrentSession();
		Query query = session.createQuery("from " + AuditTransactionRecordField.class.getName() + " where " +
				"auditTransactionRecord = :auditTransactionRecord");
		query.setParameter("auditTransactionRecord", record);
		return query.list();
	}
	
	private List<AuditTransactionRecordFieldValue> getRecordFieldValues(AuditTransactionRecordField recordField) {
		Session session = HibernateUtils.getCurrentSession();
		Query query = session.createQuery("from " + AuditTransactionRecordFieldValue.class.getName() + " where " +
				"recordField = :recordField");
		query.setParameter("recordField", recordField);
		return query.list();
	}
}
