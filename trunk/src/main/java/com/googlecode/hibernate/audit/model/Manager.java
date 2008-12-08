package com.googlecode.hibernate.audit.model;

import org.apache.log4j.Logger;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.cfg.Settings;
import org.hibernate.cfg.SettingsFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.Configuration;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.connection.DatasourceConnectionProvider;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.hibernate.Query;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionFactoryImplementor;
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
import com.googlecode.hibernate.audit.LogicalGroupProvider;
import com.googlecode.hibernate.audit.AuditSelector;
import com.googlecode.hibernate.audit.LogicalGroup;
import com.googlecode.hibernate.audit.collision.WriteCollisionDetector;
import com.googlecode.hibernate.audit.delta.TransactionDeltaImpl;
import com.googlecode.hibernate.audit.delta.TransactionDelta;
import com.googlecode.hibernate.audit.delta.EntityDeltaImpl;
import com.googlecode.hibernate.audit.delta.Deltas;
import com.googlecode.hibernate.audit.delta.MemberVariableDelta;
import com.googlecode.hibernate.audit.delta.ChangeType;
import com.googlecode.hibernate.audit.delta.CollectionDelta;
import com.googlecode.hibernate.audit.delta.EntityReferenceDelta;
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
    private static final boolean traceEnabled = log.isTraceEnabled();

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
        AuditTransaction crt = auditTransaction.get();

        if (at == null)
        {
            // we want to clean thread local

            if (crt == null)
            {
                // nothing to clean
                return;
            }

            if (traceEnabled) { log.trace("disassociating " + crt + " from current thread " + Thread.currentThread()); }
        }
        else if (traceEnabled)
        {
            if (crt == null)
            {
                log.trace("associating " + at + " with current thread " + Thread.currentThread());
            }
            else
            {
                log.trace("replacing " + crt + " with " + at + " on current thread " +
                          Thread.currentThread());
            }
        }

        auditTransaction.set(at);
    }

    // Attributes ----------------------------------------------------------------------------------

    private Settings settings;
    private Map<SessionFactoryImpl, SessionFactoryHolder> sessionFactoryHolders;
    private SecurityInformationProvider securityInformationProvider;

    // internal (audit) configuration
    private AnnotationConfiguration ic;

    // the session factory to create sessions used to write the audit log
    // a non-null session factory signifies that this manager instance is started
    private SessionFactoryImpl isf;

    private TypeCache typeCache;

    private WriteCollisionDetector writeCollisionDetector;

    private boolean suppressed;

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
        this.writeCollisionDetector = new WriteCollisionDetector();

        log.debug(this + " created");
    }

    // Public --------------------------------------------------------------------------------------

    public synchronized void start() throws Exception
    {
        log.debug(this + " starting ...");

        SettingsFactory settf = new AuditSettingsFactory(settings);
        ic = new AnnotationConfiguration(settf);
        installMappings(ic);

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

        isf = (SessionFactoryImpl)ic.buildSessionFactory();

        typeCache = new TypeCache(isf);

        // suppresses throwing exceptions and rolling back transactions in listeners
        // VERY DANGEROUS! Do not use in production!
        suppressed = Boolean.getBoolean("hba.suppressed");

        if (suppressed)
        {
            log.warn("Exception propagation and automatic transaction rollback on audit failure " + 
                     " is suppressed! DO NOT USE THIS OPTION IN PRODUCTION!");
        }

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
        typeCache.clear();
        typeCache = null;
        isf.close();
        isf = null;
        ic = null;

        log.debug(this + " stopped");
    }

    /**
     * Register the given session factory instance with this audit manager. During the registration
     * process, the audit manager registers listeners on the session factory. The listeners will
     * capture and persist state changes.
     *
     * @param lgp - the application-level LogicalGroupProvider that knows to provide logical group
     *        instances ids for entities managed by this session factory. If null, no logical group
     *        will persisted in the database. This is alright if you don't need logical grouping
     *        of entities.
     *
     * @param as - may be null, in which case the framework decides whether to log or not based on
     *        annotations.
     */
    public synchronized void register(SessionFactoryImplementor asfi,
                                      LogicalGroupProvider lgp,
                                      AuditSelector as) throws Exception
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

        LogicalGroupCache lgc = new LogicalGroupCache(isf, typeCache, asf);
        sessionFactoryHolders.put(asf, new SessionFactoryHolder(asf, lgp, lgc, as));
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
        sfh.logicalGroupProvider = null;
        sfh.logicalGroupCache.clear();
        sfh.logicalGroupCache = null;

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

    /**
     * @return internal configuration. May return null if the manager instance is stopped.
     */
    public Configuration getConfiguration()
    {
        return ic;
    }

    public TypeCache getTypeCache()
    {
        return typeCache;
    }

    /**
     * May return null, if audited session factory is unknown.
     */
    public LogicalGroupCache getLogicalGroupCache(SessionFactoryImpl asf)
    {
        SessionFactoryHolder sfh = sessionFactoryHolders.get(asf);

        if (sfh == null)
        {
            return null;
        }

        return sfh.logicalGroupCache;
    }

    /**
     * May return a disabled WriteCollisionDetector, but never null.
     */
    public WriteCollisionDetector getWriteCollisionDetector()
    {
        return writeCollisionDetector;
    }

    public boolean isSuppressed()
    {
        return suppressed;
    }

    /**
     * @param leaveSessionOpen - don't commit the current transaction and don't close the current
     *        'query session'. This is useful for cases we want to walk lazily instantiated
     *        relationships.
     *
     *        WARNING, don't forget to commit transaction and close the session after you're done
     *                 with it!
     *
     * @return a QueryResult that is a simple wrapper around a List (the result) and the query
     *         session, in case query(...) was invoked with leaveSessionOpen = true.
     */
    public QueryResult query(String query, boolean leaveSessionOpen, Object ... args)
        throws Exception
    {
        checkStarted();

        SessionFactoryImpl localIsf = null;

        synchronized(this)
        {
            localIsf = isf;
            // TODO what happens if isf is closed while in the middle of a query?
        }

        QueryResult queryResult = null;
        Session s = null;

        try
        {
            s = localIsf.openSession();
            s.beginTransaction();

            Query q = s.createQuery(query);
            QueryParameters.fill(q, args);
            List result = q.list();
            queryResult = new QueryResult(result);
            return queryResult;
        }
        finally
        {
            if (leaveSessionOpen && queryResult != null)
            {
                queryResult.setSession(s);
            }
            else if (s != null)
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
                                                               atx.getTimestamp(),
                                                               atx.getUser());
            List<AuditEvent> es = atx.getEvents();

            for(AuditEvent ae: es)
            {
                AuditType at = ae.getTargetType();

                if (!at.isEntityType())
                {
                    // haven't encountered yet a case where I get here something else than an
                    // entity, but case for it anyway
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }

                Serializable id = ae.getTargetId();
                AuditEntityType aet = (AuditEntityType)at;
                String entityName = aet.getClassInstance().getName(); // TODO this will fail when we use real entityNames https://jira.novaordis.org/browse/HBA-80
                EntityDeltaImpl ed = (EntityDeltaImpl)td.getEntityDelta(id, entityName);
                ChangeType ct = ae.getType();
                AuditLogicalGroup alg = ae.getLogicalGroup();

                if (ed == null)
                {
                    ed = new EntityDeltaImpl(id, entityName, ct, alg);
                    td.addEntityDelta(ed);
                }
                else if (ChangeType.INSERT.equals(ct))
                {
                    // 'INSERT' always overwrites an 'UPDATE' as event type
                    if (ChangeType.UPDATE.equals(ed.getChangeType()))
                    {
                        ed.setChangeType(ChangeType.INSERT);
                    }
                }

                List<AuditEventPair> pairs = ae.getPairs();

                for(AuditEventPair p: pairs)
                {
                    AuditTypeField f = p.getField();
                    String name = f.getName();
                    AuditType t = f.getType();
                    Object value = p.getValue();

                    MemberVariableDelta mvd = null;

                    if (t.isPrimitiveType())
                    {
                        mvd = Deltas.createPrimitiveDelta(name, value);
                    }
                    else if (t.isEntityType())
                    {
                        if (value == null)
                        {
                            // entity reference that was just nullified
                            continue;
                        }

                        AuditEntityType ret = (AuditEntityType)t;
                        Serializable refid = (Serializable)value;
                        mvd = Deltas.createEntityReferenceDelta(name, refid,
                                                                ret.getEntityName(),
                                                                ret.getClassInstance());
                    }
                    else if (t.isCollectionType())
                    {
                        AuditCollectionType act = (AuditCollectionType)t;
                        String memberEntityName = act.getMemberEntityName();

                        // possibly we lose ordering information, TODO analyze this
                        Collection<Serializable> ids = new HashSet<Serializable>();
                        Collection c = (Collection)value;

                        if (c.isEmpty() && ed.isInsert())
                        {
                            // ingore empty collections on INSERT
                            continue;
                        }

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
                        // sometimes hibernate sends generated duplicate collection events (INSERT
                        // followed by UPDATE). As long the deltas are identical, not a big deal

                        // TODO this is fishy, why would we ever have to deal with this? Research

                        if (t.isEntityType())
                        {
                            EntityReferenceDelta crt = (EntityReferenceDelta)ed.getScalarDelta(name);
                            EntityReferenceDelta challenger = (EntityReferenceDelta)mvd;

                            if (!crt.getEntityName().equals(challenger.getEntityName()) ||
                                !crt.getId().equals(challenger.getId()))
                            {
                                throw new IllegalStateException(
                                    "inconsistent entity reference delta update, non-matching " +
                                    "content (" + crt + ", " + challenger);
                            }
                        }
                        else if (t.isCollectionType())
                        {
                            // we're collection, check if the deltas are identical
                            CollectionDelta existingCd = ed.getCollectionDelta(name);

                            String existingMemberEntityName = existingCd.getMemberEntityName();
                            String newMemberEntityName = ((CollectionDelta)mvd).getMemberEntityName();

                            if (!existingMemberEntityName.equals(newMemberEntityName))
                            {
                                throw new IllegalStateException(
                                    "inconsistent collection delta update, non-matching " +
                                    "member entity name (" + existingMemberEntityName + ", " +
                                    newMemberEntityName);
                            }

                            Collection<Serializable> existingIds = existingCd.getIds();
                            Collection<Serializable> newIds = ((CollectionDelta)mvd).getIds();

                            if (existingIds.size() != newIds.size())
                            {
                                throw new IllegalStateException(
                                    "inconsistent collection delta update, non-matching sizes (" +
                                    existingIds.size() + ", " + newIds.size());
                            }

                            for(Serializable s: existingIds)
                            {
                                if (!newIds.contains(s))
                                {
                                    throw new IllegalStateException(
                                        "inconsistent collection delta update, non-matching content (" +
                                        existingIds + ", " + newIds);
                                }
                            }
                        }
                        else
                        {
                            // an equal() member variable delta was added already
                            throw new IllegalStateException("duplicate delta " + mvd);
                        }
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
     * @return null if it cannot figure it out.
     */
    public AuditLogicalGroup getLogicalGroup(EventSource es, Serializable id, Object entity)
            throws Exception
    {
        SessionFactoryImpl sf = (SessionFactoryImpl)es.getSessionFactory();
        SessionFactoryHolder sfh = sessionFactoryHolders.get(sf);

        if (sfh == null || sfh.logicalGroupProvider == null)
        {
            // no provider, return null
            return null;
        }

        LogicalGroup lg = sfh.logicalGroupProvider.getLogicalGroup(es, id, entity);

        if (lg == null)
        {
            return null;
        }

        return sfh.logicalGroupCache.getLogicalGroup(lg);
    }

    /**
     * @return may return null if no selector was installed with this factory.
     */
    public AuditSelector getSelector(SessionFactory sf)
    {
        SessionFactoryHolder sfh = sessionFactoryHolders.get((SessionFactoryImpl)sf);

        if (sfh == null)
        {
            log.warn("no such session factory " + sf);
            return null;
        }

        return sfh.auditSelector;
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

    private void installAuditListeners(SessionFactoryImpl asf) throws Exception
    {
        EventListeners els = asf.getEventListeners();

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

        for(String auditedEventType: eventTypes)
        {
            Class c = Listeners.getAuditEventListenerClass(auditedEventType);
            Constructor ctor = c.getConstructor(Manager.class);
            AuditEventListener ael = (AuditEventListener)ctor.newInstance(this);

            Listeners.installAuditEventListener(asf, auditedEventType, ael);
        }

        log.debug(this + " installed audit listeners " + eventTypes + " on " + asf);
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
        LogicalGroupProvider logicalGroupProvider;
        LogicalGroupCache logicalGroupCache;
        AuditSelector auditSelector;

        SessionFactoryHolder(SessionFactoryImpl sessionFactory,
                             LogicalGroupProvider logicalGroupProvider,
                             LogicalGroupCache logicalGroupCache,
                             AuditSelector auditSelector)
        {
            this.sessionFactory = sessionFactory;
            this.logicalGroupProvider = logicalGroupProvider;
            this.logicalGroupCache = logicalGroupCache;
            this.auditSelector = auditSelector;
        }
    }
}
