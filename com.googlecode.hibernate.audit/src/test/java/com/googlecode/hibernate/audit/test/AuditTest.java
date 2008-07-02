package com.googlecode.hibernate.audit.test;

import static org.testng.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.apache.log4j.Hierarchy;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;
import org.apache.log4j.xml.DOMConfigurator;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.StatelessSession;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.AfterMethod;

import com.googlecode.hibernate.audit.model_obsolete.transaction.AuditTransactionObsolete;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.AuditTransactionComponentRecord;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.AuditTransactionEntityRecord;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.AuditTransactionRecord;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordField;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.AuditTransactionRecordFieldValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.AuditValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.ComponentReferenceAuditValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.EntityReferenceAuditValue;
import com.googlecode.hibernate.audit.model_obsolete.transaction.record.field.value.SimpleAuditValue;
import com.googlecode.hibernate.audit.util.HibernateUtils;
import com.googlecode.hibernate.audit.util.Util;

/**
 * @author <a href="mailto:jchobantonov@gmail.com">Zhelyazko Chobantonov</a>
 * @author <a href="mailto:kchobantonov@gmail.com">Krasimir Chobantonov</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public abstract class AuditTest {

    private static final Logger log = Logger.getLogger(AuditTest.class);

    private static final String[] DEFAULT_AUDIT_TABLES =
        {
            "AUDIT_TRANSACTION",
            "AUDIT_CLASS",
            "AUDIT_TRANSACTION_RECORD",
            "AUDIT_CLASS_PROPERTY",
            "AUDIT_TRANSACTION_RECORD_FIELD",
            "AUDIT_TRAN_RECORD_FIELD_VALUE"
        };

    @BeforeSuite
	public void beforeSuite() throws Exception {

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
            } catch (IOException e) {
                log.warn(e);
            }
        }

        // take explicit control over Hibernate persistence unit initialization, don't leave it to
        // the (quasi)undeterminism associated with running a static block.
        HibernateUtils.initPersistenceUnit();
    }

    @AfterSuite(alwaysRun = true)
	public void afterSuite() throws Exception {

        HibernateUtils.closeCurrentSession();

        // leave the database clean
        HibernateUtils.dropPersistenceUnit();
    }

    @BeforeMethod
    public void beforeMethod(Method m) throws Exception
    {
        verifyTestTables();
        log.info("####################################################### " +
                 Util.methodToString(m) + " beginning");
    }

    @AfterMethod
    public void afterMethod(Method m) throws Exception
    {
        cleanTestTables();
        log.info("####################################################### " +
                 Util.methodToString(m) + " done");

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
					+ AuditTransactionObsolete.class.getName()
					+ " order by transactionTime desc");
			List<AuditTransactionObsolete> auditTransactions = getAuditTransactionQuery
					.list();
			for (AuditTransactionObsolete auditTransaction : auditTransactions) {
				log.debug("AuditTransaction2[id=" + auditTransaction.getId()
						+ ",time=" + auditTransaction.getTransactionTime()
						+ "] {");
				List<AuditTransactionEntityRecord> entities = getEntityObjects(session,
						auditTransaction);
				for (AuditTransactionEntityRecord entity : entities) {
					dumpAuditEntity("\t", '\t', entity);
					log.debug("}");
				}
			}
		} finally {
			session.close();
		}
	}

    /**
     * @return the tables "touched" by a sub-class test. The infrastructure will make sure those
     *         tables are empty before each test begins, and the tables are cleared after the test
     *         ends.
     */
    protected abstract String[] getTestTables();

    private void dumpAuditEntity(String indent, char indentChar, AuditTransactionEntityRecord entity) {
		log.debug(indent + "AuditEntityObject[id=" + entity.getId()
				+ ",className=" + entity.getAuditClass().getName()
				+ ",auditedEntityId=" + entity.getAuditedEntityId()
				+ ",operation=" + entity.getOperation() + "] {");
		dumpAuditObjectProperties(indent, indentChar, entity);
		log.debug(indent + "}");
	}

	private void dumpAuditComponent(String indent, char indentChar, AuditTransactionComponentRecord component) {
		log.debug(indent + "AuditComponentObject[id=" + component.getId()
				+ ",className=" + component.getAuditClass().getName()
				+ ",auditedEntityId=" + component.getAuditedEntityId()
				+ ",operation=" + component.getOperation() + "] {");
		dumpAuditObjectProperties(indent, indentChar, component);
		log.debug(indent + "}");
	}
	
	private void dumpAuditObjectProperties(String indent, char indentChar,
			AuditTransactionRecord auditObject) {
		for (AuditTransactionRecordField auditProperty : getRecordFields(auditObject)) {
			log.debug(indent + indentChar + "AuditTransactionRecordField[id="
					+ auditProperty.getId() + ",propertyName="
					+ auditProperty.getAuditClassProperty().getName() 
					+ ",operation=" + auditProperty.getOperation() 
					+ "] {");

			for (AuditTransactionRecordFieldValue recordFieldValue : getRecordFieldValues(auditProperty)) {
				AuditValue value = recordFieldValue.getValue();
				if (value.isOfType(SimpleAuditValue.class)) {
					log.debug(indent + indentChar + ((SimpleAuditValue)value).getValue());
				} else if (value.isOfType(ComponentReferenceAuditValue.class)) {
					log.debug(indent + indentChar + "component{");
					dumpAuditComponent(indent + indentChar, indentChar, ((ComponentReferenceAuditValue)value).getComponentAuditObject());
					log.debug(indent + indentChar + "}");
				} else if (value.isOfType(EntityReferenceAuditValue.class)) {
					log.debug(indent + indentChar + "entityRef[" + ((EntityReferenceAuditValue)value).getEntityReferenceId() + "]");
				} else {
					log.debug(indent + indentChar + "UNKNOWN VALUE");
				}
				
				log.debug(indent + indentChar + ",");
			}

			log.debug(indent + indentChar + "}");
		}
	}

	private List<AuditTransactionEntityRecord> getEntityObjects(Session session,
			AuditTransactionObsolete transaction) {
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

    private String[] getAllTestTables()
    {
        String[] subclassTestTables = getTestTables();

        String[] result = new String[DEFAULT_AUDIT_TABLES.length + subclassTestTables.length];

        System.arraycopy(DEFAULT_AUDIT_TABLES, 0,
                         result, 0, DEFAULT_AUDIT_TABLES.length);
        System.arraycopy(subclassTestTables, 0,
                         result, DEFAULT_AUDIT_TABLES.length, subclassTestTables.length);

        return result;
    }

    private void verifyTestTables() throws Exception
    {
        String[] tables = getAllTestTables();

        StatelessSession s = HibernateUtils.getStatelessSession();
        Transaction t = s.beginTransaction();
        Connection c = s.connection();

        for(String table: tables)
        {
            Statement stat = c.createStatement();
            ResultSet rs = stat.executeQuery("SELECT * FROM " + table);
            assert !rs.next();
            stat.close();
        }

        t.commit();
    }

    /**
     * This method ensures that all tables associated with this test are clean afther each test
     * method exits.
     */
    private void cleanTestTables() throws Exception
    {
        // we leave tables associated with this test clean after each test method

        String[] tables = getAllTestTables();

        StatelessSession s = HibernateUtils.getStatelessSession();
        Transaction t = s.beginTransaction();
        Connection c = s.connection();

        for(int i = tables.length - 1; i >=0; i --)
        {
            String table = tables[i];
            try
            {
                Statement stat = c.createStatement();
                int r = stat.executeUpdate("DELETE FROM " + table);
                log.debug("cleaned table " + table + ", " + r + " row(s) deleted");
                stat.close();
            }
            catch(Exception e)
            {
                log.error("failed to delete " + table + "'s content", e);
            }
        }

        t.commit();
    }
}
