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
 * Entity that represents audit class meta data information.
 */
public class AuditType implements Serializable {
    public static final char ENTITY_TYPE = 'E';
    public static final char COMPONENT_TYPE = 'C';
    public static final char COLLECTION_TYPE = 'A';
    public static final char PRIMITIVE_TYPE = 'P';

    protected Long id;

    protected String className;
    protected String label;
    protected char type;
    protected List<AuditTypeField> auditFields;
    protected List<AuditTypeAttribute> auditTypeAttributes;

    public List<AuditTypeAttribute> getAuditTypeAttributes() {
        if (auditTypeAttributes == null) {
            auditTypeAttributes = new ArrayList<AuditTypeAttribute>();
        }
        return auditTypeAttributes;
    }

    public void setAuditTypeAttributes(List<AuditTypeAttribute> auditTypeAttributes) {
        this.auditTypeAttributes = auditTypeAttributes;
    }

    public List<AuditTypeField> getAuditFields() {
        if (auditFields == null) {
            auditFields = new ArrayList<AuditTypeField>();
        }
        return auditFields;
    }

    public void setAuditFields(List<AuditTypeField> auditFields) {
        this.auditFields = auditFields;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long newId) {
        id = newId;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String newClassName) {
        className = newClassName;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String newLabel) {
        label = newLabel;
    }

    public char getType() {
        return type;
    }

    public void setType(char type) {
        this.type = type;
    }
}