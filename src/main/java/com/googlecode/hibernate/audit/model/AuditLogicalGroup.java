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
package com.googlecode.hibernate.audit.model;

import java.io.Serializable;

import com.googlecode.hibernate.audit.model.clazz.AuditType;

public class AuditLogicalGroup implements Serializable {
    protected Long id;
    protected AuditType auditType;
    protected String externalId;
    protected Long lastUpdatedAuditTransactionId;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public AuditType getAuditType() {
        return auditType;
    }

    public void setAuditType(AuditType auditType) {
        this.auditType = auditType;
    }

    public String getExternalId() {
        return externalId;
    }

    public void setExternalId(String externalId) {
        this.externalId = externalId;
    }

    public Long getLastUpdatedAuditTransactionId() {
        return lastUpdatedAuditTransactionId;
    }

    public void setLastUpdatedAuditTransactionId(Long lastUpdatedAuditTransactionId) {
        this.lastUpdatedAuditTransactionId = lastUpdatedAuditTransactionId;
    }
}
