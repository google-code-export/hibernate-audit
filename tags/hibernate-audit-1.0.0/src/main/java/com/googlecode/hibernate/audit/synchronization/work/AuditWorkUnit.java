/**
 * 
 */
package com.googlecode.hibernate.audit.synchronization.work;

import java.util.List;

import org.hibernate.Session;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditLogicalGroup;
import com.googlecode.hibernate.audit.model.AuditTransaction;

public interface AuditWorkUnit {
	void init(Session session, AuditConfiguration auditConfiguration);

	void perform(Session session, AuditConfiguration auditConfiguration,
			AuditTransaction auditTransaction);

	List<AuditLogicalGroup> getAuditLogicalGroups();
}
