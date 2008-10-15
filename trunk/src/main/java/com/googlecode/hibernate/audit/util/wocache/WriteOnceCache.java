package com.googlecode.hibernate.audit.util.wocache;

import org.hibernate.SessionFactory;
import org.hibernate.Criteria;
import org.hibernate.Transaction;
import org.hibernate.StatelessSession;
import org.hibernate.impl.StatelessSessionImpl;
import org.apache.log4j.Logger;

import javax.transaction.Synchronization;
import javax.transaction.Status;
import java.util.Map;
import java.util.HashMap;

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

    private SessionFactory sf;
    private Map<Key, P> cache;

    // Constructors --------------------------------------------------------------------------------

    public WriteOnceCache(SessionFactory sf)
    {
        this.sf = sf;
        cache = new HashMap<Key, P>();
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

        synchronized(cache)
        {
            P result = cache.get(key);  // TODO reentrance is broken

            if (result != null)
            {
                return result;
            }

            // not in cache, look in the database and get it from there

            // we don't use the intuitive StatelessSession because we need access to
            // isTransactionInProgress() and StatelessSession doesn't expose it
            StatelessSessionImpl ss = null;

            boolean failure = false;
            boolean weStartedTransaction = false;
            Transaction tx = null;

            try
            {
                ss = (StatelessSessionImpl)sf.openStatelessSession();

                // if no transaction was already started, write down the information that we are
                // starting it and start it, otherwise join ...
                if (!ss.isTransactionInProgress())
                {
                    weStartedTransaction = true;
                }

                tx = ss.beginTransaction();

                WriteOnceCacheSynchronization sync = new WriteOnceCacheSynchronization(key, ss);
                tx.registerSynchronization(sync);

                Criteria c = cacheQuery.generateCriteria(ss);

                P o = (P)c.uniqueResult(); // more than one record with identical keys will throw
                                           // an exception

                if (o != null)
                {
                    sync.setValue(o);
                    return o;
                }

                // not in the database
                P newInstance = cacheQuery.createMatchingInstance();
                ss.insert(newInstance);

                // can't configure StatelessSession not to use batching, so I have to manually
                // execute the batch, otherwise I run into https://jira.novaordis.org/browse/HBA-127
                ss.getBatcher().executeBatch();

                sync.setValue(newInstance);
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

        synchronized(cache)
        {
            return cache.get(key);
        }
    }

    public void clear()
    {
        synchronized(cache)
        {
            cache.clear();
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class WriteOnceCacheSynchronization implements Synchronization
    {
        private StatelessSession ss;
        private Key key;
        private P value;

        private WriteOnceCacheSynchronization(Key key, StatelessSession ss)
        {
            this.key = key;
            this.ss = ss;
        }

        public void beforeCompletion()
        {
            // noop
        }

        public void afterCompletion(int i)
        {
            if (i == Status.STATUS_COMMITTED)
            {
                if (value == null)
                {
                    throw new IllegalStateException(
                        "write once cache synchronization not properly set up, null value");
                }

                synchronized(cache)
                {
                    cache.put(key, value);
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

        private void setValue(P value)
        {
            this.value = value;
        }
    }
}
