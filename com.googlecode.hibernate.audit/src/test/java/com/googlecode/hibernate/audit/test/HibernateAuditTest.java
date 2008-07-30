package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.listener.Listeners;
import com.googlecode.hibernate.audit.listener.AuditEventListener;

import java.util.Set;
import java.util.List;
import java.lang.reflect.Method;
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
    public void testEnableDisable() throws Exception
    {
        assert !HibernateAudit.isStarted();

        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            assert HibernateAudit.isEnabled(sf);

            // make sure that all available HBA listeners are installed
            Set<String> aets = Listeners.getAuditedEventTypes();

            assert !aets.isEmpty();

            outer: for(String aet: aets)
            {
                log.debug("verifying '" + aet + "' listeners");

                Method m = Listeners.getEventListenersGetter(aet);
                Object[] listeners = (Object[])m.invoke(((SessionFactoryImpl)sf).getEventListeners());

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
            HibernateAudit.enable(sf);

            assert HibernateAudit.disableAll();

            // make sure none of the audit listeners are still registered

            assert !Listeners.ALL_EVENT_TYPES.isEmpty();

            for(String et: Listeners.ALL_EVENT_TYPES)
            {
                log.debug("verifying '" + et + "' listeners");

                Method m = Listeners.getEventListenersGetter(et);
                Object[] listeners = (Object[])m.invoke(((SessionFactoryImpl)sf).getEventListeners());

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
            assert !HibernateAudit.disableAll();
        }
        finally
        {
            HibernateAudit.disableAll();
            
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testQueryOnDisabledRuntime() throws Exception
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
        SessionFactory sf = null;
        SessionFactory sf2 = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            assert HibernateAudit.isEnabled(sf);

            sf2 = config.buildSessionFactory();

            assert !HibernateAudit.isEnabled(sf2);

            assert HibernateAudit.disableAll();
        }
        finally
        {
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;
        SessionFactory sf2 = null;

        try
        {
            sf = config.buildSessionFactory();
            sf2 = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            assert HibernateAudit.isEnabled(sf);
            assert !HibernateAudit.isEnabled(sf2);

            // make sure that all available HBA listeners are installed
            Set<String> aets = Listeners.getAuditedEventTypes();

            assert !aets.isEmpty();

            outer: for(String aet: aets)
            {
                log.debug("verifying '" + aet + "' listeners");

                Method m = Listeners.getEventListenersGetter(aet);
                Object[] listeners = (Object[])m.invoke(((SessionFactoryImpl)sf).getEventListeners());

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


            HibernateAudit.enable(sf2);

            assert HibernateAudit.isEnabled(sf);
            assert HibernateAudit.isEnabled(sf2);

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
            HibernateAudit.enable(sf);
            HibernateAudit.enable(sf2);

            assert HibernateAudit.disable(sf);
            assert !HibernateAudit.isEnabled(sf);
            assert HibernateAudit.isEnabled(sf2);

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

            assert HibernateAudit.disable(sf2);
            assert !HibernateAudit.isEnabled(sf);
            assert !HibernateAudit.isEnabled(sf2);

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
            assert !HibernateAudit.disable(sf);
            assert !HibernateAudit.disable(sf2);
            assert !HibernateAudit.disableAll();
        }
        finally
        {
            HibernateAudit.disableAll();

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
    public void testQueryOnEmptyAuditState() throws Exception
    {
        Configuration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            try
            {
                List ats = HibernateAudit.query("from com.googlecode.hibernate.audit.model.AuditTransaction");
                assert ats.isEmpty();
            }
            finally
            {
                assert HibernateAudit.disableAll();
            }
        }
        finally
        {
            HibernateAudit.disableAll();

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
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            HibernateAudit.enable(sf);

            Principal p = HibernateAudit.getPrincipal();

            assert p == null;

            assert HibernateAudit.disableAll();
        }
        finally
        {
            HibernateAudit.disableAll();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
