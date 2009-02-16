/**
 * Copyright (C) 2009 Krasimir Chobantonov <kchobantonov@yahoo.com>
 * This file is part of Hibernate Audit.

 * Hibernate Audit is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Hibernate Audit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with Hibernate Audit.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.googlecode.hibernate.audit.synchronization.work;

import java.io.Serializable;
import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.collection.PersistentCollection;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;

public class InsertCollectionAuditWorkUnit extends AbstractCollectionAuditWorkUnit {
    private String entityName;
    private Serializable id;
    private Object entity;
    private PersistentCollection persistentCollection;
    private AuditEvent auditEvent;
    private CollectionPersister collectionPersister;

    public InsertCollectionAuditWorkUnit(String entityName, Serializable id, Object entity, PersistentCollection persistentCollection) {
        this.entityName = entityName;
        this.id = id;
        this.entity = entity;
        this.persistentCollection = persistentCollection;
    }

    @Override
    protected String getEntityName() {
        return entityName;
    }

    public void perform(Session session, AuditConfiguration auditConfiguration, AuditTransaction auditTransaction) {
        initializeAuditEvents(session, auditConfiguration);
        if (auditEvent != null) {
            auditEvent.setAuditTransaction(auditTransaction);
            auditTransaction.getEvents().add(auditEvent);
            AuditLogicalGroup logicalGroup = getAuditLogicalGroup(session, auditConfiguration, auditEvent);

            auditEvent.setAuditLogicalGroup(logicalGroup);
        }
    }

    public void init(Session session, AuditConfiguration auditConfiguration) {
        collectionPersister = ((SessionImplementor) session).getPersistenceContext().getCollectionEntry(persistentCollection).getCurrentPersister();
    }

    private void initializeAuditEvents(Session session, AuditConfiguration auditConfiguration) {
        String role = collectionPersister.getCollectionMetadata().getRole();
        String propertyName = role.substring(role.lastIndexOf('.') != -1 ? role.lastIndexOf('.') + 1 : 0, role.length());

        if (!auditConfiguration.getExtensionManager().getAuditableInformationProvider().isAuditable(entityName, propertyName) || !persistentCollection.wasInitialized()) {
            return;
        }

        AuditType auditType = HibernateAudit.getAuditType(session, entity.getClass().getName());
        auditEvent = new AuditEvent();
        auditEvent.setAuditType(auditType);
        auditEvent.setType(AuditEvent.ADD_AUDIT_EVENT_TYPE);
        auditEvent.setEntityId(id == null ? null : id.toString());

        EntityAuditObject auditObject = new EntityAuditObject();
        auditObject.setAuditEvent(auditEvent);
        auditObject.setAuditType(auditType);
        auditObject.setTargetEntityId(id == null ? null : id.toString());
        auditEvent.getAuditObjects().add(auditObject);

        Type elementType = collectionPersister.getCollectionMetadata().getElementType();

        Iterator<? extends Object> iterator = persistentCollection.entries(collectionPersister);

        for (long i = 0; iterator.hasNext(); i++) {
            Object element = iterator.next();

            processElement(session, auditConfiguration, entity, element, elementType, propertyName, i, auditObject, auditEvent);
        }
    }
}
