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
import org.eclipse.emf.teneo.hibernate.HbDataStoreFactory;
import org.eclipse.emf.teneo.hibernate.HbHelper;
import org.eclipse.emf.teneo.hibernate.HbSessionDataStore;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreCollectionRecreateEventListener;
import org.hibernate.event.PreCollectionRemoveEventListener;
import org.hibernate.event.PreCollectionUpdateEventListener;

import com.googlecode.hibernate.audit.listener.AuditListener;
import com.googlecode.hibernate.audit.test.model1.Model1Package;

public abstract class AbstractHibernateAuditTest {
    protected final Logger LOG = Logger.getLogger(getClass());

    private static HbDataStoreFactory emfDataStoreFactory = new HbDataStoreFactory() {
        public HbDataStore createHbDataStore() {
            return new SessionFactory();
        }
    };

    protected final static HbDataStore dataStore = init();

    // init method
    private static HbDataStore init() {
        try {
            // Create the DataStore.
            final String dataStoreName = "AuditDataStore";
            HbHelper.setHbDataStoreFactory(emfDataStoreFactory);
            HbDataStore dataStore = HbHelper.INSTANCE.createRegisterDataStore(dataStoreName);

            // Configure the EPackages used by this DataStore.
            dataStore.setEPackages(new EPackage[] { Model1Package.eINSTANCE });

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

    private static class SessionFactory extends HbSessionDataStore {
        @Override
        protected void buildSessionFactory() {
            // programatically add the audit listener
            AuditListener auditListener = new AuditListener();

            getConfiguration().getEventListeners().setPostInsertEventListeners(
                    (PostInsertEventListener[]) addListener(getConfiguration().getEventListeners().getPostInsertEventListeners(), auditListener));
            getConfiguration().getEventListeners().setPostUpdateEventListeners(
                    (PostUpdateEventListener[]) addListener(getConfiguration().getEventListeners().getPostUpdateEventListeners(), auditListener));
            getConfiguration().getEventListeners().setPostDeleteEventListeners(
                    (PostDeleteEventListener[]) addListener(getConfiguration().getEventListeners().getPostDeleteEventListeners(), auditListener));

            getConfiguration().getEventListeners().setPreCollectionUpdateEventListeners(
                    (PreCollectionUpdateEventListener[]) addListener(getConfiguration().getEventListeners().getPreCollectionUpdateEventListeners(), auditListener));
            getConfiguration().getEventListeners().setPreCollectionRemoveEventListeners(
                    (PreCollectionRemoveEventListener[]) addListener(getConfiguration().getEventListeners().getPreCollectionRemoveEventListeners(), auditListener));
            getConfiguration().getEventListeners().setPreCollectionRecreateEventListeners(
                    (PreCollectionRecreateEventListener[]) addListener(getConfiguration().getEventListeners().getPreCollectionRecreateEventListeners(), auditListener));

            setSessionFactory(getConfiguration().buildSessionFactory());
        }

        private Object[] addListener(Object[] listeners, Object listener) {
            int length = listeners != null ? listeners.length + 1 : 1;
            Object[] newListeners = new Object[length + 1];
            for (int i = 0; i < length; i++) {
                if (listeners != null && listeners.length > i) {
                    newListeners[i] = listeners[i];
                }
            }
            newListeners[length - 1] = listener;

            return newListeners;
        }
    }
}
