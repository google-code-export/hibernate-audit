package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import org.hibernate.Session;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.TypeCache;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.test.model.data.B;
import com.googlecode.hibernate.audit.HibernateAudit;

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
public class AuditLogicalGroupTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(AuditLogicalGroupTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testSetDefiningEntityName_NoSessionFactory() throws Exception
    {
        AuditLogicalGroup alg = new StretchedAuditLogicalGroup();

        try
        {
            alg.setDefiningEntityName(B.class.getName());
            throw new Error("should've failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(">>> "  + e.getMessage());
        }
    }

    @Test(enabled = true)
    public void testSetDefiningEntityName_NoTypeCache() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            AuditLogicalGroup alg = new StretchedAuditLogicalGroup(sf);

            try
            {
                alg.setDefiningEntityName(B.class.getName());
                throw new Error("should've failed");
            }
            catch(IllegalStateException e)
            {
                log.debug(">>> "  + e.getMessage());
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

    @Test(enabled = true)
    public void testSetDefiningEntityName() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            TypeCache tc = HibernateAudit.getManager().getTypeCache();
            AuditLogicalGroup alg = new AuditLogicalGroup(tc, sf);

            alg.setDefiningEntityName(B.class.getName());

            assert B.class.getName().equals(alg.getDefiningEntityName());
            AuditType at = alg.getAuditType();
            assert B.class.equals(at.getClassInstance());
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
    public void testPullAuditLogicalGroupFromDatabase() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        config.addAnnotatedClass(B.class);
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());
            HibernateAudit.register(sf);

            Manager m = HibernateAudit.getManager();
            TypeCache tc = m.getTypeCache();
            AuditLogicalGroup alg = new AuditLogicalGroup(tc, sf);
            alg.setDefiningEntityName(B.class.getName());
            alg.setLogicalGroupId(new Long(77));

            SessionFactory isf = m.getSessionFactory();
            Session is = isf.openSession();
            is.beginTransaction();
            is.save(alg);
            is.getTransaction().commit();
            is.close();


            is = isf.openSession();
            is.beginTransaction();
            AuditLogicalGroup dalg =
                    (AuditLogicalGroup)is.get(AuditLogicalGroup.class, alg.getId());
            is.getTransaction().commit();
            is.close();

            assert B.class.getName().equals(dalg.getDefiningEntityName());

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

    class StretchedAuditLogicalGroup extends AuditLogicalGroup
    {
        /**
         * Exposes access to protected constructor.
         */
        StretchedAuditLogicalGroup()
        {
            super();
        }

        /**
         * Exposes access to protected constructor.
         */
        StretchedAuditLogicalGroup(SessionFactoryImplementor sf)
        {
            super(null, sf);
        }
    }
}