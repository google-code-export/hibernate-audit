/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.configuration;

import org.hibernate.cfg.Configuration;

import com.googlecode.hibernate.audit.extension.ExtensionManager;
import com.googlecode.hibernate.audit.synchronization.AuditSynchronizationManager;

public class AuditConfiguration {
	private AuditSynchronizationManager auditSynchronizationManager = new AuditSynchronizationManager(
			this);
	private Configuration configuration;
	private ExtensionManager extensionManager = new ExtensionManager();

	public AuditConfiguration(Configuration configuration) {
		this.configuration = configuration;
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
}