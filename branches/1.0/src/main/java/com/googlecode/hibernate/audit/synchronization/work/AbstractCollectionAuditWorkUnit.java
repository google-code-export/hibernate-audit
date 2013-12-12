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
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;
import com.googlecode.hibernate.audit.model.property.ComponentObjectProperty;
import com.googlecode.hibernate.audit.model.property.EntityObjectProperty;
import com.googlecode.hibernate.audit.model.property.SimpleObjectProperty;

public abstract class AbstractCollectionAuditWorkUnit extends AbstractAuditWorkUnit {

    protected void processElement(Session session, AuditConfiguration auditConfiguration, Object entityOwner, Object element, Type elementType, String propertyName, long index,
            EntityAuditObject auditObject, AuditEvent auditEvent) {
    	
    	String entityName = session.getEntityName(entityOwner);
    	
        AuditTypeField auditField = HibernateAudit.getAuditField(session, auditConfiguration.getExtensionManager().getAuditableInformationProvider().getAuditTypeClassName(auditConfiguration.getAuditedConfiguration(), entityName), propertyName);
        AuditObjectProperty property = null;
        
        if (elementType.isEntityType()) {
            Serializable id = null;

            if (element != null) {
                id = session.getSessionFactory().getClassMetadata(((EntityType) elementType).getAssociatedEntityName()).getIdentifier(element, session.getEntityMode());
            }

            property = new EntityObjectProperty();
            property.setAuditObject(auditObject);
            property.setAuditField(auditField);
            property.setIndex(new Long(index));
            ((EntityObjectProperty)property).setTargetEntityId(auditConfiguration.getExtensionManager().getPropertyValueConverter().toString(id));
        } else if (elementType.isComponentType()) {
        	CompositeType componentType = (CompositeType) elementType;

            property = new ComponentObjectProperty();
            property.setAuditObject(auditObject);
            property.setAuditField(auditField);
            property.setIndex(new Long(index));
            ComponentAuditObject targetComponentAuditObject = null;

            if (element != null) {
                targetComponentAuditObject = new ComponentAuditObject();
                targetComponentAuditObject.setAuditEvent(auditEvent);
                targetComponentAuditObject.setParentAuditObject(auditObject);
                AuditType auditComponentType = HibernateAudit.getAuditType(session, auditConfiguration.getExtensionManager().getAuditableInformationProvider().getAuditTypeClassName(auditConfiguration.getAuditedConfiguration(), elementType));
                targetComponentAuditObject.setAuditType(auditComponentType);

                for (int j = 0; j < componentType.getPropertyNames().length; j++) {
                    String componentPropertyName = componentType.getPropertyNames()[j];

                    Type componentPropertyType = componentType.getSubtypes()[j];
                    Object componentPropertyValue = componentType.getPropertyValue(element, j, (SessionImplementor) session);

                    processProperty(session, auditConfiguration, auditEvent, element, componentPropertyName, componentPropertyValue, componentPropertyType, targetComponentAuditObject);
                }
            }
            ((ComponentObjectProperty)property).setTargetComponentAuditObject(targetComponentAuditObject);
        } else if (elementType.isCollectionType()) {
            // collection of collections
        } else {

            property = new SimpleObjectProperty();
            property.setAuditObject(auditObject);
            property.setAuditField(auditField);
            property.setIndex(new Long(index));
            ((SimpleObjectProperty)property).setValue(auditConfiguration.getExtensionManager().getPropertyValueConverter().toString(element));
        }
        
        if (property != null) {
            AuditType auditType = null;
            if (element != null) {
                auditType = HibernateAudit.getAuditType(session, element.getClass().getName());
                if (auditType == null) {
                    // subclass that was not registered in the audit metadata - use the base class
                    auditType = property.getAuditField().getFieldType();
                }
            }

            property.setAuditType(auditType);
            auditObject.getAuditObjectProperties().add(property);
        }
    }

}
