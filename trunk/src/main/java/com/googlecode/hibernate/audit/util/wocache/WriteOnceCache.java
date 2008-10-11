package com.googlecode.hibernate.audit.util.wocache;

import org.hibernate.SessionFactory;
import org.hibernate.StatelessSession;
import org.hibernate.Criteria;
import org.apache.log4j.Logger;

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

    private long hits;
    private long misses;
    private long insertions;

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
     * TODO rollback behavior on exception.
     *
     * TODO better API  https://jira.novaordis.org/browse/HBA-125
     *
     * @throws Exception - if an exception occurs during the process of creation in the database of
     *         the instance corresponding to the given key.
     *
     */
    public P get(CacheQuery<P> cacheQuery) throws Exception
    {
        Key key = cacheQuery.getKey();

        // TODO how I make sure type is P

        synchronized(cache)
        {
            P result = cache.get(key);

            if (result != null)
            {
                hits ++;
                return result;
            }

            misses ++;
        }

        // TODO what happens if other thread places antoher instance in cache now?

        // not in cache, look in the database and get it from there

        StatelessSession ss = null;
        boolean failure = false;

        try
        {
            ss = sf.openStatelessSession();

            Criteria c = cacheQuery.generateCriteria(ss);

            // TODO make sure I don't enroll in the current transaction
            ss.beginTransaction();

            // TODO what happens if I have more than one
            synchronized(cache)
            {
                P o = (P)c.uniqueResult();

                if (o != null)
                {
                    cache.put(key, o);
                    return o;
                }

                // not in the database

                P newInstance = cacheQuery.createInstanceMatchingQuery();
                ss.insert(newInstance);
                insertions ++;
                cache.put(key, newInstance);
                return newInstance;
            }
        }
        catch(Throwable t)
        {
            log.error(t);
            failure = true;
            throw new Exception("", t);
        }
        finally
        {
            if (failure)
            {
                ss.getTransaction().rollback();
            }
            else
            {
                ss.getTransaction().commit();
            }

            if (ss != null)
            {
                ss.close();
            }
        }
    }

    /**
     * It also resets all counters.
     */
    public void clear()
    {
        synchronized(cache)
        {
            cache.clear();
            hits = 0;
            misses = 0;
            insertions = 0;
        }
    }

    public long getHitCount()
    {
        return hits;
    }

    public long getMissCount()
    {
        return misses;
    }

    public long getDatabaseInsertionCount()
    {
        return insertions;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
