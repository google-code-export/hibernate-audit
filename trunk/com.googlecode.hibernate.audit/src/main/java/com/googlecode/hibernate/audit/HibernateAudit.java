package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.event.EventListeners;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.listener.AuditEventListener;
import com.googlecode.hibernate.audit.listener.Listeners;
import com.googlecode.hibernate.audit.util.QueryParameters;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditTypeField;
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
                "Cannot enable audit unless session factory is a SessionFactoryImpl instance");
        }

        SessionFactoryImpl sfi = (SessionFactoryImpl)asf;

        if (singleton == null)
        {
            singleton = new HibernateAudit(sfi.getSettings());
            singleton.start();
        }

        singleton.enableOn(sfi);
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
    private Settings sourceSettings;
    private String secondaryConfigurationResource;
    private SecurityInformationProvider securityInformationProvider;


    // the session factory to create sessions used to write the audit log
    private SessionFactoryImpl internalSessionFactory;

    // Constructors --------------------------------------------------------------------------------

    private HibernateAudit()
    {
        auditedSessionFactories = new HashSet<SessionFactoryImpl>();
    }

    /**
     * @param settings - settings "borrowed" from the *first* audited session factory HBA is enabled
     *        on. HBA will use the datasouce/database url and associated credentials to log audit
     *        data.
     */
    private HibernateAudit(Settings settings) throws Exception
    {
        this();
        log.debug("creating HibernateAudit runtime based on settings " + settings);
        this.sourceSettings = settings;
    }

    /**
     * @param resource - the resource which contains the configuration for the secondary persistence
     *        unit (used to persist audit data). A null resource means that there is no secondary
     *        persistence unit, the audited persistence unit will be used to persist audit data as
     *        well.
     */
    private HibernateAudit(String resource) throws Exception
    {
        this();
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

        SettingsFactory sf = new AuditSettingsFactory(sourceSettings);
        AnnotationConfiguration config = new AnnotationConfiguration(sf);
        installMappings(config);
        internalSessionFactory = (SessionFactoryImpl)config.buildSessionFactory();

        // if using a different persistence unit, initialize it first

        if (secondaryConfigurationResource != null)
        {
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

        internalSessionFactory.close();

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

        // TODO:  make sure the audited session factory and the internal session factory use the
        // same connection provider, otherwise the transactional integrity is compromised.

        ConnectionProvider thatConnectioProvider = asf.getConnectionProvider();
        ConnectionProvider thisConnectionProvider =
            ((DelegateConnectionProvider)internalSessionFactory.getConnectionProvider()).
                getDelegate();

        if (thatConnectioProvider != thisConnectionProvider)
        {
            // TODO test this
            throw new IllegalArgumentException("Internal connection provider and audited session " +
                                               "factory connection provider do not match");
        }

        installAuditListeners(asf);
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

    private void installMappings(AnnotationConfiguration config) throws Exception
    {
        // TODO currently adding classes individually, it can be automated
        config.addAnnotatedClass(AuditCollectionType.class);
        config.addAnnotatedClass(AuditEntityType.class);
        config.addAnnotatedClass(AuditEvent.class);
        config.addAnnotatedClass(AuditEventCollectionPair.class);
        config.addAnnotatedClass(AuditEventPair.class);
        config.addAnnotatedClass(AuditTransaction.class);
        config.addAnnotatedClass(AuditType.class);
        config.addAnnotatedClass(AuditTypeField.class);
    }

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

    /**
     * May return null if no security information provider has been installed.
     */
    private SecurityInformationProvider getSecurityInformationProvider()
    {
        return securityInformationProvider;
    }

    // Inner classes -------------------------------------------------------------------------------
}
