package com.googlecode.hibernate.audit.test;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.listener.Listeners;

import java.util.Set;

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
public class ListenersTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(ListenersTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testGetAuditedEventTypes() throws Exception
    {
        Set<String> types = Listeners.getAuditedEventTypes();

        assert types.size() == 8;
        assert types.contains("post-insert");
        assert types.contains("save-update");
        assert types.contains("save");
        assert types.contains("post-delete");
        assert types.contains("flush");
        assert types.contains("flush-entity");
        assert types.contains("post-update");
        assert types.contains("update");
        assert !types.contains("abstract");

        log.debug("done");
    }

    @Test(enabled = true)
    public void testGetEventListenersGetterName()
    {
        assert "getMergeEventListeners".equals(Listeners.getEventListenersGetterName("merge"));
        assert "getPostInsertEventListeners".
            equals(Listeners.getEventListenersGetterName("post-insert"));
        assert "getPreCollectionRecreateEventListeners".
            equals(Listeners.getEventListenersGetterName("pre-collection-recreate"));
        assert "getPersistOnFlushEventListeners".
            equals(Listeners.getEventListenersGetterName("create-onflush"));
        assert "getSaveOrUpdateEventListeners".
            equals(Listeners.getEventListenersGetterName("save-update"));
        assert "getPersistEventListeners".
            equals(Listeners.getEventListenersGetterName("create"));
        assert "getInitializeCollectionEventListeners".
            equals(Listeners.getEventListenersGetterName("load-collection"));
    }

    @Test(enabled = true)
    public void testSetEventListenersGetterName()
    {
        assert "setMergeEventListeners".equals(Listeners.getEventListenersSetterName("merge"));
        assert "setPostInsertEventListeners".
            equals(Listeners.getEventListenersSetterName("post-insert"));
        assert "setPreCollectionRecreateEventListeners".
            equals(Listeners.getEventListenersSetterName("pre-collection-recreate"));
        assert "setPersistOnFlushEventListeners".
            equals(Listeners.getEventListenersSetterName("create-onflush"));
        assert "setSaveOrUpdateEventListeners".
            equals(Listeners.getEventListenersSetterName("save-update"));
        assert "setPersistEventListeners".
            equals(Listeners.getEventListenersSetterName("create"));
        assert "setInitializeCollectionEventListeners".
            equals(Listeners.getEventListenersSetterName("load-collection"));
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
