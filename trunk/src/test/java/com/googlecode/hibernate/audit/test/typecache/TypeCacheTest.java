package com.googlecode.hibernate.audit.test.typecache;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.typecache.data.A;
import com.googlecode.hibernate.audit.test.typecache.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditTypeField;

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
public class TypeCacheTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(TypeCacheTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    /**
     * https://jira.novaordis.org/browse/HBA-149
     */
    @Test(enabled = false)
    public void testPersistenceContextCollision() throws Exception
    {
        log.debug("test");

        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;
        Session s = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // first, write a type and a fileld in the database via the write-once cache

            s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            B b = new B();
            a.setB(b);

            s.save(a);

            s.getTransaction().commit();

            // we have the type "B" and the field "B.b" in the database

            // now clear the type cache to force it to reload

            HibernateAudit.getManager().getTypeCache().clear();

            // reload the cache again

            s = sf.openSession();
            s.beginTransaction();

            A da = (A)s.get(A.class, a.getId());
            B b2 = new B();
            da.setB(b2);
            s.update(da);
            
            s.getTransaction().commit();

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 2;

        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    @Test(enabled = true)
    public void testTypeCache_PreSavedTypesAndFields() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;
        Session s = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // save types by hand

            AuditEntityType atype = new AuditEntityType(Long.class, A.class);
            AuditEntityType btype = new AuditEntityType(Long.class, B.class);
            AuditTypeField bfield = new AuditTypeField();
            bfield.setName("b");
            bfield.setType(btype);

            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            Session is = isf.openSession();
            is.beginTransaction();
            is.save(atype);
            is.save(btype);
            is.save(bfield);
            
            is.getTransaction().commit();

            s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            B b = new B();
            a.setB(b);

            s.save(a);

            s.getTransaction().commit();


            s = sf.openSession();
            s.beginTransaction();

            A da = (A)s.get(A.class, a.getId());

            s.getTransaction().commit();

            assert da.getB().getId().equals(b.getId());

            List<AuditTransaction> txs = HibernateAudit.getTransactions();
            assert txs.size() == 1;
        }
        finally
        {
            HibernateAudit.stopRuntime();

            if (sf != null)
            {
                sf.close();
            }
        }
    }

    /**
     * https://jira.novaordis.org/browse/HBA-164
     */
    @Test(enabled = false)
    public void test_HBA164() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(A.class);
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;
        Session s = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            // save types by hand

            AuditEntityType atype = new AuditEntityType(Long.class, A.class);
            AuditEntityType btype = new AuditEntityType(Long.class, B.class);
            AuditTypeField bfield = new AuditTypeField();
            bfield.setName("b");
            bfield.setType(btype);

            SessionFactory isf = HibernateAudit.getManager().getSessionFactory();
            Session is = isf.openSession();
            is.beginTransaction();
            is.save(atype);
            is.save(btype);
            is.save(bfield);

            is.getTransaction().commit();

            s = sf.openSession();
            s.beginTransaction();

            A a = new A();
            B b = new B();
            a.setB(b);

            s.save(a);

            s.getTransaction().commit();
        }
        finally
        {
            HibernateAudit.stopRuntime();

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