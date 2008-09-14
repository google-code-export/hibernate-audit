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
import org.hibernate.Transaction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.event.EventListeners;
import org.hibernate.event.EventSource;

import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.security.Principal;
import java.io.Serializable;

import com.googlecode.hibernate.audit.DelegateConnectionProvider;
import com.googlecode.hibernate.audit.delta.TransactionDeltaImpl;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDeltaImpl;
import com.googlecode.hibernate.audit.delta.Deltas;
import com.googlecode.hibernate.audit.delta.MemberVariableDelta;
import com.googlecode.hibernate.audit.delta_deprecated.DeltaDeprecated;
import com.googlecode.hibernate.audit.delta_deprecated.DeltaEngine;
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
    private Map<SessionFactoryImpl, SessionFactoryHolder> sessionFactoryHolders;
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
        sessionFactoryHolders = new HashMap<SessionFactoryImpl, SessionFactoryHolder>();
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
     *
     * @param lgip - the application-level LogicalGroupIdProvider that knows to provide logical
     *        group ids for entities managed by this session factory. If null, no logical group id
     *        will persisted in the database. This is alright if you don't need logical grouping
     *        of entities.
     */
    public synchronized void register(SessionFactoryImplementor asfi, LogicalGroupIdProvider lgip)
        throws Exception
    {
        if (!(asfi instanceof SessionFactoryImpl))
        {
            throw new IllegalArgumentException(
                "cannot enable audit unless given session factory is a SessionFactoryImpl " +
                "instance; instead we got " +
                (asfi == null ? null : asfi.getClass().getName()));
        }

        SessionFactoryImpl asf = (SessionFactoryImpl)asfi;
        
        checkStarted();

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

        boolean sameConnectionProvider = false;

        if (thatCp == thisCp)
        {
            sameConnectionProvider = true;
        }
        else if (thatCp instanceof DatasourceConnectionProvider)
        {
            DatasourceConnectionProvider thatDscp = (DatasourceConnectionProvider)thatCp;

            if (thisCp instanceof DatasourceConnectionProvider)
            {
                DatasourceConnectionProvider thisDscp = (DatasourceConnectionProvider)thisCp;

                if (thatDscp.getDataSource() != thisDscp.getDataSource())
                {
                    throw new IllegalArgumentException(
                        "internal connection provider and audited session " +
                        "factory's connection provider do not match");
                }

                sameConnectionProvider = true;
            }
        }

        if (!sameConnectionProvider)
        {
            // TODO we should see about this
            log.warn("The connection provider of the registering session factory (" + thatCp +
                     ") differs from the audit connection provider (" + thisCp + ")!");
        }

        installAuditListeners(asf);
        sessionFactoryHolders.put(asf, new SessionFactoryHolder(asf, lgip));
    }

    public synchronized boolean unregister(SessionFactory sf) throws Exception
    {
        checkStarted();

        if (!(sf instanceof SessionFactoryImpl))
        {
            // ignore
            return false;
        }

        SessionFactoryImpl sfi = (SessionFactoryImpl)sf;
        SessionFactoryHolder sfh = sessionFactoryHolders.remove(sfi);

        if (sfh == null)
        {
            // nothing to disable
            return false;
        }

        uninstallAuditListeners(sfi);
        sfh.sessionFactory = null;
        sfh.logicalGroupIdProvider = null;
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

        if (atLeastOneFound && !sessionFactoryHolders.keySet().contains(sfi))
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
        return new HashSet<SessionFactoryImpl>(sessionFactoryHolders.keySet());
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
        checkStarted();

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

    // Delta functions -----------------------------------------------------------------------------

    /**
     * @throws Exception could be caused by an abnormal condition while accessing the database.
     */
    public TransactionDelta getDelta(Long txId) throws Exception
    {
        Session is = null;
        Transaction internalTransaction = null;

        try
        {
            is = isf.openSession();
            internalTransaction = is.beginTransaction();

            AuditTransaction atx = (AuditTransaction)is.get(AuditTransaction.class, txId);

            if (atx == null)
            {
                log.debug("no audit transaction with id " + txId + " found");
                return null;
            }

            TransactionDeltaImpl td = new TransactionDeltaImpl(atx.getId(),
                                                               atx.getLogicalGroupId(),
                                                               atx.getTimestamp(),
                                                               atx.getUser());
            List<AuditEvent> es = atx.getEvents();

            for(AuditEvent e: es)
            {
                AuditType at = e.getTargetType();

                if (!at.isEntityType())
                {
                    // haven't encountered yet a case where I get here something else than an
                    // entity, but case for it anyway
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                Serializable id = e.getTargetId();
                AuditEntityType aet = (AuditEntityType)at;
                String entityName = aet.getClassInstance().getName(); // TODO this will fail when we use real entityNames
                EntityDeltaImpl ed = (EntityDeltaImpl)td.getEntityDelta(id, entityName);

                if (ed == null)
                {
                    ed = new EntityDeltaImpl(id, entityName);
                    td.addEntityDelta(ed);
                }

                List<AuditEventPair> pairs = e.getPairs();

                for(AuditEventPair p: pairs)
                {
                    AuditTypeField f = p.getField();
                    String name = f.getName();
                    AuditType t = f.getType();
                    Object value = p.getValue();

                    MemberVariableDelta mvd = null;

                    if (t.isPrimitiveType())
                    {
                        if (value == null)
                        {
                            throw new RuntimeException("NOT YET IMPLEMENTED");
                        }
                        
                        mvd = Deltas.createPrimitiveDelta(name, value);
                    }
                    else if (t.isEntityType())
                    {
                        AuditEntityType ret = (AuditEntityType)t;
                        String referredEntityName = ret.getEntityName();
                        Serializable refid = (Serializable)value;
                        mvd = Deltas.createEntityReferenceDelta(name, refid, referredEntityName);
                    }
                    else if (t.isCollectionType())
                    {
                        AuditCollectionType ct = (AuditCollectionType)t;
                        String memberEntityName = ct.getMemberEntityName();

                        // possibly we lose ordering information, TODO analyze this
                        Collection<Serializable> ids = new HashSet<Serializable>();
                        Collection c = (Collection)value;
                        for(Object o: c)
                        {
                            if (!ids.add((Serializable)o))
                            {
                                throw new IllegalStateException("duplicate id in collection: " + o);
                            }
                        }

                        mvd = Deltas.createCollectionDelta(name, memberEntityName, ids);
                    }

                    if (!ed.addMemberVariableDelta(mvd))
                    {
                        // an equal() member variable delta was added already
                        throw new IllegalStateException("duplicate member variable delta " + mvd);
                    }
                }
            }
            return td;
        }
        catch(Exception e)
        {
            if (internalTransaction != null)
            {
                try
                {
                    internalTransaction.rollback();
                }
                catch(Exception e2)
                {
                    log.error("failed to rollback Hibernate transaction", e2);
                }
            }

            if (is != null)
            {
                try
                {
                    is.close();
                }
                catch(Exception e2)
                {
                    log.error("failed to close internal Hibernate session", e2);
                }
            }

            throw e;
        }
        finally
        {
            if (internalTransaction != null && internalTransaction.isActive())
            {
                internalTransaction.commit();
            }
        }
    }

    /**
     * @param txId - the id of the transaction that introduced the delta.
     * @param lgId - the id of the logical group. If null, all delta information is returned.
     *
     * @return the delta or null, if no delta information was found for this particular combination
     *         of transaction/logical group.
     */
    public DeltaDeprecated getDeltaDeprecated(Long txId, Serializable lgId) throws Exception
    {
        return DeltaEngine.getDelta(txId, lgId, isf);
    }

    /**
     * @param base - the intial state of the object to apply transactional delta to.
     */
    public void delta(Object base, Serializable id, Long txId) throws Exception
    {
        delta(base, null, id, txId);
    }

    /**
     * @param base - the intial state of the object to apply transactional delta to.
     * @param entityName - the entityName corresponding to the base instance. If null, base's class
     *        will be used.
     */
    public void delta(Object base, String entityName, Serializable id, Long txId) throws Exception
    {
        checkStarted();
        
        Class c = null;

        if (entityName == null)
        {
            c = base.getClass();
        }

        SessionFactoryImpl sfi = null;

        // pick up a registered session factory to provide metadata
        for(SessionFactoryImpl i: getAuditedSessionFactories())
        {
            ClassMetadata cm =
                entityName != null ? i.getClassMetadata(entityName) : i.getClassMetadata(c);

            if (cm != null)
            {
                if (sfi != null)
                {
                    throw new Exception(
                        "NOT YET IMPLEMENTED: more than one SessionFactory maintains " +
                        (entityName != null ? entityName : c.getName()) + " metadata");
                }

                sfi = i;
            }
        }

        if (sfi == null)
        {
            throw new IllegalStateException(
                "no registered session factory maintains metadata on " +
                (entityName != null ? entityName : c.getName()));
        }

        DeltaEngine.delta(base, entityName, id, txId, sfi, isf);
    }

    /**
     * @return null if it cannot figure it out.
     */
    public Serializable getLogicalGroupId(EventSource es, Serializable id, Object entity)
    {
        SessionFactoryImpl sf = (SessionFactoryImpl)es.getSessionFactory();

        SessionFactoryHolder sfh = sessionFactoryHolders.get(sf);

        if (sfh == null || sfh.logicalGroupIdProvider == null)
        {
            // no provider, return null
            return null;
        }

        return sfh.logicalGroupIdProvider.getLogicalGroupId(es, id, entity);
    }

    @Override
    public String toString()
    {
        String s = connectionProviderToString();
        return "Manager[" + Integer.toHexString(System.identityHashCode(this)) + "]" +
               (s == null ? "" : "[" + s + "]");
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
            Constructor ctor = c.getConstructor(Manager.class);
            AuditEventListener ael = (AuditEventListener)ctor.newInstance(this);

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
            return null;
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

    private void checkStarted() throws IllegalStateException
    {
        if (!isStarted())
        {
            throw new IllegalStateException("HibernateAudit runtime not started. " +
                                            "Check the log for possible startup failure causes.");
        }
    }

    // Inner classes -------------------------------------------------------------------------------

    private class SessionFactoryHolder
    {
        SessionFactoryImpl sessionFactory;
        LogicalGroupIdProvider logicalGroupIdProvider;

        SessionFactoryHolder(SessionFactoryImpl sessionFactory,
                             LogicalGroupIdProvider logicalGroupIdProvider)
        {
            this.sessionFactory = sessionFactory;
            this.logicalGroupIdProvider = logicalGroupIdProvider;
        }
    }
}
