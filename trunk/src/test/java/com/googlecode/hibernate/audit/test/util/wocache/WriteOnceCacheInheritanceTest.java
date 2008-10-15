package com.googlecode.hibernate.audit.test.util.wocache;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.util.wocache.data.B;
import com.googlecode.hibernate.audit.test.util.wocache.data.C;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCache;
import com.googlecode.hibernate.audit.util.wocache.CacheQuery;

import java.util.List;

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
public class WriteOnceCacheInheritanceTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(WriteOnceCacheInheritanceTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testCacheBaseClass() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        config.addAnnotatedClass(C.class);

        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            s = sf.openSession();
            s.beginTransaction();

            WriteOnceCache<B> cache = new WriteOnceCache<B>(sf);

            CacheQuery<B> query = new CacheQuery<B>(B.class, "bs", "X");

            B b = cache.get(query);

            Long id = b.getId();
            assert id != null;
            assert "X".equals(b.getBs());

            s.getTransaction().commit();

            B b2 = cache.get(query);
            assert b.getId().equals(b2.getId());
            assert "X".equals(b2.getBs());


            s.beginTransaction();
            B db = (B)s.createQuery("from B").uniqueResult();
            assert b.getId().equals(db.getId());
            assert "X".equals(db.getBs());
            s.getTransaction().commit();

        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testCacheSubclass() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        config.addAnnotatedClass(C.class);

        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            s = sf.openSession();
            s.beginTransaction();

            WriteOnceCache<B> cache = new WriteOnceCache<B>(sf);

            CacheQuery<B> query = new CacheQuery<B>(C.class, "cs", "Y");

            C c = (C)cache.get(query);

            Long id = c.getId();
            assert id != null;
            assert "Y".equals(c.getCs());

            s.getTransaction().commit();

            C c2 = (C)cache.get(query);
            assert c.getId().equals(c2.getId());
            assert "Y".equals(c2.getCs());

            s.beginTransaction();
            C dc = (C)s.createQuery("from B").uniqueResult();
            assert c.getId().equals(dc.getId());
            assert "Y".equals(dc.getCs());
            s.getTransaction().commit();

        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testCacheBaseClassAndSubclass() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        config.addAnnotatedClass(C.class);

        SessionFactory sf = null;
        Session s = null;

        try
        {
            sf = config.buildSessionFactory();

            s = sf.openSession();
            s.beginTransaction();

            WriteOnceCache<B> cache = new WriteOnceCache<B>(sf);

            B b = cache.get(new CacheQuery<B>(B.class, "bs", "something"));
            C c = (C)cache.get(new CacheQuery<B>(C.class, "bs", "somethingelse", "cs", "totally"));

            Long bid = b.getId();
            assert bid != null;
            assert "something".equals(b.getBs());

            Long cid = c.getId();
            assert cid != null;
            assert "somethingelse".equals(c.getBs());
            assert "totally".equals(c.getCs());

            s.getTransaction().commit();

            B b2 = cache.get(new CacheQuery<B>(B.class, "bs", "something"));
            C c2 = (C)cache.get(new CacheQuery<B>(C.class, "bs", "somethingelse", "cs", "totally"));

            assert b.getId().equals(b2.getId());
            assert "something".equals(b2.getBs());

            assert c.getId().equals(c2.getId());
            assert "somethingelse".equals(c2.getBs());
            assert "totally".equals(c2.getCs());

            s.beginTransaction();
            List result = s.createQuery("from B").list();
            s.getTransaction().commit();

            assert result.size() == 2;

            for(Object o: result)
            {
                if (o instanceof C)
                {
                    assert cid.equals(((C)o).getId());
                    assert "somethingelse".equals(((C)o).getBs());
                    assert "totally".equals(((C)o).getCs());
                }
                else
                {
                    assert bid.equals(((B)o).getId());
                    assert "something".equals(((B)o).getBs());
                }
            }
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
