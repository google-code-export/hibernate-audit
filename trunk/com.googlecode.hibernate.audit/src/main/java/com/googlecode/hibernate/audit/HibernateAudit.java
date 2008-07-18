package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.event.EventListeners;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.listener.AuditEventListener;
import com.googlecode.hibernate.audit.listener.Listeners;
import com.googlecode.hibernate.audit.util.QueryParameters;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.security.SecurityInformationProvider;
import com.googlecode.hibernate.audit.security.SecurityInformationProviderFactory;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.security.Principal;

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

    // the (mostly one) audit transaction associated with a current thread
    private static final ThreadLocal<AuditTransaction> auditTransaction;

    private static HibernateAudit singleton;

    static
    {
       auditTransaction = new ThreadLocal<AuditTransaction>();
    }

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
    public static synchronized boolean disable() throws Exception
    {
        if (singleton == null)
        {
            return false;
        }

        singleton.stop();
        singleton = null;
        return true;
    }

    /**
     * A general purpose query facility. Understands HQL.
     *
     * @exception IllegalStateException if audit is not enabled.
     */
    public synchronized static List query(String query, Object... args) throws Exception
    {
        if (singleton == null)
        {
            throw new IllegalStateException("Hibernate Audit runtime disabled");
        }

        return singleton.doQuery(query, args);
    }

    /**
     * @return the AuditTransaction instance associated with the thread, if any. May return null.
     */
    public static AuditTransaction getCurrentAuditTransaction()
    {
        return auditTransaction.get();
    }

    public static void setCurrentAuditTransaction(AuditTransaction at)
    {
        log.debug(at == null ?
                  "dissasociating audit transaction from the current thread":
                  "associating " + at + " with the current thread");
        auditTransaction.set(at);
    }

    /**
     * @return the principal associated with the current security context, if any, or null
     *         otherwise.
     */
    public static Principal getPrincipal()
    {
        if (singleton == null)
        {
            return null;
        }

        SecurityInformationProvider sip = singleton.getSecurityInformationProvider();

        if (sip == null)
        {
            return null;
        }

        return sip.getPrincipal();
    }

    /**
     * TODO I don't necessarily need an active HibernateAudit runtime for this, I can create
     * a session factory from scratch and use it, but for the time being, I am using an active
     * runtime, just to prove the idea is valid.
     */
    public static void forwardDelta(Object initialState, Long transactionId) throws Exception
    {
        if (singleton == null)
        {
            throw new IllegalStateException("Hibernate Audit runtime disabled");
        }

        DeltaEngine.applyDelta(singleton.auditedSessionFactory, initialState, transactionId);
    }

    /**
     * An AuditType instance obtained as a result of a query from the database may need
     * "postprocessing", in that some instances may need semantic enhancing. For example, we may
     * realize that AuditType instances are actually AuditEntityType instances, so we do the switch
     * here.
     */
    static AuditType enhance(SessionFactory auditedSf, AuditType at)
    {
        Class c = at.getClassInstance();
        ClassMetadata cm = auditedSf.getClassMetadata(c);
        if (cm != null)
        {
            // it's an entity
            Class idClass = cm.getIdentifierType().getReturnedClass();
            at = new AuditEntityType(idClass, at);
        }
        return at;
    }

    // Attributes ----------------------------------------------------------------------------------

    private SessionFactoryImpl auditedSessionFactory;
    private String secondaryConfigurationResource;
    private SecurityInformationProvider securityInformationProvider;

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

    /**
     * May return null if no security information provider has been installed.
     */
    SecurityInformationProvider getSecurityInformationProvider()
    {
        return securityInformationProvider;
    }

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
//        installMappings(auditedSessionFactory);

        try
        {
            securityInformationProvider =
                SecurityInformationProviderFactory.getSecurityInformationProvider();
        }
        catch(Exception e)
        {
            // something went wrong and we cannot get our provider, shoot a short warning and give
            // more info in the debug log
            log.warn("Cannot instantiate a security information provider: " + e.getMessage());
            log.debug("Cannot instantiate a security information provider", e);
        }

        log.debug(this + " started");
    }

    private void stop() throws Exception
    {
        log.debug(this + " stopping ...");

        uninstallAuditListeners(auditedSessionFactory);
//        uninstallMappings(auditedSessionFactory);

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

        log.debug(this + " installed audit listeners: " + eventTypes);
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

//    private void installMappings(SessionFactoryImpl sf) throws Exception
//    {
//    }

//    private void uninstallMappings(SessionFactoryImpl sf) throws Exception
//    {
//        throw new Exception("NOT YET IMPLEMENTED");
//    }

    private List doQuery(String query, Object... args) throws Exception
    {
        if (secondaryConfigurationResource != null)
        {
            throw new Exception("NOT YET IMPLEMENTED");
        }

        Session s = null;

        try
        {
            s = auditedSessionFactory.openSession();
            s.beginTransaction();

            Query q = s.createQuery(query);
            QueryParameters.fill(q, args);
            return postProcess(q.list());
        }
        finally
        {
            if (s != null)
            {
                try
                {
                    s.getTransaction().commit();
                }
                catch(Exception e)
                {
                    log.error("failed to commit query transaction", e);
                }

                s.close();
            }
        }
    }

    /**
     * The list with results from database may need "postprocessing", in that some instances may
     * need semantic enhancing. For example, we may realize that AuditType instances are actually
     * AuditEntityType instances, so we do the switch here.
     */
    private List postProcess(List queryResult)
    {
        List<Object> result = new ArrayList<Object>();

        for(Object o: queryResult)
        {
            if (o instanceof AuditType)
            {
                o = enhance(auditedSessionFactory, (AuditType)o);
            }

            result.add(o);
        }

        return result;
    }

    // Inner classes -------------------------------------------------------------------------------
}
