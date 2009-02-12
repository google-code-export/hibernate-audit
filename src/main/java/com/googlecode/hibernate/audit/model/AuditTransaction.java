/**
 * <copyright>
 * </copyright>
 *
 * $Id$
 */
package com.googlecode.hibernate.audit.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AuditTransaction {

	protected Long id;
	protected Date timestamp;
	protected String username;
	protected List<AuditEvent> events;
	protected List<AuditTransactionAttribute> auditTransactionAttributes;

	public List<AuditEvent> getEvents() {
		if (events == null) {
			events = new ArrayList<AuditEvent>();
		}
		return events;
	}

	public void setEvents(List<AuditEvent> events) {
		this.events = events;
	}

	public List<AuditTransactionAttribute> getAuditTransactionAttributes() {
		if (auditTransactionAttributes == null) {
			auditTransactionAttributes = new ArrayList<AuditTransactionAttribute>();
		}
		return auditTransactionAttributes;
	}

	public void setAuditTransactionAttributes(
			List<AuditTransactionAttribute> auditTransactionAttributes) {
		this.auditTransactionAttributes = auditTransactionAttributes;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long newId) {
		id = newId;
	}

	public Date getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(Date newTimestamp) {
		timestamp = newTimestamp;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String newUsername) {
		username = newUsername;
	}

}