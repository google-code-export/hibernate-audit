package com.googlecode.hibernate.audit.test.model.simpleentity;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.model.AuditComponentPropertyValue;
import com.googlecode.hibernate.audit.model.AuditEntityRefPropertyValue;
import com.googlecode.hibernate.audit.model.AuditObject;
import com.googlecode.hibernate.audit.model.AuditObjectProperty;
import com.googlecode.hibernate.audit.model.AuditObjectPropertyValue;
import com.googlecode.hibernate.audit.model.AuditSimplePropertyValue;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.test.util.HibernateUtils;

public class SimpleEntityTest {
	private DateFormat formatter = new SimpleDateFormat("yyyy-mm-dd hh:MM:ss");
	private static final char IDENT_DELIMITER = '\t';

	@BeforeSuite
	public void doBeforeTests() {
		/*
		 * DOMConfigurator configurator = new DOMConfigurator(); InputStream is =
		 * TestInsertEntity.class.getResourceAsStream("/log4j.xml");
		 * assertNotNull(is); configurator.doConfigure(is, new Hierarchy(new
		 * RootLogger(Level.DEBUG))); try { is.close(); } catch (IOException
		 * ignored) { }
		 */}

	@Test(enabled = true)
	public void test() {
		Session session = HibernateUtils.getCurrentSession();

		SimpleEntity entity = new SimpleEntity();
		entity.setString("string");

		SimpleEntity refEntity = new SimpleEntity();
		refEntity.setString("refString");
		entity.setRelatedSimpleEntity(refEntity);

		SimpleComponent comp = new SimpleComponent();
		comp.setField1("Field 1 Value");
		comp.setField2("Field 2 Value");
		entity.setInnerComponent(comp);

		SimpleEntity compRefEntity = new SimpleEntity();
		compRefEntity.setString("compRefString");
		comp.setComponentRelatedSimpleEntity(compRefEntity);

		SimpleEmbeddedComponent innerComp = new SimpleEmbeddedComponent();
		innerComp.setField1("innerCompField1");
		innerComp.setField2("innerCompField2");
		comp.setInner(innerComp);

		Transaction transaction = session.beginTransaction();
		transaction.begin();
		session.save(entity);
		transaction.commit();

		transaction.begin();
		entity.setString("newString");
		session.update(entity);
		transaction.commit();
		
		transaction = session.beginTransaction();

		Query query = session
				.createQuery("from "
						+ AuditObject.class.getName()
						+ " where auditClass.name = :className and audittedEntityId = :audittedEntityId");
		query.setParameter("className", SimpleEntity.class.getName());
		query.setParameter("audittedEntityId", String.valueOf(entity.getId()));
		List<AuditObject> auditObjectList = query.list();

		for (AuditObject auditObject : auditObjectList) {
			dumpAuditObject(auditObject, String.valueOf(IDENT_DELIMITER));
		}

		transaction.commit();
	}

	private void dumpAuditObject(AuditObject auditObject, String indent) {
		System.out.println(indent + auditObject.getAuditClass().getName() + "["
				+ auditObject.getAudittedEntityId() + "]");

		for (AuditObjectProperty property : auditObject.getAuditProperties()) {
			for (AuditObjectPropertyValue value : property.getValues()) {
				Hibernate.initialize(value);

				if (value instanceof AuditSimplePropertyValue) {
					System.out.println(indent
							+ property.getAuditClassProperty().getName() + "="
							+ ((AuditSimplePropertyValue) value).getValue());
					
					Session session = HibernateUtils.getCurrentSession();
					Query previousValueQuery = session.createQuery("from " + 
								AuditSimplePropertyValue.class.getName() + 
								" where auditObjectProperty.auditClassProperty = :classProperty and " +
								" auditObjectProperty.auditObject.audittedEntityId = :audittedEntityId and " +
								" auditObjectProperty.auditObject.auditTransaction.id < :transactionId order by auditObjectProperty.auditObject.auditTransaction.id desc");
					
					previousValueQuery.setParameter("classProperty", property.getAuditClassProperty());
					previousValueQuery.setParameter("audittedEntityId", property.getAuditObject().getAudittedEntityId());
					previousValueQuery.setParameter("transactionId", property.getAuditObject().getAuditTransaction().getId());
					List<AuditSimplePropertyValue> previousValues = previousValueQuery.list();
					for (AuditSimplePropertyValue previousValue: previousValues) {
						System.out.println(indent + indent
								+ "oldValue="
								+ previousValue.getValue() + ","
								+ "transactionId=" + previousValue.getAuditObjectProperty().getAuditObject().getAuditTransaction().getId());
					}
				} else if (value instanceof AuditComponentPropertyValue) {
					AuditObject component = ((AuditComponentPropertyValue) value)
							.getAuditObject();
					System.out.println(indent
							+ property.getAuditClassProperty().getName() + "["
							+ component.getAudittedEntityId() + ","
							+ component.getId() + "]");
					dumpAuditObject(component, indent + IDENT_DELIMITER);
				} else if (value instanceof AuditEntityRefPropertyValue) {
					Serializable entityId = ((AuditEntityRefPropertyValue) value)
							.getEntityRefId();
					System.out.println(indent
							+ property.getAuditClassProperty().getName() + "["
							+ entityId + ","
							+ ((AuditEntityRefPropertyValue) value).getId()
							+ "]");
				}
			}
		}
	}

