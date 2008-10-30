package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Settings;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.DelegateConnectionProvider;
import com.googlecode.hibernate.audit.HibernateAuditEnvironment;
import com.googlecode.hibernate.audit.model.Entities;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.listener.Listeners;
import com.googlecode.hibernate.audit.listener.AuditEventListener;

import java.util.Set;
import java.util.List;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.InvocationHandler;
import java.security.Principal;

/**
 * Tests the runtime API
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class HibernateAuditTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAuditTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testEnableOnProxy() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;


        SessionFactory proxy =
            (SessionFactory)Proxy.newProxyInstance(HibernateAudit.class.getClassLoader(),
                                                   new Class[] {SessionFactory.class},
                                                   new InvocationHandler()
                                                   {
                                                       public Object invoke(Object proxy,
                                                                            Method method,
                                                                            Object[] args)
                                                           throws Throwable
                                                       {
                                                           throw new RuntimeException(
                                                               "NOT YET IMPLEMENTED");
                                                       }
                                                   });
        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            HibernateAudit.startRuntime(sf.getSettings());
            
            HibernateAudit.register(proxy);
            throw new Error("should've failed");
        }
        catch(IllegalArgumentException e)
        {
            log.debug(">>>> " + e.getMessage());
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testRegisterUnregister() throws Exception
    {
        assert !HibernateAudit.isStarted();

        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            HibernateAudit.startRuntime(settings);

            HibernateAudit.register(sf);

            assert HibernateAudit.isRegistered(sf);

            // make sure that all available HBA listeners are installed
            Set<String> aets = Listeners.getAuditedEventTypes();

            assert !aets.isEmpty();

            outer: for(String aet: aets)
            {
                log.debug("verifying '" + aet + "' listeners");

                Method m = Listeners.getEventListenersGetter(aet);
                Object[] listeners =
                    (Object[])m.invoke(((SessionFactoryImpl)sf).getEventListeners());

                assert listeners.length != 0;
                Class c = Listeners.getAuditEventListenerClass(aet);

                for(Object o: listeners)
                {
                    if (c.isInstance(o))
                    {
                        // found, all good
                        continue outer;
                    }
                }

                throw new Exception("Did not find a " + aet + " audit listener");
            }

            // testing noop behavior
            HibernateAudit.register(sf);

            assert HibernateAudit.unregisterAll();

            // make sure none of the audit listeners are still registered

            assert !Listeners.ALL_EVENT_TYPES.isEmpty();

            for(String et: Listeners.ALL_EVENT_TYPES)
            {
                log.debug("verifying '" + et + "' listeners");

                Method m = Listeners.getEventListenersGetter(et);
                Object[] listeners =
                    (Object[])m.invoke(((SessionFactoryImpl)sf).getEventListeners());

                if (listeners == null)
                {
                    continue; // we're ok
                }

                for(Object o: listeners)
                {
                    assert !(o instanceof AuditEventListener);
                }
            }

            // testing noop behavior
            assert !HibernateAudit.unregisterAll();
        }
        finally
        {
            HibernateAudit.stopRuntime();
            
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testFailedStartup() throws Exception
    {
        assert HibernateAudit.getManager() == null;

        try
        {
            HibernateAudit.startRuntime(null);
            throw new Error("should've failed");
        }
        catch(NullPointerException e)
        {
            log.debug(e);
            assert HibernateAudit.getManager() == null;
        }
        finally
        {
            HibernateAudit.stopRuntime();
        }
    }

    @Test(enabled = true)
    public void testQueryOnStoppedRuntime() throws Exception
    {
        assert !HibernateAudit.isStarted();

        try
        {
            HibernateAudit.query("form AuditTransaction");
            throw new Error("Should've failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testIsEnabledOnDifferentSessionFactory() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;
        SessionFactory sf2 = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            HibernateAudit.startRuntime(settings);

            HibernateAudit.register(sf);

            assert HibernateAudit.isRegistered(sf);

            sf2 = config.buildSessionFactory();

            assert !HibernateAudit.isRegistered(sf2);

            assert HibernateAudit.unregisterAll();
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }

            if (sf2 != null)
            {
                sf2.close();
            }
        }
    }

    @Test(enabled = true)
    public void testEnableDisableTwoSessionFactories() throws Exception
    {
        assert !HibernateAudit.isStarted();

        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;
        SessionFactory sf2 = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            sf2 = config.buildSessionFactory();

            HibernateAudit.startRuntime(settings);
            HibernateAudit.register(sf);

            assert HibernateAudit.isRegistered(sf);
            assert !HibernateAudit.isRegistered(sf2);

            // make sure that all available HBA listeners are installed
            Set<String> aets = Listeners.getAuditedEventTypes();

            assert !aets.isEmpty();

            outer: for(String aet: aets)
            {
                log.debug("verifying '" + aet + "' listeners");

                Method m = Listeners.getEventListenersGetter(aet);
                Object[] listeners =
                    (Object[])m.invoke(((SessionFactoryImpl)sf).getEventListeners());

                assert listeners.length != 0;
                Class c = Listeners.getAuditEventListenerClass(aet);

                for(Object o: listeners)
                {
                    if (c.isInstance(o))
                    {
                        // found, all good
                        continue outer;
                    }
                }

                throw new Exception("Did not find a " + aet + " audit listener");
            }

            HibernateAudit.register(sf2);

            assert HibernateAudit.isRegistered(sf);
            assert HibernateAudit.isRegistered(sf2);

            // make sure that all available HBA listeners are installed
            outer: for(String aet: aets)
            {
                log.debug("verifying '" + aet + "' listeners");

                Method m = Listeners.getEventListenersGetter(aet);
                Object[] listeners =
                    (Object[])m.invoke(((SessionFactoryImpl)sf2).getEventListeners());

                assert listeners.length != 0;
                Class c = Listeners.getAuditEventListenerClass(aet);

                for(Object o: listeners)
                {
                    if (c.isInstance(o))
                    {
                        // found, all good
                        continue outer;
                    }
                }

                throw new Exception("Did not find a " + aet + " audit listener on sf2");
            }

            // testing noop behavior
            HibernateAudit.register(sf);
            HibernateAudit.register(sf2);

            assert HibernateAudit.unregister(sf);
            assert !HibernateAudit.isRegistered(sf);
            assert HibernateAudit.isRegistered(sf2);

            // make sure none of the audit listeners are still registered on the disabled sf

            assert !Listeners.ALL_EVENT_TYPES.isEmpty();

            for(String et: Listeners.ALL_EVENT_TYPES)
            {
                log.debug("verifying '" + et + "' listeners");

                Method m = Listeners.getEventListenersGetter(et);
                Object[] listeners =
                    (Object[])m.invoke(((SessionFactoryImpl)sf).getEventListeners());

                if (listeners == null)
                {
                    continue; // we're ok
                }

                for(Object o: listeners)
                {
                    assert !(o instanceof AuditEventListener);
                }
            }

            // however make sure the listeners are still registered with sf2

            outer: for(String aet: aets)
            {
                log.debug("verifying '" + aet + "' listeners");

                Method m = Listeners.getEventListenersGetter(aet);
                Object[] listeners =
                    (Object[])m.invoke(((SessionFactoryImpl)sf2).getEventListeners());

                assert listeners.length != 0;
                Class c = Listeners.getAuditEventListenerClass(aet);

                for(Object o: listeners)
                {
                    if (c.isInstance(o))
                    {
                        // found, all good
                        continue outer;
                    }
                }

                throw new Exception("Did not find a " + aet + " audit listener on sf2");
            }

            assert HibernateAudit.unregister(sf2);
            assert !HibernateAudit.isRegistered(sf);
            assert !HibernateAudit.isRegistered(sf2);

            // make sure none of the audit listeners are still registered on the disabled sf2

            assert !Listeners.ALL_EVENT_TYPES.isEmpty();

            for(String et: Listeners.ALL_EVENT_TYPES)
            {
                log.debug("verifying '" + et + "' listeners");

                Method m = Listeners.getEventListenersGetter(et);
                Object[] listeners =
                    (Object[])m.invoke(((SessionFactoryImpl)sf2).getEventListeners());

                if (listeners == null)
                {
                    continue; // we're ok
                }

                for(Object o: listeners)
                {
                    assert !(o instanceof AuditEventListener);
                }
            }

            // testing noop behavior
            assert !HibernateAudit.unregister(sf);
            assert !HibernateAudit.unregister(sf2);
            assert !HibernateAudit.unregisterAll();
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }

            if (sf2 != null)
            {
                sf2.close();
            }
        }
    }

    @Test(enabled = true)
    public void testQueryOnEmptyAuditState_NoRegisteredSessionFactory() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            HibernateAudit.startRuntime(settings);

            try
            {
                List ats = HibernateAudit.
                    query("from com.googlecode.hibernate.audit.model.AuditTransaction");

                assert ats.isEmpty();
            }
            finally
            {
                assert !HibernateAudit.unregisterAll();
            }
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testQueryOnEmptyAuditState() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            HibernateAudit.startRuntime(settings);

            HibernateAudit.register(sf);

            try
            {
                List ats = HibernateAudit.
                    query("from com.googlecode.hibernate.audit.model.AuditTransaction");

                assert ats.isEmpty();
            }
            finally
            {
                assert HibernateAudit.unregisterAll();
            }
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testGetNullSecurityInformationProvider() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            HibernateAudit.startRuntime(settings);

            HibernateAudit.register(sf);

            Principal p = HibernateAudit.getManager().getPrincipal();

            assert p == null;

            assert HibernateAudit.unregisterAll();
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testEnableDisable_InternalStatefulSession() throws Exception
    {
        assert !HibernateAudit.isStarted();

        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            assert HibernateAudit.getManager() == null;

            HibernateAudit.startRuntime(settings);

            HibernateAudit.register(sf);

            assert HibernateAudit.isRegistered(sf);

            SessionFactoryImpl internal = HibernateAudit.getManager().getSessionFactory();

            // make sure (somehow superfluously) that all mappings are there
            Set<Class> entities = Entities.getAuditEntities();

            for(Class e: entities)
            {
                ClassMetadata cm = internal.getClassMetadata(e);
                assert cm != null;
            }

            DelegateConnectionProvider dcp =
                (DelegateConnectionProvider)internal.getConnectionProvider();
            ConnectionProvider icp = dcp.getDelegate();
            ConnectionProvider cp = ((SessionFactoryImpl)sf).getConnectionProvider();

            assert icp == cp;

            assert HibernateAudit.unregister(sf);

            assert HibernateAudit.getManager() != null;

            HibernateAudit.stopRuntime();

            assert HibernateAudit.getManager() == null;
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testEnable_HBM2DDL_AUTO() throws Exception
    {
        assert !HibernateAudit.isStarted();

        String originalValue = System.getProperty(HibernateAuditEnvironment.HBM2DDL_AUTO);
        log.debug(originalValue);

        SessionFactoryImplementor sf = null;

        try
        {
            Configuration config = new AnnotationConfiguration();
            config.configure(getHibernateConfigurationFileName());
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            System.setProperty(HibernateAuditEnvironment.HBM2DDL_AUTO, "some complete junk");

            // this works, but logs a warning
            HibernateAudit.startRuntime(settings);
        }
        finally
        {
            if (originalValue != null)
            {
                System.setProperty(HibernateAuditEnvironment.HBM2DDL_AUTO, originalValue);
            }
            else
            {
                System.clearProperty(HibernateAuditEnvironment.HBM2DDL_AUTO);
            }

            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testConfigurableHBAProperty() throws Exception
    {
        assert !HibernateAudit.isStarted();

        assert System.getProperty("hibernate.jdbc.batch_size") == null;
        assert System.getProperty("hba.jdbc.batch_size") == null;

        SessionFactoryImplementor sf = null;

        try
        {
            Configuration config = new AnnotationConfiguration();
            config.configure(getHibernateConfigurationFileName());
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            System.setProperty("hba.jdbc.batch_size", Integer.toString(72));

            HibernateAudit.startRuntime(settings);

            SessionFactoryImpl isf = HibernateAudit.getManager().getSessionFactory();
            settings = isf.getSettings();
            assert 72 == settings.getJdbcBatchSize();
        }
        finally
        {
            assert "72".equals(System.clearProperty("hba.jdbc.batch_size"));

            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // TODO commented out due to Hibernate 3.3.1.GA bug https://jira.novaordis.org/browse/HBA-148
//    @Test(enabled = true)
//    public void testConfigurableJtaHBAProperty() throws Exception
//    {
//        assert !HibernateAudit.isStarted();
//
//        assert System.getProperty("jta.UserTransaction") == null;
//        assert System.getProperty("hba.jta.UserTransaction") == null;
//
//        SessionFactoryImplementor sf = null;
//
//        try
//        {
//            Configuration config = new AnnotationConfiguration();
//            config.configure(getHibernateConfigurationFileName());
//            sf = (SessionFactoryImplementor)config.buildSessionFactory();
//            Settings settings = sf.getSettings();
//
//            System.setProperty("hba.jta.UserTransaction", "/UserTransactionDuJour");
//
//            HibernateAudit.startRuntime(settings);
//
//            // try to create a transaction with the bogus user transaction
//
//            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
//            Session s = isf.openSession();
//
//            try
//            {
//                s.beginTransaction();
//                throw new Error("should've failed");
//            }
//            catch(TransactionException e)
//            {
//                Throwable t = e.getCause();
//                assert t instanceof NameNotFoundException;
//            }
//        }
//        finally
//        {
//            assert "/UserTransactionDuJour".equals(System.clearProperty("hba.jta.UserTransaction"));
//            assert System.getProperty("jta.UserTransaction") == null;
//
//            HibernateAudit.stopRuntime();
//
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }

    @Test(enabled = true)
    public void testBogusHBAProperty() throws Exception
    {
        assert !HibernateAudit.isStarted();

        // make sure a bogus property doesn't break the initialization process

        assert System.getProperty("hibernate.totally.bogus.property") == null;

        SessionFactoryImplementor sf = null;

        try
        {
            Configuration config = new AnnotationConfiguration();
            config.configure(getHibernateConfigurationFileName());
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            System.setProperty("hibernate.totally.bogus.property", "blah");

            HibernateAudit.startRuntime(settings);
        }
        finally
        {
            assert "blah".equals(System.clearProperty("hibernate.totally.bogus.property"));

            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testRegister_RuntimeNotStarted() throws Exception
    {
        try
        {
            HibernateAudit.register(new MockSessionFactory());
            throw new Error("should've failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(">>> " + e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testStartRuntime() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            assert !HibernateAudit.isStarted();

            HibernateAudit.startRuntime(settings);

            assert HibernateAudit.isStarted();

            Manager manager = HibernateAudit.getManager();

            assert manager != null;
            assert manager.getSessionFactory() != null;
            assert manager.getAuditedSessionFactories().isEmpty();

            // this should be a noop
            HibernateAudit.startRuntime(settings);
            assert HibernateAudit.isStarted();

        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testStopRuntime() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            assert !HibernateAudit.isStarted();

            HibernateAudit.startRuntime(settings);

            assert HibernateAudit.isStarted();

            HibernateAudit.stopRuntime();
            assert !HibernateAudit.isStarted();

            HibernateAudit.stopRuntime();
            assert !HibernateAudit.isStarted();
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testConsecutiveRegistrationsOfSameSessionFactory() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();
            Settings settings = sf.getSettings();

            HibernateAudit.startRuntime(settings);

            HibernateAudit.register(sf);

        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testVersion() throws Exception
    {
        String version = HibernateAudit.getVersion();
        String mavenTestHbaVersion = System.getProperty("maven.test.hba.version");
        log.debug(">>> HBA version:            " + version);
        log.debug(">>> maven.test.hba.version: " + mavenTestHbaVersion);
        assert mavenTestHbaVersion.equals(version);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
