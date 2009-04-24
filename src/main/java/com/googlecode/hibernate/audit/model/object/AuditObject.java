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
package com.googlecode.hibernate.audit.model.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;

public abstract class AuditObject implements Serializable {
    protected Long id;

    protected AuditEvent auditEvent;
    protected AuditType auditType;

    protected List<AuditObjectProperty> auditObjectProperties;

    public Long getId() {
        return id;
    }

    public void setId(Long newId) {
        id = newId;
    }

    public AuditEvent getAuditEvent() {
        return auditEvent;
    }

    public void setAuditEvent(AuditEvent auditEvent) {
        this.auditEvent = auditEvent;
    }

    public AuditType getAuditType() {
        return auditType;
    }

    public void setAuditType(AuditType auditType) {
        this.auditType = auditType;
    }

    public List<AuditObjectProperty> getAuditObjectProperties() {
        if (auditObjectProperties == null) {
            auditObjectProperties = new ArrayList<AuditObjectProperty>();
        }
        return auditObjectProperties;
    }

    public void setAuditObjectProperties(List<AuditObjectProperty> auditObjectProperties) {
        this.auditObjectProperties = auditObjectProperties;
    }
}
