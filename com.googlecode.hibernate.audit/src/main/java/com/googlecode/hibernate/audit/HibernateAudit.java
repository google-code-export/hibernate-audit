package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.Settings;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.Manager;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
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
public final class HibernateAudit
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAudit.class);

    // Static --------------------------------------------------------------------------------------

    private static Manager manager;
    private static Object lock = new Object();

    /**
     * Enable audit on specified session factory, by dynamically registering listeners that
     * capture and record state changes on transactional commits. In order to disable audit on the
     * session, use HibernateAudit.disable(SessionFactory).
     *
     * @see HibernateAudit#disable(SessionFactory)
     *
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     */
    public static void enable(SessionFactory auditedSessionFactory) throws Exception
    {
        if (!(auditedSessionFactory instanceof SessionFactoryImpl))
        {
            throw new IllegalArgumentException(
                "cannot enable audit unless given session factory is a SessionFactoryImpl " +
                "instance; instead we got " +
                (auditedSessionFactory == null ? null : auditedSessionFactory.getClass().getName()));
        }

        SessionFactoryImpl sfi = (SessionFactoryImpl)auditedSessionFactory;

        synchronized(lock)
        {
            if (manager == null)
            {
                Settings settings = sfi.getSettings();
                manager = new Manager(settings);
                manager.start();
            }
        }

        manager.register(sfi);

        log.debug("audit enabled on " + sfi);
    }

    /**
     * @return true if audit manager is started, false otherwise.
     */
    public static boolean isStarted()
    {
        synchronized(lock)
        {
            return manager != null && manager.isStarted();
        }
    }

    /**
     * @return true if audit is enabled on the specified session factory instance, false otherwise.
     */
    public static boolean isEnabled(SessionFactory auditedSessionFactory)
    {
        if (!(auditedSessionFactory instanceof SessionFactoryImpl))
        {
            return false;
        }

        SessionFactoryImpl sfi = (SessionFactoryImpl)auditedSessionFactory;

        synchronized(lock)
        {
            return manager != null && manager.isRegistered(sfi);
        }
    }

    /**
     * Turns audit off on the specified session factory. If this is the last active factory
     * associated with the audit manager, the manager itself is stopped and all audit resources are
     * freed.
     *
     * @return true if audit is turned off on the specified session factory, or false if there was
     *         no active audit runtime to disable.
     */
    public static boolean disable(SessionFactory sf) throws Exception
    {
        synchronized(lock)
        {
            if (manager == null)
            {
                return false;
            }

            try
            {
                return manager.unregister(sf);
            }
            finally
            {
                if (manager.getAuditedSessionFactories().isEmpty())
                {
                    manager.stop();
                    manager = null;
                }
            }
        }
    }

    /**
     * Turns audit off on all registered session factories and stops the audit manager, freeing all
     * audit resources.
     *
     * @return true if audit was turned off on at least one session factory, or false if there was
     *         no active audit runtime to disable.
     */
    public static boolean disableAll() throws Exception
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
                result |= disable(s);
            }

            return result;
        }
    }

    // Queries -------------------------------------------------------------------------------------

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
                throw new IllegalStateException("audit stopped");
            }

            m = manager;
        }

        return m.query(query, args);
    }

    /**
     * Specialized query.
     *
     * @return the list of transactions that have been applied to the entity with the specified id.
     */
    public static List<AuditTransaction> getTransactions(Serializable entityId) throws Exception
    {
        String qs =
            "from AuditTransaction as t, AuditEvent as e " +
            "where e.transaction = t and e.id = :entityId";

        List result = query(qs, entityId);

        if (result.size() == 0)
        {
            return Collections.emptyList();
        }

        List<AuditTransaction> ts = new ArrayList<AuditTransaction>();
        for(Object o: result)
        {
            Object[] a = (Object[])o;
            ts.add((AuditTransaction)a[0]);
        }

        return ts;
    }

    // Delta functions -----------------------------------------------------------------------------

    /**
     * @param base - the intial state of the object to apply transactional delta to.
     */
    public static void delta(Object base, Long txId,
                             SessionFactory auditedSessionFactory) throws Exception
    {
        delta(base, null, txId, auditedSessionFactory);
    }

    /**
     * @param base - the intial state of the object to apply transactional delta to.
     */
    public static void delta(Object base, Serializable id, Long txId,
                             SessionFactory auditedSessionFactory) throws Exception
    {

        if(!(auditedSessionFactory instanceof SessionFactoryImplementor))
        {
            throw new IllegalArgumentException(
                auditedSessionFactory + " not a SessionFactoryImplementor");
        }

        Manager m = null;

        synchronized(lock)
        {
            if (manager == null)
            {
                throw new IllegalStateException("audit stopped");
            }

            m = manager;
        }

        SessionFactoryImplementor sfi = (SessionFactoryImplementor)auditedSessionFactory;
        m.delta(base, id, txId, sfi);
    }

    /**
     * Exposing the manager to the inner packages, until I refactor and I unify package protected
     * access.
     */
    public static Manager getManager()
    {
        return manager;
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
