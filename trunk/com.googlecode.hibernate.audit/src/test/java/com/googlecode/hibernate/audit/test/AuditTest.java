package com.googlecode.hibernate.audit.test;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.transaction.Synchronization;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

import com.googlecode.hibernate.audit.model.AuditComponentPropertyValue;
import com.googlecode.hibernate.audit.model.AuditEntityRefPropertyValue;
import com.googlecode.hibernate.audit.model.AuditObject;
import com.googlecode.hibernate.audit.model.AuditObjectProperty;
import com.googlecode.hibernate.audit.model.AuditObjectPropertyValue;
import com.googlecode.hibernate.audit.model.AuditSimplePropertyValue;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.simpleentity.SimpleEntity;
import com.googlecode.hibernate.audit.test.simpleentity.SimpleEntityTest;
import com.googlecode.hibernate.audit.util.HibernateUtils;

public abstract class AuditTest implements Synchronization {
	private static final String IDENT_DELIMITER = "\t";
	private Logger LOG = Logger.getLogger(AuditTest.class);

	public void afterCompletion(int status) {
		if (status != 0) {
			logTransactionModifications(LOG);
		} else {
			if (LOG.isDebugEnabled()) {
				LOG.debug("Transaction rolledback");
			}
		}
	}

	public void beforeCompletion() {
	}

	@BeforeSuite
	public void doBeforeTests() {
		DOMConfigurator configurator = new DOMConfigurator();
		InputStream is = null;
		try {
			is = SimpleEntityTest.class.getResourceAsStream("/log4j.xml");
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

	protected void logTransactionModifications(Logger log) {
		logTransactionModifications(log, 1);
	}

	protected void logTransactionModifications(Logger log,
			int transactionHistoryLevel) {
		logTransactionModifications(log, transactionHistoryLevel, null);
	}

	protected void logTransactionModifications(Logger log,
			int transactionHistoryLevel, SimpleEntity entity) {
		if (log.isDebugEnabled()) {
			log
					.debug("Begin transaction modification dump [transactionHistoryLevel="
							+ transactionHistoryLevel
							+ ", entity="
							+ entity
							+ "]");
			try {
				Session session = HibernateUtils.getCurrentSession();
				Query transactionQuery = session.createQuery("from "
						+ AuditTransaction.class.getName()
						+ " order by id desc");

				List<AuditTransaction> transactions = transactionQuery.list();
				Iterator<AuditTransaction> iterator = transactions.iterator();
				int stopLevel = transactionHistoryLevel;
				while (iterator.hasNext() && stopLevel > 0) {
					AuditTransaction transaction = iterator.next();

					log.debug("Transaction [id=" + transaction.getId()
							+ ", time=" + transaction.getTransactionTime()
							+ "]");
					Query query = null;
					if (entity != null) {
						query = session
								.createQuery("from "
										+ AuditObject.class.getName()
										+ " where auditClass.name = :className and auditTransaction.id = :transactionId and"
										+ " audittedEntityId = :audittedEntityId");
						query.setParameter("className", SimpleEntity.class
								.getName());
						query
								.setParameter("transactionId", transaction
										.getId());
						query.setParameter("audittedEntityId", String
								.valueOf(entity.getId()));
					} else {
						query = session
								.createQuery("from "
										+ AuditObject.class.getName()
										+ " where auditClass.name = :className and auditTransaction.id = :transactionId");
						query.setParameter("className", SimpleEntity.class
								.getName());
						query
								.setParameter("transactionId", transaction
										.getId());

					}
					List<AuditObject> auditObjectList = query.list();

					for (AuditObject auditObject : auditObjectList) {
						debugAuditObject(log, auditObject, IDENT_DELIMITER);
					}
					stopLevel--;
				}
			} finally {
				log
						.debug("End transaction modification dump [transactionHistoryLevel="
								+ transactionHistoryLevel
								+ ", entity="
								+ entity + "]");
			}
		}
	}

	private void debugAuditObject(Logger log, AuditObject auditObject,
			String indent) {
		log.debug(indent + auditObject.getAuditClass().getName() + "["
				+ auditObject.getAudittedEntityId() + "]");

		for (AuditObjectProperty property : auditObject.getAuditProperties()) {
			for (AuditObjectPropertyValue value : property.getValues()) {
				Hibernate.initialize(value);

				if (value instanceof AuditSimplePropertyValue) {
					log.debug(indent
							+ property.getAuditClassProperty().getName() + "="
							+ ((AuditSimplePropertyValue) value).getValue());

					Session session = HibernateUtils.getCurrentSession();
					Query previousValueQuery = session
							.createQuery("from "
									+ AuditSimplePropertyValue.class.getName()
									+ " where auditObjectProperty.auditClassProperty = :classProperty and "
									+ " auditObjectProperty.auditObject.audittedEntityId = :audittedEntityId and "
									+ " auditObjectProperty.auditObject.auditTransaction.id < :transactionId order by auditObjectProperty.auditObject.auditTransaction.id desc");

					previousValueQuery.setParameter("classProperty", property
							.getAuditClassProperty());
					previousValueQuery.setParameter("audittedEntityId",
							property.getAuditObject().getAudittedEntityId());
					previousValueQuery.setParameter("transactionId", property
							.getAuditObject().getAuditTransaction().getId());
					List<AuditSimplePropertyValue> previousValues = previousValueQuery
							.list();
					for (AuditSimplePropertyValue previousValue : previousValues) {
						log.debug(indent
								+ indent
								+ "oldValue="
								+ previousValue.getValue()
								+ ","
								+ "transactionId="
								+ previousValue.getAuditObjectProperty()
										.getAuditObject().getAuditTransaction()
										.getId());
					}
				} else if (value instanceof AuditComponentPropertyValue) {
					AuditObject component = ((AuditComponentPropertyValue) value)
							.getAuditObject();
					log.debug(indent
							+ property.getAuditClassProperty().getName() + "["
							+ component.getAudittedEntityId() + ","
							+ component.getId() + "]");
					debugAuditObject(log, component, indent + IDENT_DELIMITER);
				} else if (value instanceof AuditEntityRefPropertyValue) {
					Serializable entityId = ((AuditEntityRefPropertyValue) value)
							.getEntityRefId();
					log.debug(indent
							+ property.getAuditClassProperty().getName() + "["
							+ entityId + ","
							+ ((AuditEntityRefPropertyValue) value).getId()
							+ "]");
				}
			}
		}
	}

	protected Transaction getTransaction(Session session) {
		Transaction transaction = session.beginTransaction();
		transaction.registerSynchronization(this);
		return transaction;
	}

	protected Session getSession() {
		return HibernateUtils.getCurrentSession();
	}
}
