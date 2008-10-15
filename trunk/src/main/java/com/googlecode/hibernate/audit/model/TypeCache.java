package com.googlecode.hibernate.audit.model;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCache;
import com.googlecode.hibernate.audit.util.wocache.CacheQuery;
import com.googlecode.hibernate.audit.util.wocache.InstanceFactory;
import com.googlecode.hibernate.audit.util.wocache.Key;

import java.util.Collection;

/**
 * A fast internal transactional cache for write-once data (instances of AuditType and subclasses,
 * AuditFieldType, etc).
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class TypeCache
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(TypeCache.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private WriteOnceCache<AuditType> types;
    private WriteOnceCache<AuditTypeField> fields;

    private InstanceFactory pif;
    private InstanceFactory eif;
    private InstanceFactory cif;
    private InstanceFactory fif;

    // Constructors --------------------------------------------------------------------------------

    public TypeCache(SessionFactory isf)
    {
        types = new WriteOnceCache<AuditType>(isf);
        fields = new WriteOnceCache<AuditTypeField>(isf);

        pif = new PrimitiveInstanceFactory();
        eif = new EntityInstanceFactory();
        cif = new CollectionInstanceFactory();
        fif = new FieldInstanceFactory();

        log.debug(this + " contstructed");
    }

    // Public --------------------------------------------------------------------------------------

    /**
     * TODO may be changed when refactoring https://jira.novaordis.org/browse/HBA-80
     */
    public AuditType getAuditPrimitiveType(Class primitiveClass) throws Exception
    {
        CacheQuery<AuditType> cq =
            new CacheQuery<AuditType>(AuditType.class, pif, "className", primitiveClass.getName());

        return types.get(cq);
    }

    /**
     * TODO may be changed when refactoring https://jira.novaordis.org/browse/HBA-80
     */
    public AuditEntityType getAuditEntityType(Class idClass, Class entityClass) throws Exception
    {
        CacheQuery<AuditType> cq =
            new CacheQuery<AuditType>(AuditEntityType.class, eif,
                                      "className", entityClass.getName(),
                                      "idClassName", idClass.getName());

        return (AuditEntityType)types.get(cq);
    }

    /**
     * TODO may be changed when refactoring https://jira.novaordis.org/browse/HBA-80
     */
    public AuditCollectionType getAuditCollectionType(Class collectionType, Class memberType)
         throws Exception
    {
        if (!Collection.class.isAssignableFrom(collectionType))
        {
            throw new IllegalArgumentException(
                "illegal usage, " + collectionType + " should be a collection type");
        }

        CacheQuery<AuditType> cq =
            new CacheQuery<AuditType>(AuditCollectionType.class, cif,
                                      "className", memberType.getName(),
                                      "collectionClassName", collectionType.getName());

        return (AuditCollectionType)types.get(cq);
    }

    /**
     * TODO may be changed when refactoring https://jira.novaordis.org/browse/HBA-80
     */
    public AuditTypeField getAuditTypeField(String fieldName, AuditType fieldType)
         throws Exception
    {
        CacheQuery<AuditTypeField> cq =
            new CacheQuery<AuditTypeField>(AuditTypeField.class, fif,
                                           "name", fieldName, "type", fieldType);
        return fields.get(cq);
    }

    public void clear()
    {
        types.clear();
        fields.clear();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class PrimitiveInstanceFactory implements InstanceFactory
    {
        public AuditType createInstance(Key key)
        {
            String cn = (String)key.getValue("className");

            if (cn == null)
            {
                throw new IllegalArgumentException("missing primitive class name");
            }

            AuditType result = new AuditType();
            result.setClassName(cn);
            return result;
        }
    }

    private class EntityInstanceFactory implements InstanceFactory
    {
        public AuditEntityType createInstance(Key key)
        {
            String cn = (String)key.getValue("className");

            if (cn == null)
            {
                throw new IllegalArgumentException("missing entity class name");
            }

            String icn = (String)key.getValue("idClassName");

            if (icn == null)
            {
                throw new IllegalArgumentException("missing id class name");
            }

            AuditEntityType result = new AuditEntityType();
            result.setClassName(cn);
            result.setIdClassName(icn);
            return result;
        }
    }

    private class CollectionInstanceFactory implements InstanceFactory
    {
        public AuditCollectionType createInstance(Key key)
        {
            String cn = (String)key.getValue("className");

            if (cn == null)
            {
                throw new IllegalArgumentException("missing collection member class name");
            }

            String ccn = (String)key.getValue("collectionClassName");

            if (ccn == null)
            {
                throw new IllegalArgumentException("missing collection class name");
            }

            AuditCollectionType result = new AuditCollectionType();
            result.setClassName(cn);
            result.setCollectionClassName(ccn);
            return result;
        }
    }

    private class FieldInstanceFactory implements InstanceFactory
    {
        public AuditTypeField createInstance(Key key)
        {
            String name = (String)key.getValue("name");

            if (name == null)
            {
                throw new IllegalArgumentException("missing field name");
            }

            AuditType type = (AuditType)key.getValue("type");

            if (type == null)
            {
                throw new IllegalArgumentException("missing field type");
            }

            AuditTypeField result = new AuditTypeField();
            result.setName(name);
            result.setType(type);
            return result;
        }
    }
}
