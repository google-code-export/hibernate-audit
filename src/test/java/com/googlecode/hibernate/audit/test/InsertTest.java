package com.googlecode.hibernate.audit.test;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.test.model1.Model1Child;
import com.googlecode.hibernate.audit.test.model1.Model1Factory;
import com.googlecode.hibernate.audit.test.model1.Model1Parent;

public class InsertTest extends AbstractHibernateAuditTest {

    @Test
    public void simpleInsert() {
        Model1Parent parent = Model1Factory.eINSTANCE.createModel1Parent();
        Model1Child child = Model1Factory.eINSTANCE.createModel1Child();

        parent.getChildren().add(child);

        Session session = null;
        try {
            session = dataStore.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();
            
            session.save(parent);
            
            tx.commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
