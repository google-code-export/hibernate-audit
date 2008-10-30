package com.googlecode.hibernate.audit.util.wocache;

import org.hibernate.SessionFactory;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.StatelessSession;
import org.hibernate.impl.StatelessSessionImpl;
import org.hibernate.impl.SessionFactoryImpl;
import org.apache.log4j.Logger;

import javax.transaction.Synchronization;
import javax.transaction.Status;
import java.util.Map;
import java.util.HashMap;

import com.googlecode.hibernate.audit.util.Hibernate;

/**
 * A process-level upfront cache for write once instances. The implementation will only work for
 * instances that are written once and then never changed in the database.
 *
 * P stands for persistent type.
 *
 * TODO: Test and make sure is consistent for a multi-process use case.
 *
 * TODO: Test and make sure it works when accessed in the context of a local Hibernate transaction.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class WriteOnceCache<P>
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteOnceCache.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private SessionFactoryImpl sf;

    // only contains <b>committed<b> instances, for in-flight transaction-associated instances
    // look in synchronizations
    private Map<Key, P> committedCache;

    // TODO will only work in JTA environment for the time being,
    //      see https://jira.novaordis.org/browse/HBA-134
    private Map<javax.transaction.Transaction, PerTransactionCache> perTxCache;

    private Object instanceLock;

    // Constructors --------------------------------------------------------------------------------

    public WriteOnceCache(SessionFactory sf)
    {
        // TODO will throw a ClassCastException if not a SessionFactoryImpl, need to cast it
        //      in order to get a hold of TransactionmManager,
        //      see https://jira.novaordis.org/browse/HBA-134
        this.sf = (SessionFactoryImpl)sf;

        committedCache = new HashMap<Key, P>();
        perTxCache = new HashMap<javax.transaction.Transaction, PerTransactionCache>();
        instanceLock = new Object();
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * If the instance is available in the memory cache, returns it. Otherwise, if it is available
     * in the database, reads it and stores it in cache. If the instance is not present in the
     * database, it creates it.
     *
     * TODO better API  https://jira.novaordis.org/browse/HBA-125
     *
     * TODO rollback behavior on exception.
     *
     * @throws WriteOnceCacheException - if an exception occurs during the process of creation in
     *         the database of the instance corresponding to the given key.
     *
     */
    public P get(CacheQuery<P> cacheQuery) throws WriteOnceCacheException
    {
        Key key = cacheQuery.getKey();

        // will hold the main lock until we get the instance from the database, if necessary

        synchronized(instanceLock)
        {
            P result = committedCache.get(key);

            if (result != null)
            {
                return result;
            }

            Transaction tx = null;
            boolean failure = false;
            boolean weStartedTransaction = false;
            javax.transaction.Transaction jtaTx = null;

            try
            {
                // we don't use the intuitive StatelessSession because we need access to
                // isTransactionInProgress() and StatelessSession doesn't expose it
                StatelessSessionImpl ss = (StatelessSessionImpl)sf.openStatelessSession();

                // if no transaction was already started, write down the information that we are
                // starting it and start it, otherwise join ...
                if (!ss.isTransactionInProgress())
                {
                    weStartedTransaction = true;
                }

                tx = ss.beginTransaction();
                jtaTx = Hibernate.getUnderlyingTransaction(sf, tx);

                if (jtaTx == null)
                {
                    throw new IllegalStateException("null JTA transaction");
                }

                // not in committed cache, next look in transaction-associated syncronization (if
                // any) and try to get it from there first

                PerTransactionCache txCache = perTxCache.get(jtaTx);

                if (txCache == null)
                {
                    txCache = new PerTransactionCache(ss);
                    tx.registerSynchronization(txCache);
                    perTxCache.put(jtaTx, txCache);
                }
                else
                {
                    result = txCache.get(key);

                    if (result != null)
                    {
                        return result;
                    }
                }

                // not in any transaction-associated synchronization, look in the database and get
                // it from there

                Criteria c = cacheQuery.generateCriteria(ss);

                P dbValue = (P)c.uniqueResult(); // more than one record with identical keys will
                                                 // throw an exception
                if (dbValue != null)
                {
                    txCache.put(key, dbValue);
                    return dbValue;
                }

                // not in the database
                P newInstance = cacheQuery.createMatchingInstance();
                ss.insert(newInstance);

                // can't configure StatelessSession not to use batching, so I have to manually
                // execute the batch, otherwise I run into https://jira.novaordis.org/browse/HBA-127
                ss.getBatcher().executeBatch();

                txCache.put(key, newInstance);
                return newInstance;
            }
            catch(Throwable t)
            {
                failure = true;

                // we double-log because the thrown exception may be swallowed if a rollback failure
                // happens

                String msg =
                    "failed to retrieve or write WriteOnce type " + cacheQuery.getType().getName() +
                    " from/to database: " + t.getMessage();
                log.error(msg, t);
                throw new WriteOnceCacheException(msg, t);
            }
            finally
            {
                if (failure)
                {
                    tx.rollback();
                }
                else if (weStartedTransaction)
                {
                    // commit the transaction only if we started transaction, otherwise it will be
                    // committed by whoever started it
                    tx.commit();
                }

                // session will be closed in synchronization, DO NOT close it here
            }
        }
    }

    /**
     * Try to get the instance from cache and fail early (by returning null) if the instance is not
     * available. Don't attempt to go to the database, insert, etc. Hit and miss counters are
     * updated.
     *
     * @return the object from cache, or null if the object is not cached.
     */
    public P getFromCacheOnly(CacheQuery<P> cacheQuery)
    {
        Key key = cacheQuery.getKey();

        synchronized(committedCache)
        {
            return committedCache.get(key);
        }
    }

    public void clear()
    {
        synchronized(committedCache)
        {
            committedCache.clear();
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class PerTransactionCache implements Synchronization
    {
        private StatelessSession ss;
        private Map<Key, P> cache;

        private PerTransactionCache(StatelessSession ss)
        {
            this.ss = ss;
            this.cache = new HashMap<Key, P>();
        }

        // Synchronization implementation ----------------------------------------------------------

        public void beforeCompletion()
        {
            // noop
        }

        public void afterCompletion(int i)
        {
            try
            {
                if (i == Status.STATUS_COMMITTED)
                {
                    synchronized(committedCache)
                    {
                        committedCache.putAll(cache);
                    }
                }

                try
                {
                    ss.close();
                }
                catch(Throwable t)
                {
                    log.warn("failed to close stateless session " + ss, t);
                }
            }
            finally
            {
                synchronized(instanceLock)
                {
                    javax.transaction.Transaction key = null;

                    for(Map.Entry<javax.transaction.Transaction, PerTransactionCache> me:
                        perTxCache.entrySet())
                    {
                        if (this == me.getValue())
                        {
                            key = me.getKey();
                            break;
                        }
                    }

                    if (key != null)
                    {
                        perTxCache.remove(key);
                    }
                }
            }
        }

        // Private ---------------------------------------------------------------------------------

        private P get(Key key)
        {
            // no need for syncrhonization, as accessed from synchronized block
            return cache.get(key);
        }

        private void put(Key k, P v)
        {
            // no need for syncrhonization, as accessed from synchronized block
            cache.put(k, v);
        }
    }
}
