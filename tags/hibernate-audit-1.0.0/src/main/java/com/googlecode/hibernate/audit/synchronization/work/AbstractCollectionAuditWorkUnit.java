package com.googlecode.hibernate.audit.synchronization.work;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.type.AbstractComponentType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;
import com.googlecode.hibernate.audit.model.object.ComponentAuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;
import com.googlecode.hibernate.audit.model.property.ComponentObjectProperty;
import com.googlecode.hibernate.audit.model.property.EntityObjectProperty;
import com.googlecode.hibernate.audit.model.property.SimpleObjectProperty;

public abstract class AbstractCollectionAuditWorkUnit extends
		AbstractAuditWorkUnit {
	
	protected void processElement(Session session,
			AuditConfiguration auditConfiguration, Object entityOwner, Object element,
			Type elementType, String propertyName, long index,
			EntityAuditObject auditObject, AuditEvent auditEvent) {

		if (elementType.isEntityType()) {
			Serializable id = null;
			
			if (element != null) {
				id = session.getSessionFactory().getClassMetadata(
						((EntityType) elementType).getAssociatedEntityName())
						.getIdentifier(element, session.getEntityMode());
			}

			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entityOwner.getClass().getName(), propertyName);

			EntityObjectProperty property = new EntityObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			property.setTargetEntityId(auditConfiguration.getExtensionManager()
					.getPropertyValueConverter().toString(id));
			auditObject.getAuditObjectProperties().add(property);
		} else if (elementType.isComponentType()) {
			AbstractComponentType componentType = (AbstractComponentType) elementType;

			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entityOwner.getClass().getName(), propertyName);

			ComponentObjectProperty property = new ComponentObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			ComponentAuditObject targetComponentAuditObject = null;

			if (element != null) {
				targetComponentAuditObject = new ComponentAuditObject();
				targetComponentAuditObject.setAuditEvent(auditEvent);
				targetComponentAuditObject.setParentAuditObject(auditObject);
				AuditType auditComponentType = HibernateAudit.getAuditType(
						session, element.getClass().getName());
				targetComponentAuditObject.setAuditType(auditComponentType);

				for (int j = 0; j < componentType.getPropertyNames().length; j++) {
					String componentPropertyName = componentType
							.getPropertyNames()[j];

					Type componentPropertyType = componentType.getSubtypes()[j];
					Object componentPropertyValue = componentType
							.getPropertyValue(element, j,
									(SessionImplementor) session);

					processProperty(session, auditConfiguration, auditEvent,
							element, componentPropertyName,
							componentPropertyValue, componentPropertyType,
							targetComponentAuditObject);
				}
			}
			property.setTargetComponentAuditObject(targetComponentAuditObject);
			auditObject.getAuditObjectProperties().add(property);
		} else if (elementType.isCollectionType()) {
			// collection of collections
		} else {
			AuditTypeField auditField = HibernateAudit.getAuditField(session,
					entityOwner.getClass().getName(), propertyName);

			SimpleObjectProperty property = new SimpleObjectProperty();
			property.setAuditObject(auditObject);
			property.setAuditField(auditField);
			property.setIndex(new Long(index));
			property.setValue(auditConfiguration.getExtensionManager()
					.getPropertyValueConverter().toString(element));
			auditObject.getAuditObjectProperties().add(property);
		}
	}

}
