package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;

import java.util.Set;

import com.googlecode.hibernate.audit.model.Entities;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.model.AuditEntityType;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.AuditEventPair;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.AuditTypeField;

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
public class EntitiesTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(EntitiesTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testGetAuditEntities() throws Exception
    {
        Set<Class> entities = Entities.getAuditEntities();

        assert entities.size() == 8;

        assert entities.contains(AuditCollectionType.class);
        assert entities.contains(AuditEntityType.class);
        assert entities.contains(AuditEvent.class);
        assert entities.contains(AuditEventCollectionPair.class);
        assert entities.contains(AuditEventPair.class);
        assert entities.contains(AuditTransaction.class);
        assert entities.contains(AuditType.class);
        assert entities.contains(AuditTypeField.class);

        log.debug("done");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
