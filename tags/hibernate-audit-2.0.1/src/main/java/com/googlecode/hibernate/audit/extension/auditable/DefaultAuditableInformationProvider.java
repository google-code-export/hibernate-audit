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
package com.googlecode.hibernate.audit.extension.auditable;

import java.util.Iterator;

import org.hibernate.Session;
import org.hibernate.cfg.Configuration;
import org.hibernate.mapping.PersistentClass;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

public class DefaultAuditableInformationProvider implements AuditableInformationProvider {

    private AuditableInformationProvider provider;

    public DefaultAuditableInformationProvider() {

    }

    public DefaultAuditableInformationProvider(AuditableInformationProvider provider) {
        this.provider = provider;
    }

    public boolean isAuditable(String entityName) {
        if (entityName.startsWith("com.googlecode.hibernate.audit.")) {
            return false;
        }

        if (provider != null) {
            return provider.isAuditable(entityName);
        }
        return true;
    }

    public boolean isAuditable(String entityName, String propertyName) {
        if (!isAuditable(entityName)) {
            return false;
        }

        if (provider != null) {
            return provider.isAuditable(entityName, propertyName);
        }
        return true;
    }

    public String getAuditTypeClassName(Configuration configuration, String entityName) {
        if (provider != null) {
            return provider.getAuditTypeClassName(configuration, entityName);
        }
    	PersistentClass classMapping = configuration.getClassMapping(entityName);
    	Class mappedClass = classMapping.getMappedClass();
        
        if (mappedClass == null) {
        	mappedClass = classMapping.getProxyInterface();
        }
        return mappedClass.getName();
    }
    
    
    public String getEntityName(Configuration configuration, Session session, String auditTypeClassName) {
        if (provider != null) {
            return provider.getEntityName(configuration, session, auditTypeClassName);
        }
        for (Iterator<PersistentClass> iterator = configuration.getClassMappings(); iterator.hasNext();) {
        	PersistentClass classMapping = iterator.next();
            Class mappedClass = classMapping.getMappedClass();
            if (mappedClass == null) {
            	mappedClass = classMapping.getProxyInterface();
            }
            if (mappedClass.getName().equals(auditTypeClassName)) {
                return classMapping.getEntityName();
            }
        }
        return auditTypeClassName;
    }
    
    public String getAuditTypeClassName(Configuration configuration, Type type) {
        if (provider != null) {
            return provider.getAuditTypeClassName(configuration, type);
        }
    	if (type.isEntityType()) {
    		return getAuditTypeClassName(configuration, ((EntityType) type).getName());
    	}
		return type.getReturnedClass().getName();
    }
}
