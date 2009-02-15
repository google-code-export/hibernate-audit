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
import java.util.ArrayList;
import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.AuditObject;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.property.ComponentObjectProperty;
import com.googlecode.hibernate.audit.model.property.EntityObjectProperty;
import com.googlecode.hibernate.audit.model.property.SimpleObjectProperty;

public abstract class AbstractAuditWorkUnit implements AuditWorkUnit {
	protected abstract String getEntityName();

	private List<AuditLogicalGroup> auditLogicalGroups = new ArrayList<AuditLogicalGroup>();

	protected void processProperty(Session session,
			AuditConfiguration auditConfiguration, AuditEvent auditEvent,
			Object object, String propertyName, Object propertyValue,
			Type propertyType, AuditObject auditObject) {
		if (!auditConfiguration.getExtensionManager()
				.getAuditableInformationProvider().isAuditable(getEntityName(),
						propertyName)) {
			return;
		}
		if (propertyType.isEntityType()) {
			processEntityProperty(session, auditConfiguration, object,
					propertyName, propertyValue, propertyType, auditObject);
		} else if (propertyType.isCollectionType()) {
			// collection will be handled by collection event listeners
		} else if (propertyType.isComponentType()) {
			processComponentValue(session, auditConfiguration, auditEvent,
					auditObject, getEntityName(), object, propertyName,
					propertyValue, (AbstractComponentType) propertyType);
		} else {
			createSimpleValue(session, auditConfiguration, auditObject,
					getEntityName(), object, propertyName, propertyValue);
		}
	}

	protected void processEntityProperty(Session session,
			AuditConfiguration auditConfiguration, Object object,
			String propertyName, Object propertyValue, Type propertyType,
			AuditObject auditObject) {
		String entityName = ((EntityType) propertyType)
				.getAssociatedEntityName();

		Serializable id = null;

		if (propertyValue != null) {
			id = session.getSessionFactory().getClassMetadata(entityName)
					.getIdentifier(propertyValue, session.getEntityMode());
		}
		AuditTypeField auditField = HibernateAudit.getAuditField(session,
				object.getClass().getName(), propertyName);

		EntityObjectProperty property = new EntityObjectProperty();
		property.setAuditObject(auditObject);
		property.setAuditField(auditField);
		property.setIndex(null);
		property.setTargetEntityId(auditConfiguration.getExtensionManager()
				.getPropertyValueConverter().toString(id));
		auditObject.getAuditObjectProperties().add(property);
	}

	protected void processComponentValue(Session session,
			AuditConfiguration auditConfiguration, AuditEvent auditEvent,
			AuditObject auditObject, String entityName, Object entity,
			String propertyName, Object component,
			AbstractComponentType componentType) {
		AuditTypeField auditField = HibernateAudit.getAuditField(session,
				entity.getClass().getName(), propertyName);

		ComponentObjectProperty property = new ComponentObjectProperty();
		property.setAuditObject(auditObject);
		property.setAuditField(auditField);
		property.setIndex(null);

		ComponentAuditObject targetComponentAuditObject = null;

		if (component != null) {
			targetComponentAuditObject = new ComponentAuditObject();
			targetComponentAuditObject.setAuditEvent(auditEvent);
			targetComponentAuditObject.setParentAuditObject(auditObject);
			AuditType auditType = HibernateAudit.getAuditType(session,
					component.getClass().getName());
			targetComponentAuditObject.setAuditType(auditType);

			for (int i = 0; i < componentType.getPropertyNames().length; i++) {
				String componentPropertyName = componentType.getPropertyNames()[i];

				Type componentPropertyType = componentType.getSubtypes()[i];
				Object componentPropertyValue = componentType.getPropertyValue(
						component, i, (SessionImplementor) session);

				processProperty(session, auditConfiguration, auditEvent,
						component, componentPropertyName,
						componentPropertyValue, componentPropertyType,
						targetComponentAuditObject);
			}
		}
		property.setTargetComponentAuditObject(targetComponentAuditObject);

		auditObject.getAuditObjectProperties().add(property);
	}

	protected void createSimpleValue(Session session,
			AuditConfiguration auditConfiguration, AuditObject auditObject,
			String entityName, Object entity, String propertyName,
			Object propertyValue) {
		AuditTypeField auditField = HibernateAudit.getAuditField(session,
				entity.getClass().getName(), propertyName);

		SimpleObjectProperty property = new SimpleObjectProperty();
		property.setAuditObject(auditObject);
		property.setAuditField(auditField);
		property.setIndex(null);
		property.setValue(auditConfiguration.getExtensionManager()
				.getPropertyValueConverter().toString(propertyValue));
		auditObject.getAuditObjectProperties().add(property);
	}

	public List<AuditLogicalGroup> getAuditLogicalGroups() {
		return auditLogicalGroups;
	}

	protected AuditLogicalGroup getAuditLogicalGroup(Session session,
			AuditConfiguration auditConfiguration, AuditEvent auditEvent) {

		AuditLogicalGroup logicalGroup = auditConfiguration
				.getExtensionManager().getAuditLogicalGroupProvider()
				.getAuditLogicalGroup(session, auditEvent);

		AuditLogicalGroup result = null;

		if (logicalGroup != null) {
			AuditType auditType = HibernateAudit.getAuditType(session,
					logicalGroup.getAuditType().getClassName());
			String externalId = logicalGroup.getExternalId();

			result = HibernateAudit.getAuditLogicalGroup(session, auditType, externalId);

			if (result == null) {
				createAduitLogicalGroup(session, logicalGroup, auditType);
				result = HibernateAudit.getAuditLogicalGroup(session, auditType, externalId);

				auditLogicalGroups.add(result);
			}

		}

		return result;
	}

	private void createAduitLogicalGroup(Session session,
			AuditLogicalGroup logicalGroup, AuditType auditType) {
		try {
			Session newSession = null;

			try {
				newSession = session.getSessionFactory().openSession();
				Transaction tx = newSession.beginTransaction();
				logicalGroup.setAuditType(auditType);
				newSession.save(logicalGroup);
				tx.commit();
			} finally {
				if (newSession != null) {
					newSession.close();
				}
			}
		} catch (HibernateException e) {
			// ignore any database generated exceptions because of concurrent
			// add.
		}
	}

}
