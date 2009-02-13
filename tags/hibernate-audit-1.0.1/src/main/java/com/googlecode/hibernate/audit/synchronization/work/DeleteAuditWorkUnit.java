package com.googlecode.hibernate.audit.synchronization.work;

import java.io.Serializable;

import org.hibernate.Session;
import org.hibernate.persister.entity.EntityPersister;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;

public class DeleteAuditWorkUnit extends AbstractAuditWorkUnit {
	private String entityName;
	private Serializable id;
	private EntityPersister entityPersister;
	private Object entity;
	private AuditEvent auditEvent;

	public DeleteAuditWorkUnit(String entityName, Serializable id,
			Object entity, EntityPersister entityPersister) {

		this.entityName = entityName;
		this.id = id;
		this.entity = entity;
		this.entityPersister = entityPersister;
	}

	@Override
	protected String getEntityName() {
		return entityName;
	}

	public void perform(Session session, AuditConfiguration auditConfiguration,
			AuditTransaction auditTransaction) {
		initializeAuditEvents(session, auditConfiguration);

		if (auditEvent != null) {
			auditEvent.setAuditTransaction(auditTransaction);
			auditTransaction.getEvents().add(auditEvent);
			AuditLogicalGroup logicalGroup = getAuditLogicalGroup(session, auditConfiguration, auditEvent);

			auditEvent.setAuditLogicalGroup(logicalGroup);
		}
	}

	public void init(Session session, AuditConfiguration auditConfiguration) {

	}

	private void initializeAuditEvents(Session session,
			AuditConfiguration auditConfiguration) {
		AuditType auditType = HibernateAudit.getAuditType(session, entity
				.getClass().getName());
		auditEvent = new AuditEvent();
		auditEvent.setAuditType(auditType);
		auditEvent.setType(AuditEvent.DELETE_AUDIT_EVENT_TYPE);
		auditEvent.setEntityId(id == null ? null : id.toString());

		EntityAuditObject auditObject = new EntityAuditObject();
		auditObject.setAuditEvent(auditEvent);
		auditObject.setAuditType(auditType);
		auditObject.setTargetEntityId(id == null ? null : id.toString());
		auditEvent.getAuditObjects().add(auditObject);
	}

}
