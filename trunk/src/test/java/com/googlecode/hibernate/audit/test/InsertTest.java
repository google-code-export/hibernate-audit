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

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import com.googlecode.hibernate.audit.test.model1.Model1Package;

public class InsertTest extends AbstractHibernateAuditTest {

    @DataProvider(name = "xmiTemplates")
    public Object[][] getXmiTemplates() throws Exception {
        Object[][] data = new Object[1][1];
        data[0][0] = "xmi/Model1Parent.xmi";
        return data;
    }

    @Test(dataProvider = "xmiTemplates")
    public void simpleInsert(String resource) {

        EList<EObject> eObjects = EMFUtil.toEObject(loadResource(resource), true, new EPackage[] { Model1Package.eINSTANCE });

        Session session = null;
        try {
            session = dataStore.getSessionFactory().openSession();
            Transaction tx = session.beginTransaction();
            for (EObject obj : eObjects) {
                session.save(obj);
            }

            tx.commit();
        } finally {
            if (session != null) {
                session.close();
            }
        }
    }
}
