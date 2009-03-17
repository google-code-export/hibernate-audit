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

import java.io.IOException;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.teneo.hibernate.HbDataStore;
import org.eclipse.emf.teneo.hibernate.HbHelper;

import com.googlecode.hibernate.audit.test.model1.Model1Package;

public abstract class AbstractHibernateAuditTest {
    protected final Logger LOG = Logger.getLogger(getClass());

    protected final static HbDataStore dataStore = init();

    // init method
    private static HbDataStore init() {
        try {
            // Create the DataStore.
            final String dataStoreName = "AuditDataStore";
            HbDataStore dataStore = HbHelper.INSTANCE.createRegisterDataStore(dataStoreName);

            // Configure the EPackages used by this DataStore.
            dataStore.setEPackages(new EPackage[] {Model1Package.eINSTANCE});

            // Initialize the DataStore. This sets up the Hibernate mapping and
            // creates the corresponding tables in the database.
            Properties prop = new Properties();
            prop.load(AbstractHibernateAuditTest.class.getResourceAsStream("/hibernate.properties"));
            dataStore.setHibernateProperties(prop);

            dataStore.initialize();

            return dataStore;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
     
}
