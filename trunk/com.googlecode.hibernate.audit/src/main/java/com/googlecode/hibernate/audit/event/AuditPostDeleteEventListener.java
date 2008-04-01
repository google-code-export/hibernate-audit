package com.googlecode.hibernate.audit.event;

import org.hibernate.StatelessSession;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.googlecode.hibernate.audit.model.AuditOperation;

public class AuditPostDeleteEventListener extends AuditAbstractEventListener {


	@Override
	protected Object getEntity(Object object) {
		PostDeleteEvent event = (PostDeleteEvent)object;
		return event.getEntity();
	}

	@Override
	protected EntityPersister getEntityPersister(Object object) {
		PostDeleteEvent event = (PostDeleteEvent)object;
		return event.getPersister();
	}

	@Override
	protected StatelessSession openStatelessSession(Object object) {
		PostDeleteEvent event = (PostDeleteEvent)object;
		return event.getPersister().getFactory().openStatelessSession(event.getSession().connection());
	}
	
	@Override
	protected AuditOperation getAuditEntityOperation(Object event) {
		return AuditOperation.DELETE;
	}
}
