package com.googlecode.hibernate.audit.test.collection;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.event.EventSource;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.model_obsolete.collection.Child;
import com.googlecode.hibernate.audit.model_obsolete.collection.Parent;
import com.googlecode.hibernate.audit.test.AuditTest;

public class CollectionTest extends AuditTest {
	private static final Logger LOG = Logger.getLogger(CollectionTest.class);

	@Test(enabled = true)
	public void test() {
		Session session = getSession();
		Transaction transaction = session.beginTransaction();

		Parent parentEntity = new Parent();
		parentEntity.setParentProperty("Parent 1");

		Child childEntity1 = new Child();
		childEntity1.setChildProperty("Child 1");
		parentEntity.addChild(childEntity1);
		
		Child childEntity2 = new Child();
		childEntity2.setChildProperty("Child 2");
		parentEntity.addChild(childEntity2);

		session.save(parentEntity);
		((EventSource)session).getActionQueue().executeActions();
		transaction.commit();
	}

    @Override
    protected String[] getTestTables()
    {
        //return new String[] { "PARENT", "CHILD" };
        return new String[0];
    }
}
