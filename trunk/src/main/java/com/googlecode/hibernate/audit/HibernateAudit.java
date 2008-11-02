package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.Settings;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.util.Reflections;
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
     *       information in the database, use enable(SessionFactory, LogicalGroupIdProvider).
     *
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     *
     * @exception IllegalStateException if the runtime is not started when invoked.
     *
     * @see HibernateAudit#unregister(SessionFactory)
     * @see HibernateAudit#register(SessionFactory, LogicalGroupIdProvider)
     */
    public static void register(SessionFactory auditedSessionFactory) throws Exception
    {
        register(auditedSessionFactory, null);
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
     * @param lgip - the application-level LogicalGroupIdProvider. If null, no logical group id
     *        will persisted in the database. This is alright if you don't need logical grouping
     *        of entities.
     *
     * @exception IllegalStateException if the runtime is not started when invoked.
     */
    public static void register(SessionFactory auditedSessionFactory, LogicalGroupIdProvider lgip)
        throws Exception
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

        m.register((SessionFactoryImplementor)auditedSessionFactory, lgip);

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
     * A general purpose query facility. Understands HQL.
     */
    public static List query(String query, Object... args) throws Exception
    {
        Manager m = null;

        synchronized(lock)
        {
            if (manager == null)
            {
                throw new IllegalStateException("audit runtime not enabled");
            }

            m = manager;
        }

        return m.query(query, args);
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
     * @param entityId - if null, returns all transactions. This may be a very costly operation.
     *
     * @return the list of transactions that have been applied to the entity with the specified id.
     */
    public static List<AuditTransaction> getTransactions(Serializable entityId) throws Exception
    {
        List result = null;
        if (entityId == null)
        {
            String qs = "from AuditTransaction as t order by t.id";
            result = query(qs);
        }
        else
        {
            String qs =
                "from AuditTransaction as t, AuditEvent as e " +
                "where e.transaction = t and e.targetId = :entityId order by t.id";

            result = query(qs, entityId);
        }

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
        }

        return ts;
    }

    /**
     * Specialized query.
     *
     * TODO add tests.
     *
     * @return the list of transactions that have been applied to entities belonging to the
     *         specified logical group.
     */
    public static List<AuditTransaction> getTransactionsByLogicalGroup(Serializable lgId)
        throws Exception
    {
        return getTransactionsByLogicalGroup(lgId, null);
    }

    /**
     * Specialized query.
     *
     * TODO add tests.
     *
     * @return the list of transactions that have been applied to entities belonging to the
     *         specified logical group, satisfying the filter.
     */
    public static List<AuditTransaction> getTransactionsByLogicalGroup(Serializable lgId,
                                                                       TransactionFilter filter)
        throws Exception
    {
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

        if (entityTypeId == null)
        {
            String qs =
                "from AuditTransaction as t " +
                "where t.logicalGroupId = :lgId and " +
                "t.timestamp >= :from and " +
                "t.timestamp <= :to " +
                "order by t.id";

            return query(qs, lgId, from, to);
        }
        else
        {
            String qs =
                "from AuditTransaction as tx, AuditEvent as e, AuditEntityType as t " +
                "where tx.logicalGroupId = :lgId and " +
                "e.transaction = tx and " +
                "e.targetType = t and " +
                "t.id = :entityTypeId " +
                "order by tx.id";

            List result = query(qs, lgId, entityTypeId);

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
            }

            return ts;
        }
    }

    /**
     * @return the latest (most recent) recorded transaction for this specific logical group or
     *         null if there's not such transaction.
     *
     * @exception IllegalStateException if the audit runtime was not started.
     */
    public static AuditTransaction getLatestTransactionByLogicalGroup(Serializable lgId)
        throws Exception
    {
        String qs =
            "from AuditTransaction where id = " +
            "( select max(t.id) from AuditTransaction as t where t.logicalGroupId = :lgId )";

        List result = query(qs, lgId);

        if (result.isEmpty())
        {
            return null;
        }

        return (AuditTransaction)result.get(0);
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
        Manager m = null;

        synchronized(lock)
        {
            if (manager == null)
            {
                throw new IllegalStateException("audit runtime not enabled");
            }

            m = manager;
        }

        return m.getDelta(txId);
    }

    // Others --------------------------------------------------------------------------------------

    /**
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

    // Inner classes -------------------------------------------------------------------------------
}
