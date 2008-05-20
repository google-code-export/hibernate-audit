package com.googlecode.hibernate.audit.test.simpleentity;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.model.simpleentity.SimpleComponent;
import com.googlecode.hibernate.audit.model.simpleentity.SimpleEmbeddedComponent;
import com.googlecode.hibernate.audit.model.simpleentity.SimpleEntity;
import com.googlecode.hibernate.audit.test.AuditTest;

public class SimpleEntityTest extends AuditTest {
	private static final Logger LOG = Logger.getLogger(SimpleEntityTest.class);

	@Test(enabled = true)
	public void test() {
		Session session = getSession();

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
		session.save(entity);
		transaction.commit();

		transaction.begin();
		entity.setString("newString");
		session.update(entity);
		transaction.commit();
	}
	
}
