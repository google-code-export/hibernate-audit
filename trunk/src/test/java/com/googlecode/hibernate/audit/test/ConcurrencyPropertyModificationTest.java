package com.googlecode.hibernate.audit.test;

import org.eclipse.emf.teneo.mapping.strategy.EntityNameStrategy;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.exception.ObjectConcurrentModificationException;
import com.googlecode.hibernate.audit.exception.PropertyConcurrentModificationException;
import com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationBehavior;
import com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationLevelCheck;
import com.googlecode.hibernate.audit.extension.concurrent.ConcurrentModificationProvider;
import com.googlecode.hibernate.audit.listener.AuditSessionFactoryObserver;
import com.googlecode.hibernate.audit.test.model1.Model1Factory;
import com.googlecode.hibernate.audit.test.model1.Model1Package;
import com.googlecode.hibernate.audit.test.model1.Model1Person;

public class ConcurrencyPropertyModificationTest extends AbstractHibernateAuditTest {

	@Test(sequential = true)
	public void testUpdateDifferentObjectProperties() {
		AuditConfiguration config = AuditSessionFactoryObserver.getAuditConfiguration(dataStore.getSessionFactory());
		config.getExtensionManager().setConcurrentModificationProvider(new TestPropertyConcurrencyModificationProvider());

		EntityNameStrategy strategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);

		Model1Person person = createNewPerson();
		Assert.assertNotNull(person, "Unable to create and store a new Person instance");
		Assert.assertNotNull(person.getId(), "Hibernate did not assign new id for Person instance");

		Long transactionId = getLastTransactionId();
		Assert.assertNotNull(transactionId);

