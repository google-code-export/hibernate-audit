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