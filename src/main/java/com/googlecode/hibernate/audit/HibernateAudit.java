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
package com.googlecode.hibernate.audit;

import org.hibernate.Query;
import org.hibernate.Session;

import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.clazz.AuditTypeField;

public final class HibernateAudit {
	public static final String AUDIT_CONFIGURATION_OBSERVER_PROPERTY = "hba.configuration.observer.clazz";
	public static final String AUDIT_CONCURRENT_MODIFICATION_CHECK_PROPERTY = "hba.concurrent.modification.check";

	private static final String SELECT_AUDIT_LOCAL_GROUP_BY_AUDIT_TYPE_AND_EXTERNAL_ID = "com.googlecode.hibernate.audit.HibernateAudit.getAuditLogicalGroup";
	private static final String SELECT_AUDIT_TYPE_BY_CLASS_NAME = "com.googlecode.hibernate.audit.HibernateAudit.getAuditType";
	private static final String SELECT_AUDIT_TYPE_FIELD_BY_CLASS_NAME_AND_PROPERTY_NAME = "com.googlecode.hibernate.audit.HibernateAudit.getAuditField";

	public static final String AUDIT_META_DATA_QUERY_CACHE_REGION = "com.googlecode.hibernate.audit.model.query";

	private HibernateAudit() {
	}

	public static AuditLogicalGroup getAuditLogicalGroup(Session session,
			AuditType auditType, String externalId) {
		Query query = session
				.getNamedQuery(SELECT_AUDIT_LOCAL_GROUP_BY_AUDIT_TYPE_AND_EXTERNAL_ID);

		query.setParameter("auditType", auditType);
		query.setParameter("externalId", externalId);
		AuditLogicalGroup storedAuditLogicalGroup = (AuditLogicalGroup) query
				.uniqueResult();
		return storedAuditLogicalGroup;
	}

	public static AuditType getAuditType(Session session, String className) {
		Query query = session.getNamedQuery(SELECT_AUDIT_TYPE_BY_CLASS_NAME);
		query.setParameter("className", className);
		
		query.setCacheable(true);
		query.setCacheRegion(AUDIT_META_DATA_QUERY_CACHE_REGION);
		
		AuditType auditType = (AuditType) query.uniqueResult();
		return auditType;
	}

	public static AuditTypeField getAuditField(Session session,
			String className, String propertyName) {
		Query query = session
				.getNamedQuery(SELECT_AUDIT_TYPE_FIELD_BY_CLASS_NAME_AND_PROPERTY_NAME);

		query.setParameter("className", className);
		query.setParameter("name", propertyName);
		
		query.setCacheable(true);
		query.setCacheRegion(AUDIT_META_DATA_QUERY_CACHE_REGION);
		
		AuditTypeField auditField = (AuditTypeField) query.uniqueResult();
		return auditField;
	}
}