		// prepare to update the entity
		try {

			TestPropertyConcurrencyModificationProvider.auditTransactionId.set(transactionId); // save
			// current
			// transaction audit id

			Session session = dataStore.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();
			Model1Person loadedPerson = (Model1Person) session.get(strategy.toEntityName(Model1Package.eINSTANCE.getModel1Person()), person.getId());
			Assert.assertNotNull(loadedPerson, "Unable to load the previously stored person object");

			loadedPerson.setFirstName("New-" + loadedPerson.getFirstName());

			tx.commit();
			session.close();

			Long transactionIdAfterInsert = getLastTransactionId();
			Assert.assertNotNull(transactionIdAfterInsert, "Unable to obtain the audit transaction id");
			Assert.assertTrue(transactionIdAfterInsert.longValue() > transactionId.longValue(), "The audit transaction id did not increase.");

			// we have changes the Model1Person.firstName - now try to update
			// the person object without setting the auditTransactionId.set to
			// the current audit transaction
			session = dataStore.getSessionFactory().openSession();
			tx = session.beginTransaction();

			// this should succeed even though the auditTransactionId.get() is
			// the same as the previous update - we are changing different
			// property
			session.merge(loadedPerson);
			loadedPerson.setLastName("New-" + person.getLastName());

			tx.commit();
			session.close();
		} finally {
			TestPropertyConcurrencyModificationProvider.auditTransactionId.set(null);
		}

	}

	@Test(sequential = true)
	public void testPropertyConcurrentModificationException() {
		AuditConfiguration config = AuditSessionFactoryObserver.getAuditConfiguration(dataStore.getSessionFactory());
		config.getExtensionManager().setConcurrentModificationProvider(new TestPropertyConcurrencyModificationProvider());

		EntityNameStrategy strategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);

		Model1Person person = createNewPerson();
		Assert.assertNotNull(person, "Unable to create and store a new Person instance");
		Assert.assertNotNull(person.getId(), "Hibernate did no assign new id for Person instance");

		Long transactionId = getLastTransactionId();
		Assert.assertNotNull(transactionId);

		// prepare to update the entity
		try {

			TestPropertyConcurrencyModificationProvider.auditTransactionId.set(transactionId); // save
			// the
			// current
			// transaction audit id

			Session session = dataStore.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();
			Model1Person loadedPerson = (Model1Person) session.get(strategy.toEntityName(Model1Package.eINSTANCE.getModel1Person()), person.getId());
			Assert.assertNotNull(loadedPerson, "Unable to load the previously stored person object");

			loadedPerson.setLastName("New-" + loadedPerson.getLastName());

			tx.commit();
			session.close();

			Long transactionIdAfterInsert = getLastTransactionId();
			Assert.assertNotNull(transactionIdAfterInsert, "Unable to obtain the audit transaction id");
			Assert.assertTrue(transactionIdAfterInsert.longValue() > transactionId.longValue(), "The audit transaction id did not increase.");

			// we have changes the Model1Person.firstName - now try to update
			// the person object without setting the auditTransactionId.set to
			// the current audit transaction
			try {
				session = dataStore.getSessionFactory().openSession();
				tx = session.beginTransaction();

				session.merge(person);
				person.setLastName("Old-" + person.getFirstName());

				tx.commit();
				session.close();

				Assert.fail("We did not got PropertyConcurrentModificationException");
			} catch (PropertyConcurrentModificationException e) {
				Assert.assertEquals(e.getPropertyName(), Model1Package.eINSTANCE.getModel1Person_LastName().getName(), "We got concurrency exception but for some other property");
			}

		} finally {
			TestPropertyConcurrencyModificationProvider.auditTransactionId.set(null);
		}
	}

	@Test(sequential = true)
	public void testPropertyConcurrentModificationDeleteAfterUpdate() {
		AuditConfiguration config = AuditSessionFactoryObserver.getAuditConfiguration(dataStore.getSessionFactory());
		config.getExtensionManager().setConcurrentModificationProvider(new TestPropertyConcurrencyModificationProvider());

		EntityNameStrategy strategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);

		// 1. Create a new entity
		Model1Person person = createNewPerson();
		Assert.assertNotNull(person, "Unable to create and store a new Person instance");
		Assert.assertNotNull(person.getId(), "Hibernate did no assign new id for Person instance");

		Long transactionId = getLastTransactionId();
		Assert.assertNotNull(transactionId);

		// prepare to update the entity
		try {
			// 2. Update the entity with the most up to date transaction id
			TestPropertyConcurrencyModificationProvider.auditTransactionId.set(transactionId); // save

			Session session = dataStore.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();
			Model1Person loadedPerson = (Model1Person) session.get(strategy.toEntityName(Model1Package.eINSTANCE.getModel1Person()), person.getId());
			Assert.assertNotNull(loadedPerson, "Unable to load the previously stored person object");

			loadedPerson.setLastName("New-" + loadedPerson.getLastName());

			tx.commit();
			session.close();

			Long transactionIdAfterUpdate = getLastTransactionId();
			Assert.assertNotNull(transactionIdAfterUpdate, "Unable to obtain the audit transaction id");
			Assert.assertTrue(transactionIdAfterUpdate.longValue() > transactionId.longValue(), "The audit transaction id did not increase.");

			// 3. Try to delete the entity with the audit transaction id from
			// point 1

			// update the entity
			try {
				TestPropertyConcurrencyModificationProvider.auditTransactionId.set(transactionId);
				session = dataStore.getSessionFactory().openSession();
				tx = session.beginTransaction();

				session.delete(person);

				tx.commit();
				session.close();

				Assert.fail("We did not got PropertyConcurrentModificationException");
			} catch (PropertyConcurrentModificationException e) {
				Assert.assertEquals(e.getPropertyName(), Model1Package.eINSTANCE.getModel1Person_LastName().getName(), "We got concurrency exception but for some other property");
			}

			// 4. Try to delete the entity with the audit transaction id from
			// point 3

			// update the entity
			TestPropertyConcurrencyModificationProvider.auditTransactionId.set(transactionIdAfterUpdate);
			session = dataStore.getSessionFactory().openSession();
			tx = session.beginTransaction();

			session.delete(person);

			tx.commit();
			session.close();

		} finally {
			TestPropertyConcurrencyModificationProvider.auditTransactionId.set(null);
		}
	}

	private Long getLastTransactionId() {
		Session session = null;
		try {
			session = dataStore.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();
			Long transactionId = HibernateAudit.getLatestAuditTransactionId(session);
			tx.commit();

			return transactionId;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	private Model1Person createNewPerson() {
		Session session = null;
		try {
			session = dataStore.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();

			Model1Person person = Model1Factory.eINSTANCE.createModel1Person();
			person.setFirstName("First Name");
			person.setLastName("Last Name");

			session.save(person);

			person.getId();
			tx.commit();

			return person;
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}

	private static class TestPropertyConcurrencyModificationProvider implements ConcurrentModificationProvider {
		private static final ThreadLocal<Long> auditTransactionId = new ThreadLocal<Long>();

		public ConcurrentModificationBehavior getCheckBehavior() {
			return ConcurrentModificationBehavior.THROW_EXCEPTION;
		}

		public ConcurrentModificationLevelCheck getLevelCheck() {
			return ConcurrentModificationLevelCheck.PROPERTY;
		}

		public Long getLoadAuditTransactionId() {
			return auditTransactionId.get();
		}
	}
}
