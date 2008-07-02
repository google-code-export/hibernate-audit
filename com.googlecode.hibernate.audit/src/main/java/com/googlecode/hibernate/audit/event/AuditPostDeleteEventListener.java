package com.googlecode.hibernate.audit.event;

import org.apache.log4j.Logger;
import org.hibernate.StatelessSession;
import org.hibernate.event.AbstractEvent;
import org.hibernate.event.PostDeleteEvent;
import org.hibernate.persister.entity.EntityPersister;

import com.googlecode.hibernate.audit.model_obsolete.AuditOperation;

@SuppressWarnings("serial")
public class AuditPostDeleteEventListener extends AuditAbstractEventListener {
	private Logger LOG = Logger
			.getLogger(AuditPostDeleteEventListener.class);

	@Override
	protected Object getEntity(AbstractEvent object) {
		PostDeleteEvent event = (PostDeleteEvent) object;
		return event.getEntity();
	}

	@Override
	protected EntityPersister getEntityPersister(AbstractEvent object) {
		PostDeleteEvent event = (PostDeleteEvent) object;
		return event.getPersister();
	}

	@Override
	protected StatelessSession openStatelessSession(AbstractEvent object) {
		PostDeleteEvent event = (PostDeleteEvent) object;
		return event.getPersister().getFactory().openStatelessSession(
				event.getSession().connection());
	}

	@Override
	protected AuditOperation getAuditEntityOperation(AbstractEvent event) {
		return AuditOperation.DELETE;
	}
}
