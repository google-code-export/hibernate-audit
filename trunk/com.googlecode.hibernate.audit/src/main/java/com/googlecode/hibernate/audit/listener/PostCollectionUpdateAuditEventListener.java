package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.PostCollectionUpdateEventListener;
import org.hibernate.event.PostCollectionUpdateEvent;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.CollectionType;
import org.hibernate.type.Type;
import org.hibernate.collection.PersistentCollection;
import org.apache.log4j.Logger;
import com.googlecode.hibernate.audit.model.Manager;
import com.googlecode.hibernate.audit.model.AuditTypeField;
import com.googlecode.hibernate.audit.model.AuditEventCollectionPair;
import com.googlecode.hibernate.audit.model.AuditCollectionType;
import com.googlecode.hibernate.audit.util.Hibernate;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;
import java.io.Serializable;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class PostCollectionUpdateAuditEventListener
    extends AbstractAuditEventListener implements PostCollectionUpdateEventListener
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(PostCollectionUpdateAuditEventListener.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    public PostCollectionUpdateAuditEventListener(Manager m)
    {
        super(m);
    }

    // PostInsertEventListener implementation ------------------------------------------------------

    public void onPostUpdateCollection(PostCollectionUpdateEvent event)
    {
        log.debug(this + ".onPostUpdateCollection(...)");

        EntityEventContext ec = createAndLogEntityEventContext(event);
        PersistentCollection c = event.getCollection();
        String role = c.getRole();
        CollectionPersister cp = ec.factory.getCollectionPersister(role);
        CollectionType ct = cp.getCollectionType();
        Class cc = Hibernate.collectionTypeToClass(ct);
        Type et = ct.getElementType(ec.factory);
        Class elemc = et.getReturnedClass();
        AuditCollectionType ft = (AuditCollectionType)ec.auditTransaction.getAuditType(cc, elemc);
        String fn = Hibernate.roleToVariableName(role);
        AuditTypeField f = ec.auditTransaction.getAuditTypeField(fn, ft);

        AuditEventCollectionPair pair = new AuditEventCollectionPair();
        pair.setEvent(ec.auditEvent);
        pair.setField(f);

        List<Long> ids = new ArrayList<Long>();
        for(Iterator i = c.entries(cp); i.hasNext(); )
        {
            Object entry = i.next();
            Long id = (Long)ec.session.getIdentifier(entry);
            ids.add(id);
        }

        pair.setIds(ids);

        ec.auditTransaction.log(pair);
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "PostCollectionUpdateAuditEventListener[" +
               Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
