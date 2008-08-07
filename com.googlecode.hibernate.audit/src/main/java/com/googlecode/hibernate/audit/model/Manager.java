package com.googlecode.hibernate.audit.model;

import org.apache.log4j.Logger;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.DatasourceConnectionProvider;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.event.EventListeners;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.security.Principal;
import java.io.Serializable;

import com.googlecode.hibernate.audit.DelegateConnectionProvider;
import com.googlecode.hibernate.audit.DeltaEngine;
import com.googlecode.hibernate.audit.util.QueryParameters;
import com.googlecode.hibernate.audit.listener.Listeners;
import com.googlecode.hibernate.audit.listener.AuditEventListener;
import com.googlecode.hibernate.audit.security.SecurityInformationProviderFactory;
import com.googlecode.hibernate.audit.security.SecurityInformationProvider;

import javax.sql.DataSource;

/**
 * The main run-time repository of package-protected audit logic. This class is allowed to access
 * package-protected methods of model classes, and it should be the only point of access to
 * manipulate model instances.
 *
 * There is only one audit manager instance per data source.
 *
 * TODO analyize multithreading behavior
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Manager
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(Manager.class);

    // Static --------------------------------------------------------------------------------------

    // the (mostly one) audit transaction associated with a current thread
    private static final ThreadLocal<AuditTransaction> auditTransaction;

    static
    {
       auditTransaction = new ThreadLocal<AuditTransaction>();
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

    // Attributes ----------------------------------------------------------------------------------

    private Settings settings;
    private Set<SessionFactoryImpl> auditedSessionFactories;
    private SecurityInformationProvider securityInformationProvider;

    // the session factory to create sessions used to write the audit log
    // a non-null session factory signifies that this manager instance is started
    private SessionFactoryImpl isf;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param settings - settings "borrowed" from the *first* audited session factory HBA is enabled
     *        on. HBA will use the datasouce/database url and associated credentials to log audit
     *        data.
     */
    public Manager(Settings settings) throws Exception
    {
        auditedSessionFactories = new HashSet<SessionFactoryImpl>();
        this.settings = settings;
        log.debug(this + " created");
    }

    // Public --------------------------------------------------------------------------------------

    public synchronized void start() throws Exception
    {
        log.debug(this + " starting ...");

        SettingsFactory settf = new AuditSettingsFactory(settings);
        AnnotationConfiguration config = new AnnotationConfiguration(settf);
        installMappings(config);

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

        isf = (SessionFactoryImpl)config.buildSessionFactory();

        log.debug(this + " started");
    }

    public synchronized boolean isStarted()
    {
        return isf != null;
    }

    public synchronized void stop() throws Exception
    {
        log.debug(this + " stopping ...");

        Set<SessionFactoryImpl> copy = getAuditedSessionFactories();
        for(SessionFactoryImpl sf: copy)
        {
            unregister(sf);
        }

        settings = null;
        securityInformationProvider = null;
        isf.close();
        isf = null;

        log.debug(this + " stopped");
    }

    /**
     * Register the given session factory instance with this audit manager. During the registration
     * process, the audit manager registers listeners on the session factory. The listeners will
     * capture and persist state changes.
     */
    public synchronized void register(SessionFactoryImpl asf) throws Exception
    {
        if (isRegistered(asf))
        {
            log.debug(asf + " already registered on " + this);
            return;
        }

        ConnectionProvider thatCp = asf.getConnectionProvider();
        ConnectionProvider thisCp = ((DelegateConnectionProvider)isf.
            getConnectionProvider()).getDelegate();

        // insure transactional consistency by making sure that we ultimately delegate to the same
        // connection provider

        if (!(thatCp instanceof DatasourceConnectionProvider))
        {
            // we haven't addressed this case yet, TODO
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }
        else
        {
            DatasourceConnectionProvider thatDscp = (DatasourceConnectionProvider)thatCp;
            DatasourceConnectionProvider thisDscp = (DatasourceConnectionProvider)thisCp;

            if (thatDscp.getDataSource() != thisDscp.getDataSource())
            {
                throw new IllegalArgumentException(
                    "internal connection provider and audited session " +
                    "factory's connection provider do not match");
            }
        }

        installAuditListeners(asf);
        auditedSessionFactories.add(asf);
    }

    public synchronized boolean unregister(SessionFactory sf) throws Exception
    {
        if (!(sf instanceof SessionFactoryImpl))
        {
            // ignore
            return false;
        }

        SessionFactoryImpl sfi = (SessionFactoryImpl)sf;
        if (!auditedSessionFactories.remove(sfi))
        {
            // nothing to disable
            return false;
        }

        uninstallAuditListeners(sfi);
        return true;
    }

    /**
     * @exception IllegalStateException on corrupted states (partial set of listeners, etc).
     */
    public synchronized boolean isRegistered(SessionFactoryImpl sfi)
    {
        // we look at whether the audit listeners are actually registered, we disregard the internal
        // session factory list

        boolean atLeastOneFound = false;

        try
        {
            Set<String> aets = Listeners.getAuditedEventTypes();

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
                        "partial set of audit listeners found, possibly corrupted state");
                }
            }
        }
        catch(IllegalStateException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new IllegalStateException(
                "failed to determine whether the session factory is registered", e);
        }

        if (atLeastOneFound && !auditedSessionFactories.contains(sfi))
        {
            throw new IllegalStateException(
                "complete set of audit listeners found, but session factory is not known, " +
                "possibly corrupted state");
        }

        return atLeastOneFound;
    }

    /**
     * @return a copy of the internal set.
     */
    public synchronized Set<SessionFactoryImpl> getAuditedSessionFactories()
    {
        return new HashSet<SessionFactoryImpl>(auditedSessionFactories);
    }

    /**
     * @return the principal associated with the current security context, if any, or null
     *         otherwise.
     */
    public synchronized Principal getPrincipal()
    {
        if (securityInformationProvider == null)
        {
            return null;
        }

        return securityInformationProvider.getPrincipal();
    }

    /**
     * @return the internal SessionFactory instance. May return null if manager is not started.
     */
    public SessionFactoryImpl getSessionFactory()
    {
        return isf;
    }

    public List query(String query, Object... args) throws Exception
    {
        SessionFactoryImpl localIsf = null;
        synchronized(this)
        {
            localIsf = isf;
            // TODO what happens if isf is closed while in the middle of a query?
        }

        Session s = null;

        try
        {
            s = localIsf.openSession();
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
     * @param base - the intial state of the object to apply transactional delta to.
     */
    public void delta(Object base, Serializable id, Long txId) throws Exception
    {
        // pick up a registered session factory to provide metadata
        Class c = base.getClass();
        SessionFactoryImpl sfi = null;
        for(SessionFactoryImpl i: getAuditedSessionFactories())
        {
            if (i.getClassMetadata(c) != null)
            {
                if (sfi != null)
                {
                    throw new Exception(
                        "NOT YET IMPLEMENTED: more than one SessionFactory maintains " +
                        c.getName() + " metadata");
                }

                sfi = i;
            }
        }

        if (sfi == null)
        {
            throw new IllegalStateException(
                "no registered session factory maintains metadata on " + c.getName());
        }

        DeltaEngine.delta(base, id, txId, sfi);
    }

    @Override
    public String toString()
    {
        return "Manager[" + Integer.toHexString(System.identityHashCode(this)) + "][" +
               connectionProviderToString() + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

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
                (Object[]) Array.newInstance(hibernateListenerInteface, listeners.length + 1);
            System.arraycopy(listeners, 0, newListeners, 0, listeners.length);

            Class c = Listeners.getAuditEventListenerClass(auditEventType);
            AuditEventListener ael = (AuditEventListener)c.newInstance();

            newListeners[newListeners.length - 1] = ael;
            setter.invoke(els, ((Object)newListeners));
        }

        log.debug(this + " installed audit listeners " + eventTypes + " on " + sf);
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
                log.debug("uninstalling '" + eventType + "' audit listeners from " + sf);

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

        log.debug(this + " uninstalled audit listeners from " + sf);
    }

    private void installMappings(AnnotationConfiguration config) throws Exception
    {
        Set<Class> entities = Entities.getAuditEntities();
        for(Class e: entities)
        {
            config.addAnnotatedClass(e);
            log.debug(this + " installed annotated class " + e);
        }
    }

    private String connectionProviderToString()
    {
        if (isf == null)
        {
            return "STOPPED";
        }

        ConnectionProvider cp = isf.getConnectionProvider();

        if (cp instanceof DelegateConnectionProvider)
        {
            cp = ((DelegateConnectionProvider)cp).getDelegate();
        }

        if (cp instanceof DatasourceConnectionProvider)
        {
            DataSource ds = ((DatasourceConnectionProvider)cp).getDataSource();
            return ds.toString();
        }

        return cp.toString();
    }

    // Inner classes -------------------------------------------------------------------------------
}
