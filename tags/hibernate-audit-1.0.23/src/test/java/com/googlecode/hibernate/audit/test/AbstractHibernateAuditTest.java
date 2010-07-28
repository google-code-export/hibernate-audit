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

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.emf.compare.match.metamodel.MatchModel;
import org.eclipse.emf.compare.match.metamodel.UnmatchElement;
import org.eclipse.emf.compare.match.service.MatchService;
import org.eclipse.emf.compare.util.ModelUtils;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.teneo.hibernate.HbDataStore;
import org.eclipse.emf.teneo.hibernate.HbDataStoreFactory;
import org.eclipse.emf.teneo.hibernate.HbHelper;
import org.eclipse.emf.teneo.hibernate.HbSessionDataStore;
import org.hibernate.event.PostCollectionRecreateEventListener;
import org.hibernate.event.PostDeleteEventListener;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.event.PostUpdateEventListener;
import org.hibernate.event.PreCollectionRemoveEventListener;
import org.hibernate.event.PreCollectionUpdateEventListener;
import org.hibernate.transaction.JDBCTransaction;
import org.testng.Assert;

import com.googlecode.hibernate.audit.listener.AuditListener;
import com.googlecode.hibernate.audit.test.model1.Model1Package;

public abstract class AbstractHibernateAuditTest {
    protected final Logger LOG = Logger.getLogger(getClass());

    private static HbDataStoreFactory emfDataStoreFactory = new HbDataStoreFactory() {
        public HbDataStore createHbDataStore() {
            return new SessionFactory();
        }
    };

    private static final Logger HIBERNATE_TRANSACTION_LOG = Logger.getLogger(JDBCTransaction.class);

    
    protected static final HbDataStore dataStore = init();
    
    static {
        // this will ensure that we will get concurrency exceptions
        interceptLog(HIBERNATE_TRANSACTION_LOG);
    }
    
    private static void interceptLog(Logger logger) {
        if (Level.OFF.equals(logger.getLevel())) {
            logger.setLevel(Level.ERROR);
        }

        logger.addAppender(new AppenderSkeleton() {

            @Override
            public boolean requiresLayout() {
                return false;
            }

            @Override
            public void close() {
            }

            @Override
            protected void append(LoggingEvent event) {
                if (event.getThrowableInformation() != null && event.getThrowableInformation().getThrowable() instanceof RuntimeException) {
                    throw (RuntimeException) event.getThrowableInformation().getThrowable();
                }
            }
        });
    }

    protected String loadResource(String xmi) {
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(xmi);
        if (in == null) {
            throw new IllegalArgumentException("Unable to locate resource " + xmi);
        }
        String result = null;
        try {
            result = readContentAsString(new BufferedReader(new InputStreamReader(in)));
        } catch (IOException ex) {
            throw new IllegalArgumentException("Unable to read resource " + xmi, ex);
        }

        return result;
    }

    private static String readContentAsString(Reader reader) throws IOException {
        StringBuffer result = new StringBuffer(1000);
        char[] buf = new char[1024];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            result.append(buf, 0, numRead);
        }
        reader.close();
        return result.toString();
    }

    protected void assertEquals(String resourceURI, String loadedXmi, String storedXmi) {
        try {
            final ResourceSet resourceSet = new ResourceSetImpl();
            for (EPackage ePackage : dataStore.getEPackages()) {
                resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
            }
            
            final EObject model1 = ModelUtils.load(new ByteArrayInputStream(loadedXmi.getBytes()), "loadedXmi.xmi", resourceSet);
            final EObject model2 = ModelUtils.load(new ByteArrayInputStream(storedXmi.getBytes()), "storedXmi.xmi", resourceSet);

            // Matching model elements
            Map<String, Object> options = new HashMap<String, Object>();
            options.put("match.ignore.id", Boolean.TRUE);
            options.put("match.ignore.xmi.id", Boolean.TRUE);

            MatchModel match = MatchService.doContentMatch(model1, model2, options);
            List<UnmatchElement> elements = match.getUnmatchedElements();

            Assert.assertTrue(elements.isEmpty(), "resourceURI=" + resourceURI + ",loadedXmi=\n" + loadedXmi + "\nstoredXmi=\n" + storedXmi + "\n");
        } catch (IOException e) {
            LOG.error(e);
        } catch (InterruptedException e) {
            LOG.error(e);
        }
    }

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

            // explicitly cast the auditListener so that the generic function
            // will have the correct type.
            getConfiguration().getEventListeners().setPostInsertEventListeners(
                    addListener(getConfiguration().getEventListeners().getPostInsertEventListeners(), (PostInsertEventListener) auditListener));
            getConfiguration().getEventListeners().setPostUpdateEventListeners(
                    addListener(getConfiguration().getEventListeners().getPostUpdateEventListeners(), (PostUpdateEventListener) auditListener));
            getConfiguration().getEventListeners().setPostDeleteEventListeners(
                    addListener(getConfiguration().getEventListeners().getPostDeleteEventListeners(), (PostDeleteEventListener) auditListener));

            getConfiguration().getEventListeners().setPreCollectionUpdateEventListeners(
                    addListener(getConfiguration().getEventListeners().getPreCollectionUpdateEventListeners(), (PreCollectionUpdateEventListener) auditListener));
            getConfiguration().getEventListeners().setPreCollectionRemoveEventListeners(
                    addListener(getConfiguration().getEventListeners().getPreCollectionRemoveEventListeners(), (PreCollectionRemoveEventListener) auditListener));
            getConfiguration().getEventListeners().setPostCollectionRecreateEventListeners(
                    addListener(getConfiguration().getEventListeners().getPostCollectionRecreateEventListeners(), (PostCollectionRecreateEventListener) auditListener));

            setSessionFactory(getConfiguration().buildSessionFactory());
        }

        private <T> T[] addListener(T[] listeners, T listener) {
            int length = listeners != null ? listeners.length + 1 : 1;
            T[] newListeners = (T[]) Array.newInstance(listener.getClass(), length);
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