	/*
	 * private void validateInsertEntity(Session session, SimpleEntity entity,
	 * Map<String, String> propertyToValue, Date startDate, Date endDate) {
	 * Transaction transaction; // check the audit log
	 * 
	 * transaction = session.beginTransaction(); transaction.begin(); AuditClass
	 * auditClass = getAuditClass(session, entity);
	 * 
	 * AuditTransaction auditTransaction = getAuditTransaction(session,
	 * startDate, endDate);
	 * 
	 * AuditObject auditEntity = getAuditEntity(session, entity, auditClass,
	 * auditTransaction); assertEquals(auditEntity.getOperation(),
	 * AuditOperation.INSERT);
	 * 
	 * for (java.util.Iterator<Map.Entry<String, String>> iter =
	 * propertyToValue .entrySet().iterator(); iter.hasNext();) {
	 * 
	 * Map.Entry<String, String> entry = iter.next(); String name =
	 * entry.getKey(); String value = entry.getValue();
	 * 
	 * AuditClassProperty auditProperty = getAuditProperty(session, auditClass,
	 * name); AuditEntityProperty auditEntityProperty = getAuditEntityProperty(
	 * session, auditEntity, auditProperty);
	 * assertEquals(auditEntityProperty.getOperation(),
	 * AuditEntityPropertyOperation.INSERT);
	 * 
	 * AuditEntityPropertyValue auditEntityPropertyValue =
	 * getAuditEntityPropertyValue( session, auditEntityProperty);
	 * assertNotNull(auditEntityPropertyValue);
	 * assertEquals(auditEntityPropertyValue.getOperation(),
	 * AuditEntityPropertyValueOperation.SET_VALUE);
	 * assertEquals(auditEntityPropertyValue.getValue(), value); }
	 * 
	 * transaction.commit(); }
	 * 
	 * private void validateUpdateEntity(Session session, SimpleEntity entity,
	 * Map<String, String> oldPropertyToValue, Map<String, String>
	 * newPropertyToValue, Date startDate, Date endDate) { Transaction
	 * transaction; // check the audit log
	 * 
	 * transaction = session.beginTransaction(); transaction.begin(); AuditClass
	 * auditClass = getAuditClass(session, entity);
	 * 
	 * AuditTransaction auditTransaction = getAuditTransaction(session,
	 * startDate, endDate);
	 * 
	 * AuditObject auditEntity = getAuditEntity(session, entity, auditClass,
	 * auditTransaction); assertEquals(auditEntity.getOperation(),
	 * AuditOperation.UPDATE);
	 * 
	 * for (java.util.Iterator<Map.Entry<String, String>> iter =
	 * newPropertyToValue .entrySet().iterator(); iter.hasNext();) {
	 * 
	 * Map.Entry<String, String> entry = iter.next(); String name =
	 * entry.getKey(); String oldValue = oldPropertyToValue.get(name); String
	 * newValue = entry.getValue();
	 * 
	 * AuditClassProperty auditProperty = getAuditProperty(session, auditClass,
	 * name); AuditEntityProperty auditEntityProperty = getAuditEntityProperty(
	 * session, auditEntity, auditProperty);
	 * assertEquals(auditEntityProperty.getOperation(),
	 * AuditEntityPropertyOperation.UPDATE);
	 * 
	 * 
	 * AuditEntityPropertyValue oldAuditEntityPropertyValue =
	 * getAuditEntityPropertyValue( session, auditEntityProperty,
	 * AuditEntityPropertyValueOperation.UNSET_VALUE);
	 * assertNotNull(oldAuditEntityPropertyValue);
	 * assertEquals(oldAuditEntityPropertyValue.getValue(), oldValue);
	 * 
	 * AuditEntityPropertyValue newAuditEntityPropertyValue =
	 * getAuditEntityPropertyValue( session, auditEntityProperty,
	 * AuditEntityPropertyValueOperation.SET_VALUE);
	 * assertNotNull(newAuditEntityPropertyValue);
	 * assertEquals(newAuditEntityPropertyValue.getValue(), newValue); }
	 * 
	 * transaction.commit(); }
	 * 
	 * private void validateDeleteEntity(Session session, SimpleEntity entity,
	 * Date startDate, Date endDate) { Transaction transaction; // check the
	 * audit log
	 * 
	 * transaction = session.beginTransaction(); transaction.begin(); AuditClass
	 * auditClass = getAuditClass(session, entity);
	 * 
	 * AuditTransaction auditTransaction = getAuditTransaction(session,
	 * startDate, endDate);
	 * 
	 * AuditObject auditEntity = getAuditEntity(session, entity, auditClass,
	 * auditTransaction); assertEquals(auditEntity.getOperation(),
	 * AuditOperation.DELETE);
	 * 
	 * transaction.commit(); }
	 * 
	 * private AuditEntityPropertyValue getAuditEntityPropertyValue( Session
	 * session, AuditEntityProperty auditEntityProperty) { org.hibernate.Query
	 * auditEntityPropertyValueQuery = session .createQuery("from " +
	 * AuditEntityPropertyValue.class.getName() + " where auditEntityProperty =
	 * :auditEntityProperty");
	 * 
	 * auditEntityPropertyValueQuery.setParameter("auditEntityProperty",
	 * auditEntityProperty); AuditEntityPropertyValue auditEntityPropertyValue =
	 * (AuditEntityPropertyValue) auditEntityPropertyValueQuery .uniqueResult();
	 * return auditEntityPropertyValue; }
	 * 
	 * private AuditEntityPropertyValue getAuditEntityPropertyValue( Session
	 * session, AuditEntityProperty auditEntityProperty,
	 * AuditEntityPropertyValueOperation operation) { org.hibernate.Query
	 * auditEntityPropertyValueQuery = session .createQuery("from " +
	 * AuditEntityPropertyValue.class.getName() + " where auditEntityProperty =
	 * :auditEntityProperty and operation = :operation");
	 * 
	 * auditEntityPropertyValueQuery.setParameter("auditEntityProperty",
	 * auditEntityProperty);
	 * auditEntityPropertyValueQuery.setParameter("operation", operation);
	 * AuditEntityPropertyValue auditEntityPropertyValue =
	 * (AuditEntityPropertyValue) auditEntityPropertyValueQuery .uniqueResult();
	 * return auditEntityPropertyValue; }
	 * 
	 * private AuditEntityProperty getAuditEntityProperty(Session session,
	 * AuditObject auditEntity, AuditClassProperty auditProperty) {
	 * org.hibernate.Query auditEntityPropertyQuery = session .createQuery("from " +
	 * AuditEntityProperty.class.getName() + " where auditEntity = :auditEntity
	 * and auditProperty = :auditProperty");
	 * auditEntityPropertyQuery.setParameter("auditEntity", auditEntity);
	 * auditEntityPropertyQuery.setParameter("auditProperty", auditProperty);
	 * AuditEntityProperty auditEntityProperty = (AuditEntityProperty)
	 * auditEntityPropertyQuery .uniqueResult();
	 * assertNotNull(auditEntityProperty); return auditEntityProperty; }
	 * 
	 * private AuditObject getAuditEntity(Session session, SimpleEntity entity,
	 * AuditClass auditClass, AuditTransaction auditTransaction) {
	 * org.hibernate.Query auditEntityQuery = session .createQuery("from " +
	 * AuditObject.class.getName() + " where auditTransaction =
	 * :auditTransaction and auditClass = :auditClass");
	 * auditEntityQuery.setParameter("auditTransaction", auditTransaction);
	 * auditEntityQuery.setParameter("auditClass", auditClass);
	 * 
	 * AuditObject auditEntity = (AuditObject) auditEntityQuery.uniqueResult(); //
	 * we // inserted // only // one // entity // in // this // transaction
	 * assertNotNull(auditEntity); assertEquals(auditEntity.getEntityId(),
	 * String.valueOf(entity.getId())); return auditEntity; }
	 * 
	 * private AuditTransaction getAuditTransaction(Session session, Date
	 * auditLogTransactionStartDate, Date auditLogTransactionEndDate) {
	 * org.hibernate.Query transactionQuery = session .createQuery("from " +
	 * AuditTransaction.class.getName() + " where transactionTime >
	 * :transactionStartTime and transactionTime < :transactionEndTime");
	 * transactionQuery.setParameter("transactionStartTime",
	 * auditLogTransactionStartDate);
	 * transactionQuery.setParameter("transactionEndTime",
	 * auditLogTransactionEndDate); AuditTransaction auditTransaction =
	 * (AuditTransaction) transactionQuery .uniqueResult();
	 * assertNotNull(auditTransaction); return auditTransaction; }
	 * 
	 * private AuditClass getAuditClass(Session session, SimpleEntity entity) {
	 * org.hibernate.Query auditClassQuery = session.createQuery("from " +
	 * AuditClass.class.getName() + " where name = :name");
	 * auditClassQuery.setParameter("name", entity.getClass().getName());
	 * AuditClass auditClass = (AuditClass) auditClassQuery.uniqueResult();
	 * assertNotNull(auditClass); return auditClass; }
	 * 
	 * private AuditClassProperty getAuditProperty(Session session, AuditClass
	 * auditClass, String name) { org.hibernate.Query auditPropertyQuery =
	 * session.createQuery("from " + AuditClassProperty.class.getName() + "
	 * where auditClass = :auditClass and name = :name");
	 * auditPropertyQuery.setParameter("auditClass", auditClass);
	 * auditPropertyQuery.setParameter("name", name); AuditClassProperty
	 * auditProperty = (AuditClassProperty) auditPropertyQuery .uniqueResult();
	 * assertNotNull(auditProperty); return auditProperty; }
	 */
	@AfterSuite(alwaysRun = true)
	public void doAfterTests() {
		HibernateUtils.closeCurrentSession();
	}
}
