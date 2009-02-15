/**
 * Copyright (C) 2009 Krasimir Chobantonov <kchobantonov@yahoo.com>
 * This file is part of GNU Hibernate Audit.

 * GNU Hibernate Audit is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * GNU Hibernate Audit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with GNU Hibernate Audit.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.googlecode.hibernate.audit.configuration;

import org.hibernate.cfg.Configuration;

import com.googlecode.hibernate.audit.HibernateAudit;
import com.googlecode.hibernate.audit.extension.ExtensionManager;
import com.googlecode.hibernate.audit.synchronization.AuditSynchronizationManager;

public class AuditConfiguration {
	private AuditSynchronizationManager auditSynchronizationManager = new AuditSynchronizationManager(
			this);
	private Configuration configuration;
	private ExtensionManager extensionManager = new ExtensionManager();
	private boolean concurrentModificationCheckEnabled = false;

	public AuditConfiguration(Configuration configuration) {
		this.configuration = configuration;
		String concurrentProperty = configuration
				.getProperty(HibernateAudit.AUDIT_CONCURRENT_MODIFICATION_CHECK_PROPERTY);
		if (concurrentProperty != null) {
			concurrentModificationCheckEnabled = Boolean.valueOf(
					concurrentProperty).booleanValue();
		}
	}

	public Configuration getAuditedConfiguration() {
		return configuration;
	}

	public AuditSynchronizationManager getAuditSynchronizationManager() {
		return auditSynchronizationManager;
	}

	public ExtensionManager getExtensionManager() {
		return extensionManager;
	}

	public boolean isConcurrentModificationCheckEnabled() {
		return concurrentModificationCheckEnabled;
	}
}