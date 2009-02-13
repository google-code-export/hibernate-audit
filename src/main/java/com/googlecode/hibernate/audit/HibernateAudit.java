package com.googlecode.hibernate.audit;

import org.hibernate.Query;
import org.hibernate.Session;

import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;

public final class HibernateAudit {
	public static final String AUDIT_ENTITY_QUERY_CACHE_REGION = "com.googlecode.hibernate.audit.model.query";

	public static final String AUDIT_LOGICAL_GROUP_QUERY_CACHE_REGION = "com.googlecode.hibernate.audit.model.AuditLogicalGroup.query";

	public static final String AUDIT_CONFIGURATION_OBSERVER_PROPERTY = "hba.configuration.observer.clazz";
	public static final String AUDIT_CONCURRENT_MODIFICATION_CHECK_PROPERTY = "hba.concurrent.modification.check";

	private HibernateAudit() {
	}

	public static AuditLogicalGroup getAuditLogicalGroup(Session session,
			AuditType auditType, String externalId) {
		Query getAuditLogicalGroupQuery = session.createQuery("from "
				+ AuditLogicalGroup.class.getName()
				+ " where auditType = :auditType and externalId = :externalId");
		getAuditLogicalGroupQuery.setParameter("auditType", auditType);
		getAuditLogicalGroupQuery.setParameter("externalId", externalId);
		getAuditLogicalGroupQuery.setCacheable(true);
		getAuditLogicalGroupQuery
				.setCacheRegion(AUDIT_LOGICAL_GROUP_QUERY_CACHE_REGION);
		AuditLogicalGroup storedAuditLogicalGroup = (AuditLogicalGroup) getAuditLogicalGroupQuery
				.uniqueResult();

		if (storedAuditLogicalGroup == null) {
			session.getSessionFactory().evictQueries(
					AUDIT_LOGICAL_GROUP_QUERY_CACHE_REGION);
		}
		return storedAuditLogicalGroup;
	}

	public static AuditType getAuditType(Session session, String className) {
		Query query = session.createQuery("from " + AuditType.class.getName()
				+ " where className = :className");
		query.setParameter("className", className);
		query.setCacheable(true);
		query.setCacheRegion(HibernateAudit.AUDIT_ENTITY_QUERY_CACHE_REGION);
		AuditType auditType = (AuditType) query.uniqueResult();
		return auditType;
	}

	public static AuditTypeField getAuditField(Session session,
			String className, String propertyName) {
		Query query = session.createQuery("from "
				+ AuditTypeField.class.getName()
				+ " where ownerType.className = :className and name = :name");
		query.setParameter("className", className);
		query.setParameter("name", propertyName);
		query.setCacheable(true);
		query.setCacheRegion(HibernateAudit.AUDIT_ENTITY_QUERY_CACHE_REGION);
		AuditTypeField auditField = (AuditTypeField) query.uniqueResult();
		return auditField;
	}
}
