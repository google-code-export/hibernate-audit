/**
 * Copyright (C) 2009 Krasimir Chobantonov <kchobantonov@yahoo.com>
 * This file is part of Hibernate Audit.

 * Hibernate Audit is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Hibernate Audit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with Hibernate Audit.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.googlecode.hibernate.audit.test;

import java.util.Iterator;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.teneo.mapping.strategy.EntityNameStrategy;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.HibernateAuditInstantiator;
import com.googlecode.hibernate.audit.extension.auditable.AuditableInformationProvider;
import com.googlecode.hibernate.audit.listener.AuditListener;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.test.model1.Model1Child;
import com.googlecode.hibernate.audit.test.model1.Model1Package;
import com.googlecode.hibernate.audit.test.model1.Model1Parent;

public class InsertTest extends AbstractHibernateAuditTest {

    @DataProvider(name = "xmiTemplates")
    public Object[][] getXmiTemplates() throws Exception {
        Object[][] data = new Object[1][1];
        data[0][0] = "xmi/Model1Parent.xmi";
        return data;
    }

    @Test(dataProvider = "xmiTemplates", enabled = true)
    public void simpleInsert(String resource) {
    	EntityNameStrategy entityNameStrategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);
    	AuditableInformationProvider auditableInformationProvider = AuditListener.getAuditConfiguration(dataStore.getHibernateConfiguration()).getExtensionManager().getAuditableInformationProvider();
    	
        String loadedXmi = loadResource(resource);
        EList<EObject> eObjects = EMFUtil.toEObject(loadedXmi, true, dataStore.getEPackages());

        Session session = null;
        try {
            session = dataStore.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            Long latestTransactionIdBeforeInsert = HibernateAudit.getLatestAuditTransactionId(session);
            Assert.assertNotNull(latestTransactionIdBeforeInsert);

            for (EObject obj : eObjects) {
                session.save(obj);
                Iterator<EObject> objContentIterator = obj.eAllContents();
                while (objContentIterator.hasNext()) {
                    EObject dependentEObject = objContentIterator.next();
                    session.save(dependentEObject);
                }
            }

            tx.commit();

            tx = session.beginTransaction();
            Long latestTransactionIdAfterInsert = HibernateAudit.getLatestAuditTransactionId(session);

            Assert.assertNotNull(latestTransactionIdAfterInsert);
            Assert.assertTrue(latestTransactionIdAfterInsert > latestTransactionIdBeforeInsert);

            for (EObject eObj : eObjects) {
                AuditType auditType = HibernateAudit.getAuditType(session, auditableInformationProvider.getAuditTypeClassName(dataStore.getHibernateConfiguration(), entityNameStrategy.toEntityName(eObj.eClass())));
                EObject auditEObject = (EObject) HibernateAuditInstantiator.getEntity(session, auditType, eObj.eGet(eObj.eClass().getEIDAttribute()) + "", latestTransactionIdAfterInsert);

                assertEquals(resource, loadedXmi, EMFUtil.toXMI(auditEObject, dataStore.getEPackages()));
            }
            tx.commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Test(dataProvider = "xmiTemplates", enabled = true)
    public void simpleInsertUpdate(String resource) {
    	EntityNameStrategy entityNameStrategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);
    	AuditableInformationProvider auditableInformationProvider = AuditListener.getAuditConfiguration(dataStore.getHibernateConfiguration()).getExtensionManager().getAuditableInformationProvider();

        String loadedXmi = loadResource(resource);
        Model1Parent parent = (Model1Parent) EMFUtil.toEObject(loadedXmi, true, dataStore.getEPackages()).get(0);

        Session session = null;
        try {
            session = dataStore.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            Long latestTransactionIdBeforeInsert = HibernateAudit.getLatestAuditTransactionId(session);
            Assert.assertNotNull(latestTransactionIdBeforeInsert);

            session.save(parent);
            for (Model1Child child : parent.getChildren()) {
                session.save(child);
            }

            tx.commit();

            tx = session.beginTransaction();
            EntityNameStrategy strategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);
            Model1Parent storedParent = (Model1Parent) session.get(strategy.toEntityName(Model1Package.eINSTANCE.getModel1Parent()), parent.getId());
            Assert.assertNotNull(storedParent);

            storedParent.setFirstName(storedParent.getFirstName() + "-New name");

            loadedXmi = EMFUtil.toXMI(storedParent, dataStore.getEPackages());
            tx.commit();

            tx = session.beginTransaction();
            Long latestTransactionIdAfterInsert = HibernateAudit.getLatestAuditTransactionId(session);

            Assert.assertNotNull(latestTransactionIdAfterInsert);
            Assert.assertTrue(latestTransactionIdAfterInsert > latestTransactionIdBeforeInsert);

            AuditType auditType = HibernateAudit.getAuditType(session, auditableInformationProvider.getAuditTypeClassName(dataStore.getHibernateConfiguration(), entityNameStrategy.toEntityName(parent.eClass())));
            Model1Parent auditEObject = (Model1Parent) HibernateAuditInstantiator.getEntity(session, auditType, parent.getId() + "", latestTransactionIdAfterInsert);

            assertEquals(resource, loadedXmi, EMFUtil.toXMI(auditEObject, dataStore.getEPackages()));
            tx.commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }

    @Test(dataProvider = "xmiTemplates", enabled = true)
    public void simpleInsertDelete(String resource) {
    	EntityNameStrategy entityNameStrategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);
    	AuditableInformationProvider auditableInformationProvider = AuditListener.getAuditConfiguration(dataStore.getHibernateConfiguration()).getExtensionManager().getAuditableInformationProvider();

        String loadedXmi = loadResource(resource);
        Model1Parent parent = (Model1Parent) EMFUtil.toEObject(loadedXmi, true, dataStore.getEPackages()).get(0);

        Session session = null;
        try {
            session = dataStore.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();

            Long latestTransactionIdBeforeInsert = HibernateAudit.getLatestAuditTransactionId(session);
            Assert.assertNotNull(latestTransactionIdBeforeInsert);

            session.save(parent);
            for (Model1Child child : parent.getChildren()) {
                session.save(child);
            }

            tx.commit();

            tx = session.beginTransaction();
            EntityNameStrategy strategy = dataStore.getExtensionManager().getExtension(EntityNameStrategy.class);
            Model1Parent storedParent = (Model1Parent) session.get(strategy.toEntityName(Model1Package.eINSTANCE.getModel1Parent()), parent.getId());
            Assert.assertNotNull(storedParent);

            Model1Child child = storedParent.getChildren().remove(0);

            session.delete(child);
            session.flush();
            loadedXmi = EMFUtil.toXMI(storedParent, dataStore.getEPackages());
            tx.commit();

            tx = session.beginTransaction();
            Long latestTransactionIdAfterInsert = HibernateAudit.getLatestAuditTransactionId(session);

            Assert.assertNotNull(latestTransactionIdAfterInsert);
            Assert.assertTrue(latestTransactionIdAfterInsert > latestTransactionIdBeforeInsert);

            AuditType auditType = HibernateAudit.getAuditType(session, auditableInformationProvider.getAuditTypeClassName(dataStore.getHibernateConfiguration(), entityNameStrategy.toEntityName(parent.eClass())));
            Model1Parent auditEObject = (Model1Parent) HibernateAuditInstantiator.getEntity(session, auditType, parent.getId() + "", latestTransactionIdAfterInsert);

            assertEquals(resource, loadedXmi, EMFUtil.toXMI(auditEObject, dataStore.getEPackages()));
            tx.commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
