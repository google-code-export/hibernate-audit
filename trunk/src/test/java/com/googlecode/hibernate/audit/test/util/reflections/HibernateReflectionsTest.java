package com.googlecode.hibernate.audit.test.util.reflections;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.EntityMode;
import com.googlecode.hibernate.audit.util.reflections.HibernateReflections;
import com.googlecode.hibernate.audit.test.util.reflections.data.B;
import com.googlecode.hibernate.audit.test.util.reflections.data.A;

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
public class HibernateReflectionsTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateReflectionsTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testApplyChanges_Primitives() throws Throwable
    {
        log.debug("testApplyChanges_Primitives");

        AnnotationConfiguration conf = new AnnotationConfiguration();
        conf.configure("/hibernate-thread.cfg.xml");
        conf.getProperties().setProperty("hibernate.show_sql", "false");
        conf.addAnnotatedClass(A.class);
        conf.addAnnotatedClass(B.class);

        SessionFactoryImplementor sf = null;

        EntityMode entityMode = EntityMode.POJO;

        // TODO what about other entity modes?

        try
        {
            sf = (SessionFactoryImplementor)conf.buildSessionFactory();

            A base = new A();

            A modified = new A();
            modified.setI(new Integer(1));
            modified.setS("a");

            HibernateReflections.applyChanges(sf, entityMode, base, modified);

            assert new Integer(1).equals(base.getI());
            assert "a".equals(base.getS());
        }
        finally
        {
            if (sf != null)
            {
                sf.close();
            }
        }
    }

//    @Test(enabled = true) // TODO enable if I ever decide to pursue this path, which I doubt
//    public void testApplyChanges_Collections() throws Throwable
//    {
//        AnnotationConfiguration conf = new AnnotationConfiguration();
//        conf.configure("/hibernate-thread.cfg.xml");
//        conf.getProperties().setProperty("hibernate.show_sql", "false");
//        conf.addAnnotatedClass(A.class);
//        conf.addAnnotatedClass(B.class);
//
//        SessionFactoryImplementor sf = null;
//
//        EntityMode entityMode = EntityMode.POJO;
//
//        // TODO what about other entity modes?
//
//        try
//        {
//            sf = (SessionFactoryImplementor)conf.buildSessionFactory();
//
//            A base = new A();
//
//            A modified = new A();
//            B b = new B();
//            modified.getBs().add(b);
//
//            HibernateReflections.applyMemoryDelta(sf, entityMode, base, modified);
//
//            assert new Integer(1).equals(base.getI());
//            assert "a".equals(base.getS());
//        }
//        finally
//        {
//            if (sf != null)
//            {
//                sf.close();
//            }
//        }
//    }
//
//    @Test(enabled = true) // TODO enable if I ever decide to pursue this path, which I doubt
//    public void testApplyChanges_Entities() throws Throwable
//    {
//        throw new RuntimeException("NOT YET IMPLEMENTED");
//    }

//    @Test(enabled = true) // TODO https://jira.novaordis.org/browse/HBA-32
//    public void testApplyChanges_Components() throws Throwable
//    {
//        throw new RuntimeException("NOT YET IMPLEMENTED");
//    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
