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

import org.hibernate.Session;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.object.AuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;

public class InsertAuditWorkUnit extends AbstractAuditWorkUnit {
    private String entityName;
    private Serializable id;
    private EntityPersister entityPersister;
    private Object entity;
    private AuditEvent auditEvent;

    public InsertAuditWorkUnit(String entityName, Serializable id, Object entity, EntityPersister entityPersister) {

        this.entityName = entityName;
        this.id = id;
        this.entity = entity;
        this.entityPersister = entityPersister;
    }

    @Override
    protected String getEntityName() {
        return entityName;
    }

    public void perform(Session session, AuditConfiguration auditConfiguration, AuditTransaction auditTransaction) {
        initializerAuditEvents(session, auditConfiguration);

        if (auditEvent != null) {
            auditEvent.setAuditTransaction(auditTransaction);
            auditTransaction.getEvents().add(auditEvent);
            AuditLogicalGroup logicalGroup = getAuditLogicalGroup(session, auditConfiguration, auditEvent);

            auditEvent.setAuditLogicalGroup(logicalGroup);
        }
    }

    public void init(Session session, AuditConfiguration auditConfiguration) {

    }

    private void initializerAuditEvents(Session session, AuditConfiguration auditConfiguration) {
        AuditType auditType = HibernateAudit.getAuditType(session, entity.getClass().getName());
        auditEvent = new AuditEvent();
        auditEvent.setAuditType(auditType);
        auditEvent.setType(AuditEvent.INSERT_AUDIT_EVENT_TYPE);
        auditEvent.setEntityId(id == null ? null : id.toString());

        EntityAuditObject auditObject = new EntityAuditObject();
        auditObject.setAuditEvent(auditEvent);
        auditObject.setAuditType(auditType);
        auditObject.setTargetEntityId(id == null ? null : id.toString());
        auditEvent.getAuditObjects().add(auditObject);

        processProperties(session, auditConfiguration, auditEvent, entityPersister, entity, auditObject);
    }

    private void processProperties(Session session, AuditConfiguration auditConfiguration, AuditEvent auditEvent, EntityPersister persister, Object entity, AuditObject auditObject) {

        for (String propertyName : persister.getPropertyNames()) {
            Type propertyType = persister.getPropertyType(propertyName);
            Object propertyValue = persister.getPropertyValue(entity, propertyName, persister.guessEntityMode(entity));

            processProperty(session, auditConfiguration, auditEvent, entity, propertyName, propertyValue, propertyType, auditObject);
        }
    }

    @Override
    protected void processEntityProperty(Session session, AuditConfiguration auditConfiguration, Object object, String propertyName, Object propertyValue, Type propertyType, AuditObject auditObject) {
        if (propertyValue != null) {
            // only record not null values
            super.processEntityProperty(session, auditConfiguration, object, propertyName, propertyValue, propertyType, auditObject);
        }
    }

    @Override
    protected void processComponentValue(Session session, AuditConfiguration auditConfiguration, AuditEvent auditEvent, AuditObject auditObject, String entityName, Object entity, String propertyName,
            Object component, AbstractComponentType componentType) {
        if (component != null) {
            // only record not null values
            super.processComponentValue(session, auditConfiguration, auditEvent, auditObject, entityName, entity, propertyName, component, componentType);
        }
    }

    @Override
    protected void createSimpleValue(Session session, AuditConfiguration auditConfiguration, AuditObject auditObject, String entityName, Object entity, String propertyName, Object propertyValue) {
        if (propertyValue != null) {
            // only record not null values
            super.createSimpleValue(session, auditConfiguration, auditObject, entityName, entity, propertyName, propertyValue);
        }
    }
}
