package com.googlecode.hibernate.audit.test.util.wocache;

import org.testng.annotations.Test;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.wocache.data.A;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCache;
import com.googlecode.hibernate.audit.util.wocache.CacheQuery;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class WriteOnceCacheTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Primitive Updates ---------------------------------------------------------------------------

    @Test(enabled = true)
    public void testAccessCacheWitouthExternalTransaction() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        SessionFactory sf = null;

        try
        {
            sf = config.buildSessionFactory();

            WriteOnceCache<A> cache = new WriteOnceCache<A>(sf);

            assert 0 == cache.getHitCount();
            assert 0 == cache.getMissCount();
            assert 0 == cache.getDatabaseInsertionCount();

            CacheQuery<A> cquery = new CacheQuery<A>(A.class, "s", "blah");
            A a = cache.get(cquery);

            assert 0 == cache.getHitCount();
            assert 1 == cache.getMissCount();
            assert 1 == cache.getDatabaseInsertionCount();

            Long id = a.getId();
            assert id != null;
            assert "blah".equals(a.getS());

            cache.clear();

            a = (A)cache.get(cquery);

            assert 0 == cache.getHitCount();
            assert 1 == cache.getMissCount();
            assert 0 == cache.getDatabaseInsertionCount();

            assert id.equals(a.getId());
            assert "blah".equals(a.getS());

            a = (A)cache.get(cquery);

            assert 1 == cache.getHitCount();
            assert 1 == cache.getMissCount();
            assert 0 == cache.getDatabaseInsertionCount();

            assert id.equals(a.getId());
            assert "blah".equals(a.getS());
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
