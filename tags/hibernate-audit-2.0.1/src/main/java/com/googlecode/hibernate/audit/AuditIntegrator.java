package com.googlecode.hibernate.audit;

import java.util.Map;

import org.hibernate.cfg.Configuration;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.DuplicationStrategy;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.integrator.spi.Integrator;
import org.hibernate.internal.CoreMessageLogger;
import org.hibernate.internal.util.config.ConfigurationHelper;
import org.hibernate.metamodel.source.MetadataImplementor;
import org.hibernate.service.spi.SessionFactoryServiceRegistry;
import org.jboss.logging.Logger;

import com.googlecode.hibernate.audit.listener.AuditListener;
import com.googlecode.hibernate.audit.listener.AuditSessionFactoryObserver;
import com.googlecode.hibernate.audit.util.ConcurrentReferenceHashMap;

public class AuditIntegrator implements Integrator {
	private static final CoreMessageLogger LOG = Logger.getMessageLogger(CoreMessageLogger.class,
			AuditIntegrator.class.getName());

	public static final String AUTO_REGISTER = "com.googlecode.hibernate.audit.listener.autoRegister";
	
    private static final Map<SessionFactoryImplementor, AuditListener> MAP = new ConcurrentReferenceHashMap<SessionFactoryImplementor, AuditListener>(16,
            ConcurrentReferenceHashMap.ReferenceType.WEAK, ConcurrentReferenceHashMap.ReferenceType.STRONG);

	public void integrate(Configuration configuration, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
		final boolean autoRegister = ConfigurationHelper.getBoolean(AUTO_REGISTER, configuration.getProperties(), true);
		if (!autoRegister) {
			LOG.debug("Skipping HibernateAudit listener auto registration");
			return;
		}
		
		final EventListenerRegistry listenerRegistry = serviceRegistry.getService( EventListenerRegistry.class );
		listenerRegistry.addDuplicationStrategy(new DuplicationStrategy() {
			public boolean areMatch(Object listener, Object original) {
				return listener.getClass().equals( original.getClass() ) && AuditListener.class.isInstance( listener );
			}
			
			public Action getAction() {
				return Action.KEEP_ORIGINAL;
			}
		});
		
		AuditListener auditListener = MAP.get(sessionFactory);
		
		if (auditListener == null) {
			auditListener = new AuditListener();
			MAP.put(sessionFactory, auditListener);
		}
		
		listenerRegistry.appendListeners(EventType.POST_INSERT, auditListener);
		listenerRegistry.appendListeners(EventType.POST_UPDATE, auditListener);
		listenerRegistry.appendListeners(EventType.POST_DELETE, auditListener);
		listenerRegistry.appendListeners(EventType.POST_COLLECTION_RECREATE, auditListener);
		listenerRegistry.appendListeners(EventType.PRE_COLLECTION_UPDATE, auditListener);
		listenerRegistry.appendListeners(EventType.PRE_COLLECTION_REMOVE, auditListener);
		
		auditListener.initialize(configuration);
		
		sessionFactory.addObserver(new AuditSessionFactoryObserver(AuditListener.getAuditConfiguration(configuration), configuration));
	}
	
     public void disintegrate(SessionFactoryImplementor sessionFactory, SessionFactoryServiceRegistry serviceRegistry) {
    	 AuditListener auditListener = MAP.remove(sessionFactory);
    	 if (auditListener != null) {
    		 auditListener.cleanup();
    	 }
     }
	 
	 public void integrate(MetadataImplementor metadata, SessionFactoryImplementor sessionFactory,
			SessionFactoryServiceRegistry serviceRegistry) {
	}
}
