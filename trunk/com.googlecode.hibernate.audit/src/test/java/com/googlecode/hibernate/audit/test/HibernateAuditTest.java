package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.SessionFactory;
import com.googlecode.hibernate.audit.HibernateAudit;

/**
 * Tests the runtime API
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class HibernateAuditTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAuditTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testEnableDisable() throws Exception
    {
        assert !HibernateAudit.isEnabled();

        Configuration config = new AnnotationConfiguration();
        config.configure("/hibernate.cfg.xml");
        SessionFactory sf = config.buildSessionFactory();

        HibernateAudit.enable(sf);

        assert HibernateAudit.isEnabled();

        // testing noop behavior
        HibernateAudit.enable(sf);

        assert HibernateAudit.disable();

        // testing noop behavior
        assert !HibernateAudit.disable();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
