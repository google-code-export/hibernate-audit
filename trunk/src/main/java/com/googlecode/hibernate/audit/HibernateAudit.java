package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.EntityMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.Type;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.Settings;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.QueryResult;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.LogicalGroupCache;
import com.googlecode.hibernate.audit.util.Reflections;
import com.googlecode.hibernate.audit.util.Hibernate;
import com.googlecode.hibernate.audit.delta.TransactionDelta;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.Date;
import java.io.Serializable;
import java.io.InputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;

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
public final class HibernateAudit
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAudit.class);

    private static final String HBA_VERSION_FILE_NAME = "HBA_VERSION";

    // Static --------------------------------------------------------------------------------------

    private static Manager manager;
    private static Object lock = new Object();

    private static String version;

    public static final String getVersion()
    {
        if (version == null)
        {
            InputStream is = null;
            BufferedReader br = null;

            try
            {
                is = HibernateAudit.class.
                    getClassLoader().getResourceAsStream(HBA_VERSION_FILE_NAME);

                if (is == null)
                {
                    throw new Exception("cannot locate resource '" + HBA_VERSION_FILE_NAME + "'");
                }
                
                br = new BufferedReader(new InputStreamReader(is));
                version = br.readLine();
            }
            catch(Exception e)
            {
                log.error("failed to read HBA version", e);

                version = "UNKNOWN";
            }
            finally
            {
                if (br != null)
                {
                    try
                    {
                        br.close();
                    }
                    catch(Exception e)
                    {
                        log.warn("failed to close version reader", e);
                    }
                }

                if (is != null)
                {
                    try
                    {
                        is.close();
                    }
                    catch(Exception e)
                    {
                        log.warn("failed to close version input stream", e);
                    }
                }
            }
        }

        return version;
    }

    /**
     * Starts the Hibernate Audit runtime (creates a valid manager). Upon successful method
     * completion, the Hibernate Audit runtime is started and ready to acceept SessionFactory.
     *
     * If the runtime is already started, the method is a noop.
     *
     * @param settings - setting for the internal session factory, which will be used to perist the
     *        audit log.
     */
    public static void startRuntime(Settings settings) throws Exception
    {
        synchronized(lock)
        {
            if (manager == null)
            {
                manager = new Manager(settings);
                boolean successfulStart = false;

                try
                {
                    manager.start();
                    successfulStart = true;
                }
                finally
                {
                    if (!successfulStart)
                    {
                        // cleanup ...
                        manager = null;
                        // ... and the exception will bubble up
                    }
                }
            }
        }
    }

    /**
     * Stops the Hibernate Audit runtime, freeing all resources. If SessionFactory instances are
     * registred, they are unregistered first.
     */
    public static void stopRuntime() throws Exception
    {
        synchronized(lock)
        {
            if (manager == null)
            {
                return;
            }

            unregisterAll();
            manager.stop();
            manager = null;
        }
    }

    /**
     * @return true if audit runtime is started (there is a valid manager), false otherwise.
     */
    public static boolean isStarted()
    {
        synchronized(lock)
        {
            return manager != null && manager.isStarted();
        }
    }

    /**
     * Registers the specified session factory with the audit runtime, by dynamically adding to it
     * listeners that capture and record state changes on transactional commits. In order to disable
     * audit logging on the session, use HibernateAudit.unregister(SessionFactory).
     *
     * The audit runtime must be up and running at the time of the invocation, otherwise an
     * IllegalStateException will be thrown.
     *
     * Note: The persistence events generated by sessions created by this auditedSessionFactory
     *       won't be associated with any applicaton level logical group. If you need logical group
     *       information in the database, use enable(SessionFactory, LogicalGroupProvider).
     *
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     *
     * @exception IllegalStateException if the runtime is not started when invoked.
     *
     * @see HibernateAudit#unregister(SessionFactory)
     * @see HibernateAudit#register(SessionFactory, LogicalGroupProvider)
     */
    public static void register(SessionFactory auditedSessionFactory) throws Exception
    {
        register(auditedSessionFactory, null, null);
    }

    public static void register(SessionFactory auditedSessionFactory, LogicalGroupProvider lgip) throws Exception
    {
        register(auditedSessionFactory, lgip, null);
    }

    public static void register(SessionFactory auditedSessionFactory, AuditSelector as) throws Exception
    {
        register(auditedSessionFactory, null, as);
    }

    /**
     * Registers the specified session factory with the audit runtime, by dynamically adding to it
     * listeners that capture and record state changes on transactional commits. In order to disable
     * audit logging on the session, use HibernateAudit.unregister(SessionFactory).
     *
     * The audit runtime must be up and running at the time of the invocation, otherwise an
     * IllegalStateException will be thrown.
     *
     * @see HibernateAudit#unregister(SessionFactory)
     *
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     *
     * @param lg - the application-level LogicalGroupProvider. If null, no logical group id
     *        will persisted in the database. This is alright if you don't need logical grouping
     *        of entities.
     *
     * @param as - the application-level AuditSelector implementation. The application provides it
     *        as a way to specify what persistent entities it wants audited. Form more details on
     *        AuditSelector usage and how it interacts with @Audited annotations, see AuditSelector
     *        javadoc.
     *
     * @exception IllegalStateException if the runtime is not started when invoked.
     *
     * @see AuditSelector
     */
    public static void register(SessionFactory auditedSessionFactory,
                                LogicalGroupProvider lg,
                                AuditSelector as) throws Exception
    {
        Manager m = null;
        synchronized(lock)
        {
            if (!isStarted())
            {
                throw new IllegalStateException("Hibernate Audit runtime not started");
            }

            m = manager;
        }

        if (!(auditedSessionFactory instanceof SessionFactoryImplementor))
        {
            throw new IllegalArgumentException(
                "cannot enable audit unless given session factory is a SessionFactoryImplementor " +
                "instance; instead we got " +
                (auditedSessionFactory == null ? null : auditedSessionFactory.getClass().getName()));
        }

        m.register((SessionFactoryImplementor)auditedSessionFactory, lg, as);

        log.debug(auditedSessionFactory + " registered with the audit runtime");
    }

    /**
     * Turns audit off on specified session factory, by unregistering it from the runtime.
     *
     * @return true if audit is turned off on the specified session factory, or false if there was
     *         no active audit runtime to disable.
     */
    public static boolean unregister(SessionFactory sf) throws Exception
    {
        synchronized(lock)
        {
            return manager != null && manager.unregister(sf);
        }
    }

    /**
     * Unregisters all registered session factories, but it doesn't stop the runtime. If you want
     * to stop the runtime, use stop().
     *
     * @return true if at least one session factory instance was unregistered, false if there were
     *         no registered session factories or the audit runtime was stopped.
     */
    public static boolean unregisterAll() throws Exception
    {
        synchronized(lock)
        {
            if (manager == null)
            {
                return false;
            }

            boolean result = false;

            for(SessionFactoryImpl s: manager.getAuditedSessionFactories())
            {
                result |= unregister(s);
            }

            return result;
        }
    }

    /**
     * @return true if audit is enabled on the specified session factory instance, false otherwise.
     */
    public static boolean isRegistered(SessionFactory auditedSessionFactory)
    {
        if (!(auditedSessionFactory instanceof SessionFactoryImpl))
        {
            return false;
        }

        synchronized(lock)
        {
            return manager != null &&
                   manager.isRegistered((SessionFactoryImpl)auditedSessionFactory);
        }
    }

    // Generic Queries -----------------------------------------------------------------------------

    /**
     * A general purpose query facility. Understands HQL. Does close the query session on exit
     * so it cannot be used to explore lazily loaded relationships.
     */
    public static List query(String query, Object... args) throws Exception
    {
        Manager m = getManagerOrFail();
        return m.query(query, false, args).getResult();
    }

    // Specialized Queries -------------------------------------------------------------------------


    /**
     * Returns *ALL* logged transactions. USE WITH CARE, because it may return a lot of data.
     */
    public static List<AuditTransaction> getTransactions() throws Exception
    {
        return getTransactions(null);
    }

    /**
     * Specialized query.
     *
     * TODO add tests.
     *
     * TODO - loads lazy relationships - potential performance hit.
     *
     * @param entityId - if null, returns all transactions. This may be a very costly operation.
     *
     * @return the list of transactions that have been applied to the entity with the specified id.
     */
    public static List<AuditTransaction> getTransactions(Serializable entityId) throws Exception
    {
        Manager m = getManagerOrFail();
        QueryResult qr = null;

        try
        {
            if (entityId == null)
            {
                String qs = "from AuditTransaction as t order by t.id";
                qr = m.query(qs, true);
            }
            else
            {
                String qs =
                    "from AuditTransaction as t, AuditEvent as e " +
                    "where e.transaction = t and e.targetId = :entityId order by t.id";

                qr = m.query(qs, true, entityId);
            }

            List result = qr.getResult();
            if (result.size() == 0)
            {
                return Collections.emptyList();
            }

            List<AuditTransaction> ts = new ArrayList<AuditTransaction>();

            for(Object o: result)
            {
                AuditTransaction at =
                    entityId == null ? (AuditTransaction)o : (AuditTransaction)((Object[])o)[0];

                if (!ts.contains(at))
                {
                    ts.add(at);
                }

                // walk the lazy loaded relationship
                // TODO: performance hit when I don't actually need this
                at.getEvents().isEmpty();
            }

            return ts;
        }
        finally
        {
            // make sure the transaction is committed and query session is closed

            if (qr != null)
            {
                Session qs = qr.getSession();

                if (qs != null)
                {
                    Transaction ht = qs.getTransaction();

                    if (ht != null)
                    {
                        ht.commit();
                    }

                    qs.close();
                }
            }
        }
    }

    /**
     * Specialized query.
     *
     * TODO add tests.
     *
     * TODO - loads lazy relationships - potential performance hit.
     *
     * @return the list of transactions that have been applied to entities belonging to the
     *         specified logical group.
     */
    public static List<AuditTransaction> getTransactionsByLogicalGroup(LogicalGroup lg)
        throws Exception
    {
        return getTransactionsByLogicalGroup(lg, null);
    }

    /**
     * Specialized query.
     *
     * TODO add tests.
     *
     * TODO - loads lazy relationships - potential performance hit.
     *
     * @return the list of transactions that have been applied to entities belonging to the
     *         specified logical group, satisfying the filter.
     */
    public static List<AuditTransaction> getTransactionsByLogicalGroup(LogicalGroup lg,
                                                                       TransactionFilter filter)
        throws Exception
    {

        Manager m = getManagerOrFail();

        Date from = null;
        Date to = null;
        String user = null;
        Long entityTypeId = null;

        if (filter != null)
        {
            from = filter.getFromDate();
            to = filter.getToDate();
            user = filter.getUser();
            entityTypeId = filter.getAuditEntityTypeId();
        }

        from = from == null ? new Date(0) : from;
        to = to == null ? new Date(Long.MAX_VALUE) : to;

        QueryResult qr = null;

        try
        {

            // use the logical group cache

            // TODO OASF - we assume there's a single registered audited session factory
            Set<SessionFactoryImpl> asfs = m.getAuditedSessionFactories();
            if (asfs.size() != 1)
            {
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            LogicalGroupCache lgc = m.getLogicalGroupCache(asfs.iterator().next());
            AuditLogicalGroup alg = lgc.getLogicalGroup(lg, false);

            if (alg == null)
            {
                return Collections.emptyList();
            }

            if (entityTypeId == null)
            {
                String qs =
                    "from AuditTransaction as t where " +
                    "t.timestamp >= :from and " +
                    "t.timestamp <= :to and " +
                    "t in (select transaction from AuditEvent where logicalGroup = :alg ) " +
                    "order by t.id";

                qr = m.query(qs, true, from, to, alg);
                return qr.getResult();
            }
            else
            {
                String qs =
                    "from AuditTransaction as tx, AuditEvent as e, AuditEntityType as t where " +
                    "e.transaction = tx and " +
                    "e.targetType = t and " +
                    "t.id = :entityTypeId and " +
                    "e.logicalGroup = :alg " +
                    "order by tx.id";

                qr = m.query(qs, true, entityTypeId, alg);

                List result = qr.getResult();

                if (result.size() == 0)
                {
                    return Collections.emptyList();
                }

                List<AuditTransaction> ts = new ArrayList<AuditTransaction>();
                for(Object o: result)
                {
                    AuditTransaction at = (AuditTransaction)((Object[])o)[0];

                    if (!ts.contains(at))
                    {
                        ts.add(at);
                    }

                    // walk the lazy loaded relationship
                    // TODO: performance hit when I don't actually need this
                    at.getEvents().isEmpty();
                }

                return ts;
            }
        }
        finally
        {
            // make sure the transaction is committed and query session is closed

            if (qr != null)
            {
                Session qs = qr.getSession();

                if (qs != null)
                {
                    Transaction ht = qs.getTransaction();

                    if (ht != null)
                    {
                        ht.commit();
                    }

                    qs.close();
                }
            }
        }
    }

    /**
     * @return the latest (most recent) recorded transaction for this specific logical group or
     *         null if there's not such transaction.
     *
     * @exception IllegalStateException if the audit runtime was not started.
     */
    public static AuditTransaction getLatestTransactionForLogicalGroup(LogicalGroup lg)
        throws Exception
    {
        Manager m = getManagerOrFail();

        QueryResult qr = null;

        try
        {
            // TODO OASF
            // we assume there's only one audites session factory, this is WRONG
            Set<SessionFactoryImpl> asfs = m.getAuditedSessionFactories();
            SessionFactoryImpl asf = asfs.iterator().next();
            LogicalGroupCache lgc = m.getLogicalGroupCache(asf);

            AuditLogicalGroup alg = lgc.getLogicalGroup(lg, false);

            if (alg == null)
            {
                return null;
            }

            String qs =
                "from AuditTransaction " +
                " where id = " +
                " ( select max(e.transaction.id) from AuditEvent as e " +
                "                                where e.logicalGroup = :alg )";

            qr = m.query(qs, true, alg);
            List result = qr.getResult();

            if (result.isEmpty())
            {
                return null;
            }

            AuditTransaction tx = (AuditTransaction)result.get(0);

            // walk the lazy loaded relationship
            // TODO: performance hit when I don't actually need this
            tx.getEvents().isEmpty();
            
            return tx;
        }
        finally
        {
            // make sure the transaction is committed and query session is closed

            if (qr != null)
            {
                Session qs = qr.getSession();

                if (qs != null)
                {
                    Transaction ht = qs.getTransaction();

                    if (ht != null)
                    {
                        ht.commit();
                    }

                    qs.close();
                }
            }
        }
    }

    /**
     * @return the latest (most recent) recorded transaction for this specific entity, or null if
     *         there is not such transaction.
     *
     * @exception IllegalStateException if the audit runtime was not started.
     */
    public static AuditTransaction getLatestTransaction(String entityName, Serializable entityId)
        throws Exception
    {
        // TODO totally inefficient, can be optimized

        Class idClass = entityId.getClass();

        String qs = "from AuditEntityType as at where at.className = ? and at.idClassName = ?";

        // TODO - make sure this goes through the type cache, otherwise I end up with persistence context collisions
        List result = query(qs, entityName, idClass.getName());

        if (result.isEmpty())
        {
            return null;
        }

        if (result.size() > 1)
        {
            throw new IllegalStateException("multiple entities " + entityName + "[" +
                                            idClass.getName() + "] in the type table");
        }

        AuditEntityType aet = (AuditEntityType)result.get(0);

        qs =
            "from AuditEvent where id = " +
            "( select max(e.id) from AuditEvent as e " +
            "  where e.targetType = :aet and e.targetId = :entityId )";

        result = query(qs, aet, entityId);

        if (result.isEmpty())
        {
            return null;
        }

        return ((AuditEvent)result.get(0)).getTransaction();
    }

    /**
     * TODO very bad implementation, must be optimized
     *
     * @return the value of the specified field of the specified entity as it was available in the
     *         database for the given version. May retrun null if the value corresponding to the
     *         given version was null.
     *
     * @throws IllegalArgumentException for an invalid entityName, entityId or version.
     * @throws org.hibernate.HibernateException for an invalid field name.
     */
    public static Object getValue(SessionFactoryImplementor sf,
                                  String entityName, Serializable entityId,
                                  String fieldName, Long version) throws Exception
    {
        Manager m = getManagerOrFail();

        // currently we implicitly assume 'entityName' is class name, this has to change
        // TODO https://jira.novaordis.org/browse/HBA-80
        Class entityClass = null;
        try
        {
            entityClass = Class.forName(entityName);
        }
        catch(Exception e)
        {
            // try tuplizer
            EntityPersister ep = sf.getEntityPersister(entityName);
            entityClass = Hibernate.getTypeFromTuplizer(ep, EntityMode.POJO);
        }
        
        Class idClass = entityId.getClass();

        TypeCache tc = m.getTypeCache();
        AuditEntityType entityType = tc.getAuditEntityType(idClass, entityClass, false);

        if (entityType == null)
        {
            throw new IllegalArgumentException(
                "entity " + entityName + "[" + idClass.getName() +
                "] was not seen by the audit framework yet");
        }

        Type pt = sf.getEntityPersister(entityName).getPropertyType(fieldName);
        AuditType fieldType = Hibernate.hibernateTypeToAuditType(pt, tc, sf);
        AuditTypeField field = tc.getAuditTypeField(fieldName, fieldType, false);

        if (field == null)
        {
            throw new IllegalArgumentException(
                "field " + entityName + "[" + idClass.getName() +
                "]." + fieldName + " was not seen by the audit framework yet");
        }

        // TODO inefficient, probably these two queries can be coalesced

        String qs =
            "select count(*) from AuditTransaction as t, AuditEvent as e where " +
            "e.transaction = t and " +
            "e.targetId = :entityId and " +
            "e.targetType = :entityType and t.id <= :version";

        // TODO have an "uniqueResult" version for query()
        
        List result = query(qs, entityId, entityType, version);

        Long count = (Long)result.get(0);

        if (count.longValue() == 0)
        {
            throw new IllegalArgumentException(
                "no trace of " + entityName + "[" + entityId +
                "] with version smaller or equal with " + version +
                " has been recorded by the audit framework yet");
        }

        qs =
            "from AuditTransaction t, AuditEvent as e, AuditEventPair as p where " +
            "e.transaction = t and " +
            "e.targetId = :entityId and " +
            "e.targetType = :entityType and " +
            "p.event = e and " +
            "p.field = :field and t.id <= :version " +
            "order by t.id desc";

        // TODO - bad, this returns a whole bunch of things, unnecessariyl

        result = query(qs, entityId, entityType, field, version);

        if (result.isEmpty())
        {
            // no change recorded for the field, hence is null
            return null;
        }

        return ((AuditEventPair)(((Object[])result.get(0))[2])).getValue();
    }

    // Delta functions -----------------------------------------------------------------------------

    /**
     * @param txId the ide of the transaction we want delta for.
     *
     * @return the transaction delta or null if no such transaction if found.
     *
     * @throws Exception could be caused by an abnormal condition while accessing the database.
     */
    public static TransactionDelta getDelta(Long txId) throws Exception
    {
        Manager m = getManagerOrFail();
        return m.getDelta(txId);
    }

    // Others --------------------------------------------------------------------------------------

    /**
     * TODO
     *
     * Exposing the manager to the inner packages, until I refactor and I unify package protected
     * access.
     */
    public static Manager getManager()
    {
        return manager;
    }

    public static boolean registerImmutableClass(Class c)
    {
        return Reflections.registerImmutableClass(c);
    }

    public static Set<Class> getImmutableClasses()
    {
        return Reflections.getImmutableClasses();
    }

    public static boolean unregisterImmutableClass(Class c)
    {
        return Reflections.unregisterImmutableClass(c);
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "HibernateAudit[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    /**
     *
     * @return a non-null manager reference or fail with IllegalStateException
     *         'audit runtime not started'.
     */
    private static Manager getManagerOrFail()
    {
        Manager m = null;

        synchronized(lock)
        {
            if (manager == null)
            {
                throw new IllegalStateException("audit runtime not started");
            }

            m = manager;
        }

        return m;
    }

    // Inner classes -------------------------------------------------------------------------------
}
