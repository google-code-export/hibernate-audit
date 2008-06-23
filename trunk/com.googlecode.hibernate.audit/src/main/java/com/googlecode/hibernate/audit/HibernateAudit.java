package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.event.EventListeners;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.listener.AuditEventListener;
import com.googlecode.hibernate.audit.listener.Listeners;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Array;

/**
 * The main programmatic entry point. This class allows turning audit on/off at runtime, and various
 * other configuration options.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class HibernateAudit
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAudit.class);

    // Static --------------------------------------------------------------------------------------

    private static HibernateAudit singleton;

    public static synchronized boolean isEnabled()
    {
        return singleton != null;
    }

    /**
     * Turns audit on.
     *
     * The persistence unit being audited will be used to persist the audit data as well.
     */
    public static void enable(SessionFactory auditedSessionFactory) throws Exception
    {
        enable(auditedSessionFactory, null);
    }

    /**
     * Turns audit on.
     *
     * The caller has a choice in using a different persistence unit to store audited data.
     *
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     * @param secondaryPersistenceUnitConfigFile - the resource which contains the configuration for
     *        the secondary persistence unit (used to persist audit data). A null resource means
     *        that there is no secondary persistence unit, the audited persistence unit will be used
     *        to persist audit data as well.
     */
    public static synchronized void enable(SessionFactory auditedSessionFactory,
                                           String secondaryPersistenceUnitConfigFile)
        throws Exception
    {
        if (singleton != null)
        {
            log.debug(singleton + " already enabled");
            return;
        }

        singleton = new HibernateAudit(auditedSessionFactory, secondaryPersistenceUnitConfigFile);
        singleton.start();
    }

    /**
     * Turns audit off.
     *
     * @return true if calling this method ended up in audit being disabled, or false if there was
     *         no active audit runtime to disable.
     */
    public static boolean disable() throws Exception
    {
        if (singleton == null)
        {
            return false;
        }

        singleton.stop();
        singleton = null;
        return true;
    }

    // Attributes ----------------------------------------------------------------------------------

    private SessionFactoryImpl auditedSessionFactory;
    private String secondaryConfigurationResource;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     * @param resource - the resource which contains the configuration for the secondary persistence
     *        unit (used to persist audit data). A null resource means that there is no secondary
     *        persistence unit, the audited persistence unit will be used to persist audit data as
     *        well.
     */
    private HibernateAudit(SessionFactory auditedSessionFactory, String resource) throws Exception
    {
        this.auditedSessionFactory = (SessionFactoryImpl)auditedSessionFactory;
        this.secondaryConfigurationResource = resource;
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "HibernateAuditRuntime[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private void start() throws Exception
    {
        log.debug(this + " starting ...");

        // if using a different persistence unit, initialize it first

        if (secondaryConfigurationResource != null)
        {
            //Configuration secondary = new AnnotationConfiguration()

//    /**
//     * Use the mappings and properties specified in the given application
//     * resource. The format of the resource is defined in
//     * <tt>hibernate-configuration-3.0.dtd</tt>.
//     * <p/>
//     * The resource is found via <tt>getConfigurationInputStream(resource)</tt>.
//     */
//    public Configuration configure(String resource) throws HibernateException {
//        log.info( "configuring from resource: " + resource );
//        InputStream stream = getConfigurationInputStream( resource );
//        return doConfigure( stream, resource );
//    }
//
//    /**
//     * Use the mappings and properties specified in the given document.
//     * The format of the document is defined in
//     * <tt>hibernate-configuration-3.0.dtd</tt>.
//     *
//     * @param url URL from which you wish to load the configuration
//     * @return A configuration configured via the file
//     * @throws HibernateException
//     */
//    public Configuration configure(URL url) throws HibernateException {
//        log.info( "configuring from url: " + url.toString() );
//        try {
//            return doConfigure( url.openStream(), url.toString() );
//        }
//        catch (IOException ioe) {
//            throw new HibernateException( "could not configure from URL: " + url, ioe );
//        }
//    }
//
//    /**
//     * Use the mappings and properties specified in the given application
//     * file. The format of the file is defined in
//     * <tt>hibernate-configuration-3.0.dtd</tt>.
//     *
//     * @param configFile <tt>File</tt> from which you wish to load the configuration
//     * @return A configuration configured via the file
//     * @throws HibernateException
//     */
//    public Configuration configure(File configFile) throws HibernateException {
//        log.info( "configuring from file: " + configFile.getName() );
//        try {
//            return doConfigure( new FileInputStream( configFile ), configFile.toString() );
//        }
//        catch (FileNotFoundException fnfe) {
//            throw new HibernateException( "could not find file: " + configFile, fnfe );
//        }
//    }

            throw new Exception("NOT YET IMPLEMENTED");
        }

        installAuditListeners(auditedSessionFactory);

        log.debug(this + " started");
    }

    private void stop() throws Exception
    {
        log.debug(this + " stopping ...");

        uninstallAuditListeners(auditedSessionFactory);

        //TODO if using a different persistence unit, clean-up that

        auditedSessionFactory = null;

        log.debug(this + " stopped");
    }

    private void installAuditListeners(SessionFactoryImpl sf) throws Exception
    {
        EventListeners els = sf.getEventListeners();

        // at this stage, we should not have any registered audit listeners, but trust and verify
        for(String eventType: Listeners.ALL_EVENT_TYPES)
        {
            Method getter = Listeners.getEventListenersGetter(eventType);
            Object[] listeners = (Object[])getter.invoke(els);
            for(Object listener: listeners)
            {
                if (listener instanceof AuditEventListener)
                {
                    throw new IllegalStateException("Hibernate audit already enabled, " +
                                                    "found " + listener);
                }
            }
        }

        Set<String> eventTypes = Listeners.getAuditedEventTypes();

        for(String auditEventType: eventTypes)
        {
            Method getter = Listeners.getEventListenersGetter(auditEventType);
            Method setter = Listeners.getEventListenersSetter(auditEventType);

            // we expect a listener array here, anything else would be invalid state
            Object[] listeners = (Object[])getter.invoke(els);
            Class hibernateListenerInteface = els.getListenerClassFor(auditEventType);
            Object[] newListeners =
                (Object[])Array.newInstance(hibernateListenerInteface, listeners.length + 1);
            System.arraycopy(listeners, 0, newListeners, 0, listeners.length);

            Class c = Listeners.getAuditEventListenerClass(auditEventType);
            AuditEventListener ael = (AuditEventListener)c.newInstance();

            newListeners[newListeners.length - 1] = ael;
            setter.invoke(els, ((Object)newListeners));
        }

        log.debug(this + " installed audit listeners");
    }

    private void uninstallAuditListeners(SessionFactoryImpl sf) throws Exception
    {
        EventListeners els = sf.getEventListeners();

        // scan all listener and uninstall all AuditEventListeners
        for(String eventType: Listeners.ALL_EVENT_TYPES)
        {
            Method getter = Listeners.getEventListenersGetter(eventType);
            Object[] listeners = (Object[])getter.invoke(els);

            boolean needUninstall = false;
            for(Object listener: listeners)
            {
                if (listener instanceof AuditEventListener)
                {
                    // uninstall it
                    needUninstall = true;
                    break;
                }
            }

            if (needUninstall)
            {
                log.debug("uninstalling '" + eventType + "' audit listeners");

                List<Object> clean = new ArrayList<Object>();
                for(Object listener: listeners)
                {
                    if (!(listener instanceof AuditEventListener))
                    {
                        clean.add(listener);
                    }
                }

                Object[] cleanArray =
                    (Object[])Array.newInstance(els.getListenerClassFor(eventType), clean.size());

                int i = 0;
                for(Object cleanListener: clean)
                {
                    cleanArray[i++] = cleanListener;
                }

                Method setter = Listeners.getEventListenersSetter(eventType);
                setter.invoke(els, ((Object)cleanArray));
            }
        }

        log.debug(this + " uninstalled audit listeners");
    }

    // Inner classes -------------------------------------------------------------------------------
}
