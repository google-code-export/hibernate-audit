package com.googlecode.hibernate.audit.test.model;

import org.testng.annotations.Test;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.cfg.AnnotationConfiguration;
import com.googlecode.hibernate.audit.test.base.JTATransactionTest;
import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.collision.WriteCollisionDetector;

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
public class AuditSettingsFactoryTest extends JTATransactionTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Primitive Updates ---------------------------------------------------------------------------

    @Test(enabled = true)
    public void testWriteCollisionDetectionEnabledConfiguration() throws Exception
    {
        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure(getHibernateConfigurationFileName());
        SessionFactoryImplementor sf = null;

        try
        {
            sf = (SessionFactoryImplementor)config.buildSessionFactory();

            HibernateAudit.startRuntime(sf.getSettings());

            WriteCollisionDetector wcd = HibernateAudit.getManager().getWriteCollisionDetector();

            assert !wcd.isWriteCollisionDetectionEnabled();

            // try to set the system property without bouncing the engine

            System.setProperty("hba.write.collision.detection.enable", "true");

            assert !wcd.isWriteCollisionDetectionEnabled();

            // now bounce the engine
            HibernateAudit.stopRuntime();
            HibernateAudit.startRuntime(sf.getSettings());

            wcd = HibernateAudit.getManager().getWriteCollisionDetector();

            assert wcd.isWriteCollisionDetectionEnabled();
        }
        finally
        {
            HibernateAudit.stopRuntime();

            System.clearProperty("hba.write.collision.detection.enable");

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