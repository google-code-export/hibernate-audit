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
package com.googlecode.hibernate.audit.model.clazz;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Entity that represents audit field meta data information.
 */
public class AuditTypeField implements Serializable {

    protected Long id;
    protected String name;
    protected String label;
    protected AuditType ownerType;
    protected AuditType fieldType;
    protected List<AuditTypeFieldAttribute> auditTypeFieldAttributes;

    public List<AuditTypeFieldAttribute> getAuditTypeFieldAttributes() {
        if (auditTypeFieldAttributes == null) {
            auditTypeFieldAttributes = new ArrayList<AuditTypeFieldAttribute>();
        }
        return auditTypeFieldAttributes;
    }

    public void setAuditTypeFieldAttributes(List<AuditTypeFieldAttribute> auditTypeFieldAttributes) {
        this.auditTypeFieldAttributes = auditTypeFieldAttributes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long newId) {
        id = newId;
    }

    public String getName() {
        return name;
    }

    public void setName(String newName) {
        name = newName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }

    public AuditType getOwnerType() {
        return ownerType;
    }

    public void setOwnerType(AuditType newOwnerType) {
        ownerType = newOwnerType;
    }

    public AuditType getFieldType() {
        return fieldType;
    }

    public void setFieldType(AuditType newFieldType) {
        fieldType = newFieldType;
    }
}