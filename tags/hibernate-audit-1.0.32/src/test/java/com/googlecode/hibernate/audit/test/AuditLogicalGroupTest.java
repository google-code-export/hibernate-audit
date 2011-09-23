package com.googlecode.hibernate.audit.test;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.extension.event.AuditLogicalGroupProvider;
import com.googlecode.hibernate.audit.listener.AuditSessionFactoryObserver;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.test.model1.Model1Factory;
import com.googlecode.hibernate.audit.test.model1.Model1Person;
import com.googlecode.hibernate.audit.test.model1.impl.Model1PersonImpl;

public class AuditLogicalGroupTest extends AbstractHibernateAuditTest {
	private static final ThreadLocal<AuditLogicalGroup> AUDIT_LOGICAL_GROUP = new ThreadLocal<AuditLogicalGroup>();
	
	@Test
	public void testSaveAuditLogicalGroup() {
		AuditConfiguration config = AuditSessionFactoryObserver.getAuditConfiguration(dataStore.getSessionFactory());
		config.getExtensionManager().setAuditLogicalGroupProvider(new TestAuditLogicalGroupProvider());
		
		
		AuditLogicalGroup auditLogicalGroup = new AuditLogicalGroup();
		AuditType auditType = new AuditType();
		auditType.setClassName(Model1PersonImpl.class.getName());
		
		auditLogicalGroup.setAuditType(auditType);
		auditLogicalGroup.setExternalId("3252499999");
		
		try {
			AUDIT_LOGICAL_GROUP.set(auditLogicalGroup);

			Session s = dataStore.getSessionFactory().openSession();
			Transaction t = s.beginTransaction();
			HibernateAudit.getAuditType(s, Model1PersonImpl.class.getName());
			t.commit();
			s.close();

			Model1Person person = createNewPerson();
			
			Session session = dataStore.getSessionFactory().openSession();
			Transaction tx = session.beginTransaction();
			
			session.save(person);
			tx.commit();
			session.close();

			
		} finally {
			AUDIT_LOGICAL_GROUP.set(null);
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

	
	private class TestAuditLogicalGroupProvider implements AuditLogicalGroupProvider {
	    public AuditLogicalGroup getAuditLogicalGroup(Session session, AuditEvent auditEvent) {
	    	return AUDIT_LOGICAL_GROUP.get();
	    }
	}
}
