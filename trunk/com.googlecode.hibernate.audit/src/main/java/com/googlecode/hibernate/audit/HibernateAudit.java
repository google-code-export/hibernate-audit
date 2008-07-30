package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.event.EventListeners;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.listener.AuditEventListener;
import com.googlecode.hibernate.audit.listener.Listeners;
import com.googlecode.hibernate.audit.util.QueryParameters;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.security.SecurityInformationProvider;
import com.googlecode.hibernate.audit.security.SecurityInformationProviderFactory;

import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.security.Principal;
import java.io.Serializable;

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

    public static synchronized boolean isStarted()
    {
        return singleton != null;
    }

    public static synchronized boolean isEnabled(SessionFactory asf)
    {
        if (!(asf instanceof SessionFactoryImpl))
        {
            throw new IllegalArgumentException(
                "session factory should be a SessionFactoryImpl instance");
        }

        return singleton != null && singleton.isEnabledOn((SessionFactoryImpl)asf);
    }

    /**
     * Turns audit on.
     *
     * @param asf - the session factory of the audited persistence unit.
     */
    public static synchronized void enable(SessionFactory asf) throws Exception
    {
        if (!(asf instanceof SessionFactoryImpl))
        {
            throw new IllegalArgumentException(
                "session factory should be a SessionFactoryImpl instance");
        }

        if (singleton == null)
        {
            singleton = new HibernateAudit(null);
            singleton.start();
        }

        singleton.enableOn((SessionFactoryImpl)asf);
    }

    /**
     * Turns audit off on this specific factory. If this is the last active factory associated
     * with the audit engine, the engine itself is stopped.
     *
     * @return true if calling this method ended up in audit being disabled, or false if there was
     *         no active audit runtime to disableAll.
     */
    public static synchronized boolean disable(SessionFactory sf) throws Exception
    {
        if (singleton == null)
        {
            return false;
        }

        if (!singleton.disableOn(sf))
        {
            return false;
        }

        if (singleton.auditedSessionFactories.isEmpty())
        {
            singleton.stop();
            singleton = null;
        }

        return true;
    }

    /**
     * Turns audit off, by disabling audit on all audited session factories, and stops the audit
     * engine.
     *
     * @return true if calling this method ended up in audit being disabled, or false if there was
     *         no active audit runtime to disableAll.
     */
    public static synchronized boolean disableAll() throws Exception
    {
        if (singleton == null)
        {
            return false;
        }

        boolean result = false;

        for(SessionFactoryImpl s: singleton.auditedSessionFactories)
        {
            result |= disable(s);
        }

        return result;
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

    public static void delta(Object preTransactionState, Long transactionId) throws Exception
    {
        delta(preTransactionState, null, transactionId);
    }

    /**
     * TODO I don't necessarily need an active HibernateAudit runtime for this, I can create
     * a session factory from scratch and use it, but for the time being, I am using an active
     * runtime, just to prove the idea is valid.
     */
    public static void delta(Object preTransactionState, Serializable id, Long transactionId)
        throws Exception
    {
        if (singleton == null || singleton.auditedSessionFactories.isEmpty())
        {
            throw new IllegalStateException("Hibernate Audit runtime disabled");
        }

        // Just pick one session factory for that, this is undeterministic and bad, and also see
        // the above TODO
        SessionFactoryImpl sf = singleton.auditedSessionFactories.iterator().next();
        DeltaEngine.delta(preTransactionState, id, transactionId, sf);
    }

    // Attributes ----------------------------------------------------------------------------------

    private Set<SessionFactoryImpl> auditedSessionFactories;
    private String secondaryConfigurationResource;
    private SecurityInformationProvider securityInformationProvider;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param resource - the resource which contains the configuration for the secondary persistence
     *        unit (used to persist audit data). A null resource means that there is no secondary
     *        persistence unit, the audited persistence unit will be used to persist audit data as
     *        well.
     */
    private HibernateAudit(String resource) throws Exception
    {
        auditedSessionFactories = new HashSet<SessionFactoryImpl>();
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

        //TODO if using a different persistence unit, clean-up that

        for(SessionFactoryImpl sf: auditedSessionFactories)
        {
            disableOn(sf);
        }

        auditedSessionFactories.clear();

        log.debug(this + " stopped");
    }

    /**
     * TODO: change name, it's named this way only because we have a similar static signature.
     * @exception IllegalStateException on corrupted states (partial set of listeners, etc).
     */
    private boolean isEnabledOn(SessionFactoryImpl sfi)
    {
        // we look at whether the audit listeners are actually registered, we disregard the internal
        // session factory list

        try
        {
            Set<String> aets = Listeners.getAuditedEventTypes();

            boolean atLeastOneFound = false;

            outer: for(String aet: aets)
            {
                Method m = Listeners.getEventListenersGetter(aet);
                Object[] listeners = (Object[])m.invoke(sfi.getEventListeners());
                Class c = Listeners.getAuditEventListenerClass(aet);

                for(Object o: listeners)
                {
                    if (c.isInstance(o))
                    {
                        // found at least one
                        atLeastOneFound = true;
                        continue outer;
                    }
                }

                if (atLeastOneFound)
                {
                    throw new IllegalStateException(
                        "Partial set of audit listeners found, possibly corrupted state");
                }
            }

            return atLeastOneFound;
        }
        catch(Exception e)
        {
            throw new IllegalStateException("failed to determine whether HBA is enabled or not", e);
        }
    }

    /**
     * Turns audit on.
     *
     * @param asf - the session factory of the audited persistence unit.
     */
    private synchronized void enableOn(SessionFactoryImpl asf) throws Exception
    {
        if (isEnabledOn(asf))
        {
            // nothing to do here, noop
            log.debug("audit already enabled on " + asf);
            return;
        }

        installAuditListeners(asf);
//        installMappings(asf);
        auditedSessionFactories.add(asf);
    }

    /**
     * Turns audit off.
     *
     * @param asf - the session factory of the audited persistence unit.
     */
    private synchronized boolean disableOn(SessionFactory asf) throws Exception
    {
        if (!(asf instanceof SessionFactoryImpl))
        {
            // ignore
            return false;
        }

        SessionFactoryImpl sfi = (SessionFactoryImpl)asf;
        if (!auditedSessionFactories.remove(sfi))
        {
            // nothing to disable
            return false;
        }

        uninstallAuditListeners(sfi);
//        uninstallMappings(asf);
        
        return true;
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
            // Just pick one session factory for that, this is undeterministic and bad, and also see
            // the above TODO (delta())
            SessionFactoryImpl sf = auditedSessionFactories.iterator().next();

            s = sf.openSession();
            s.beginTransaction();

            Query q = s.createQuery(query);
            QueryParameters.fill(q, args);
            return q.list();
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

    // Inner classes -------------------------------------------------------------------------------
}
