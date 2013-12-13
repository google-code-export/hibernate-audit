package com.googlecode.hibernate.audit;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.hibernate.EntityMode;
import org.hibernate.Hibernate;
import org.hibernate.HibernateException;
import org.hibernate.MappingException;
import org.hibernate.PropertyNotFoundException;
import org.hibernate.Session;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.persister.entity.AbstractEntityPersister;
import org.hibernate.property.BasicPropertyAccessor;
import org.hibernate.property.DirectPropertyAccessor;
import org.hibernate.property.PropertyAccessor;
import org.hibernate.property.Setter;
import org.hibernate.proxy.HibernateProxy;
import org.hibernate.type.CollectionType;
import org.hibernate.type.ComponentType;
import org.hibernate.type.CompositeType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;

import com.googlecode.hibernate.audit.configuration.AuditConfiguration;
import com.googlecode.hibernate.audit.model.AuditEvent;
import com.googlecode.hibernate.audit.model.clazz.AuditType;
import com.googlecode.hibernate.audit.model.object.AuditObject;
import com.googlecode.hibernate.audit.model.object.EntityAuditObject;
import com.googlecode.hibernate.audit.model.property.AuditObjectProperty;
import com.googlecode.hibernate.audit.model.property.ComponentObjectProperty;
import com.googlecode.hibernate.audit.model.property.EntityObjectProperty;
import com.googlecode.hibernate.audit.model.property.SimpleObjectProperty;

public final class HibernateAuditInstantiator {
    private static final PropertyAccessor BASIC_PROPERTY_ACCESSOR = new BasicPropertyAccessor();
    private static final PropertyAccessor DIRECT_PROPERTY_ACCESSOR = new DirectPropertyAccessor();

    private static final ThreadLocal<Map<EntityKey, Object>> KEY_TO_OBJECT_CONTEXT = new ThreadLocal<Map<EntityKey, Object>>() {
        @Override
        protected Map<EntityKey, Object> initialValue() {
            return new HashMap<EntityKey, Object>();
        }
    };

    private static final ThreadLocal<Map<EntityKey, Object>> DELETED_KEY_TO_OBJECT_CONTEXT = new ThreadLocal<Map<EntityKey, Object>>() {
        @Override
        protected Map<EntityKey, Object> initialValue() {
            return new HashMap<EntityKey, Object>();
        }
    };

    private HibernateAuditInstantiator() {
    }

    /**
     * Reconstruct the object from the audit log.
     * 
     * @param session
     * @param auditType
     * @param externalId
     * @param transactionId
     * @return
     */
    public static Object getEntity(Session session, AuditType auditType, String externalId, Long transactionId) {
        AuditConfiguration auditConfiguration = HibernateAudit.getAuditConfiguration(session);
        KEY_TO_OBJECT_CONTEXT.get().clear();
        DELETED_KEY_TO_OBJECT_CONTEXT.get().clear();
        Object result;
        try {
            result = doGetEntity(session, auditType, externalId, transactionId, auditConfiguration);
            return result;
        } catch (ClassNotFoundException e) {
            throw new HibernateException(e);
        } finally {
            KEY_TO_OBJECT_CONTEXT.get().clear();
            DELETED_KEY_TO_OBJECT_CONTEXT.get().clear();
        }
    }

    private static Object doGetEntity(Session session, AuditType auditType, String externalId, Long transactionId, AuditConfiguration auditConfiguration) throws ClassNotFoundException {
        if (KEY_TO_OBJECT_CONTEXT.get().containsKey(new EntityKey(auditType.getClassName(), externalId))) {
            return KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(auditType.getClassName(), externalId));
        }
        List<AuditEvent> auditEvents = HibernateAudit.getAllAuditEventsForEntityUntilTransactionId(session, auditType, externalId, transactionId);
        for (AuditEvent event : auditEvents) {
            for (AuditObject object : event.getAuditObjects()) {
                object = instantiate(object);

                if (object instanceof EntityAuditObject) {
                    EntityAuditObject entity = (EntityAuditObject) object;

                    Object entityObject;

                    if (AuditEvent.INSERT_AUDIT_EVENT_TYPE.equals(event.getType())) {
                        String entityName = HibernateAudit.getEntityName(auditConfiguration, session, entity.getAuditType().getClassName());
                        ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(entityName);
                        Serializable id = (Serializable) auditConfiguration.getExtensionManager().getPropertyValueConverter().valueOf(classMetadata.getIdentifierType().getReturnedClass(),
                                entity.getTargetEntityId());
                        entityObject = ((AbstractEntityPersister) classMetadata).getEntityMetamodel().getTuplizer().instantiate(id, (SessionImplementor)session);

                        // place the object before property initialize so if
                        // there is bi-directional relationship they will be
                        // initialized correctly.

                        KEY_TO_OBJECT_CONTEXT.get().put(new EntityKey(entity.getAuditType().getClassName(), entity.getTargetEntityId()), entityObject);
                        initializeProperties(session, auditConfiguration, event, object, entityObject, classMetadata, transactionId);
                    } else if (AuditEvent.UPDATE_AUDIT_EVENT_TYPE.equals(event.getType())) {
                        entityObject = KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(entity.getAuditType().getClassName(), entity.getTargetEntityId()));

                        String entityName = HibernateAudit.getEntityName(auditConfiguration, session, entity.getAuditType().getClassName());
                        ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(entityName);

                        initializeProperties(session, auditConfiguration, event, object, entityObject, classMetadata, transactionId);
                    } else if (AuditEvent.DELETE_AUDIT_EVENT_TYPE.equals(event.getType())) {
                        // need to remove all references
                        Object removedEntity = KEY_TO_OBJECT_CONTEXT.get().remove(new EntityKey(entity.getAuditType().getClassName(), entity.getTargetEntityId()));
                        DELETED_KEY_TO_OBJECT_CONTEXT.get().put(new EntityKey(entity.getAuditType().getClassName(), entity.getTargetEntityId()), removedEntity);
                    } else if (AuditEvent.ADD_AUDIT_EVENT_TYPE.equals(event.getType())) {
                        entityObject = KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(entity.getAuditType().getClassName(), entity.getTargetEntityId()));

                        String entityName = HibernateAudit.getEntityName(auditConfiguration, session, entity.getAuditType().getClassName());
                        ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(entityName);

                        initializeProperties(session, auditConfiguration, event, object, entityObject, classMetadata, transactionId);
                    } else if (AuditEvent.MODIFY_AUDIT_EVENT_TYPE.equals(event.getType())) {
                        entityObject = KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(entity.getAuditType().getClassName(), entity.getTargetEntityId()));

                        String entityName = HibernateAudit.getEntityName(auditConfiguration, session, entity.getAuditType().getClassName());
                        ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(entityName);

                        initializeProperties(session, auditConfiguration, event, object, entityObject, classMetadata, transactionId);
                    } else if (AuditEvent.REMOVE_AUDIT_EVENT_TYPE.equals(event.getType())) {
                        entityObject = KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(entity.getAuditType().getClassName(), entity.getTargetEntityId()));

                        String entityName = HibernateAudit.getEntityName(auditConfiguration, session, entity.getAuditType().getClassName());
                        ClassMetadata classMetadata = session.getSessionFactory().getClassMetadata(entityName);

                        initializeProperties(session, auditConfiguration, event, object, entityObject, classMetadata, transactionId);
                    }
                }
            }
        }

        return KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(auditType.getClassName(), externalId));
    }

    private static void initializeProperties(Session session, AuditConfiguration auditConfiguration, AuditEvent event, AuditObject object, Object entityObject, ClassMetadata classMetadata,
            Long transactionId) throws ClassNotFoundException {
        for (AuditObjectProperty property : object.getAuditObjectProperties()) {
            property = instantiate(property);

            if (property instanceof EntityObjectProperty) {
                EntityObjectProperty prop = (EntityObjectProperty) property;

                AuditType propertyFieldType = prop.getAuditType();
                Object entityValue = null;
                if (propertyFieldType != null) {
                    entityValue = doGetEntity(session, propertyFieldType, prop.getTargetEntityId(), transactionId, auditConfiguration);

                    if (entityValue == null) {
                        if (DELETED_KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(propertyFieldType.getClassName(), prop.getTargetEntityId())) == null) {
                            // the object was not detected to be deleted so
                            // check if it is a config data..

                            String entityName = HibernateAudit.getEntityName(auditConfiguration, session, propertyFieldType.getClassName());
                            ClassMetadata metadata = session.getSessionFactory().getClassMetadata(entityName);

                            Serializable id = (Serializable) auditConfiguration.getExtensionManager().getPropertyValueConverter().valueOf(metadata.getIdentifierType().getReturnedClass(),
                                    prop.getTargetEntityId());

                            entityValue = session.get(entityName, id);
                        }
                    }
                }

                if (prop.getIndex() == null) {
                    classMetadata.setPropertyValue(entityObject, prop.getAuditField().getName(), entityValue);
                } else {
                    Object collectionValue = classMetadata.getPropertyValue(entityObject, prop.getAuditField().getName());
                    Type colType = classMetadata.getPropertyType(prop.getAuditField().getName());
                    if (colType instanceof CollectionType && collectionValue instanceof Collection) {
                        CollectionType collectionType = (CollectionType) colType;

                        Collection collection = ((Collection) collectionValue);
                        if (AuditEvent.ADD_AUDIT_EVENT_TYPE.equals(event.getType())) {
                            Collection newCollectionValue = null;
                            if (DELETED_KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(propertyFieldType.getClassName(), prop.getTargetEntityId())) == null) {
                                if (collection == null || collection.isEmpty()) {
                                    newCollectionValue = (Collection) collectionType.instantiate(1);
                                    newCollectionValue.add(entityValue);
                                    classMetadata.setPropertyValue(entityObject, prop.getAuditField().getName(), newCollectionValue);
                                } else {
                                    collection.add(entityValue);
                                }
                            }
                        } else if (AuditEvent.REMOVE_AUDIT_EVENT_TYPE.equals(event.getType())) {
                            Type elementType = collectionType.getElementType((SessionFactoryImplementor) session.getSessionFactory());

                            ClassMetadata elementClassMetadata = session.getSessionFactory().getClassMetadata(((EntityType) elementType).getAssociatedEntityName());
                            Serializable deletedElementId = (Serializable) auditConfiguration.getExtensionManager().getPropertyValueConverter().valueOf(
                                    elementClassMetadata.getIdentifierType().getReturnedClass(), prop.getTargetEntityId());

                            boolean elementFound = false;
                            for (Iterator i = collection.iterator(); i.hasNext();) {
                                Object existingElement = i.next();

                                Serializable existingElementId = ((AbstractEntityPersister) elementClassMetadata).getIdentifier(existingElement);
                                if (deletedElementId != null && deletedElementId.equals(existingElementId)) {
                                    i.remove();
                                    elementFound = true;
                                    break;
                                }
                            }

                            if (!elementFound && (DELETED_KEY_TO_OBJECT_CONTEXT.get().get(new EntityKey(propertyFieldType.getClassName(), prop.getTargetEntityId())) == null)) {
                                throw new HibernateException("Unable to find entity with id " + deletedElementId + " in collection " + prop.getAuditField().getOwnerType().getClassName() + "."
                                        + prop.getAuditField().getName() + " collection");
                            }
                        }
                    } else {
                        throw new HibernateException("The property " + prop.getAuditField().getName() + " has multiple values but we currently only support " + Collection.class.getName() + " types.");
                    }
                }
            } else if (property instanceof ComponentObjectProperty) {
                try {
                    ComponentObjectProperty prop = (ComponentObjectProperty) property;

                    CompositeType componentType = (CompositeType) classMetadata.getPropertyType(prop.getAuditField().getName());
                    Object component = null;
                    if (componentType instanceof ComponentType) {
                        component = ((ComponentType) componentType).instantiate(EntityMode.POJO);
                    } else {
                        Constructor constructor = org.hibernate.internal.util.ReflectHelper.getDefaultConstructor(Class.forName(prop.getAuditField().getFieldType().getClassName()));
                        component = constructor.newInstance(null);
                    }
                    String[] propertyNames = componentType.getPropertyNames();
                    Object[] values = new Object[propertyNames.length];

                    for (AuditObjectProperty componentProperty : prop.getTargetComponentAuditObject().getAuditObjectProperties()) {
                        if (componentProperty instanceof EntityObjectProperty) {
                            EntityObjectProperty componentProp = (EntityObjectProperty) componentProperty;

                            Object entityValue = doGetEntity(session, componentProp.getAuditField().getFieldType(), componentProp.getTargetEntityId(), transactionId, auditConfiguration);
                            for (int i = 0; i < propertyNames.length; i++) {
                                if (propertyNames[i].equals(componentProp.getAuditField().getName())) {
                                    values[i] = entityValue;
                                    break;
                                }
                            }
                        } else if (componentProperty instanceof ComponentObjectProperty) {
                            ComponentObjectProperty componentProp = (ComponentObjectProperty) componentProperty;

                        } else {
                            SimpleObjectProperty componentProp = (SimpleObjectProperty) componentProperty;

                            Object value = auditConfiguration.getExtensionManager().getPropertyValueConverter().valueOf(Class.forName(componentProp.getAuditField().getFieldType().getClassName()),
                                    componentProp.getValue());
                            for (int i = 0; i < propertyNames.length; i++) {
                                if (propertyNames[i].equals(componentProp.getAuditField().getName())) {
                                    values[i] = value;
                                    break;
                                }
                            }
                        }
                    }

                    if (componentType.isMutable()) {
                        componentType.setPropertyValues(component, values, EntityMode.POJO);
                    } else {
                        for (int i = 0; i < componentType.getPropertyNames().length; i++) {
                            Setter setter = setter(component.getClass(), componentType.getPropertyNames()[i]);
                            setter.set(component, values[i], (SessionFactoryImplementor) session.getSessionFactory());
                        }
                    }

                    if (prop.getIndex() == null) {
                        classMetadata.setPropertyValue(entityObject, prop.getAuditField().getName(), component);
                    } else {
                        Object collectionValue = classMetadata.getPropertyValue(entityObject, prop.getAuditField().getName());
                        Type colType = classMetadata.getPropertyType(prop.getAuditField().getName());

                        if (colType instanceof CollectionType && collectionValue instanceof Collection) {
                            CollectionType collectionType = (CollectionType) colType;
                            Collection collection = ((Collection) collectionValue);
                            if (AuditEvent.ADD_AUDIT_EVENT_TYPE.equals(event.getType())) {
                                if (collection == null || collection.isEmpty()) {
                                    Collection newCollectionValue = (Collection) collectionType.instantiate(1);
                                    newCollectionValue.add(component);
                                    classMetadata.setPropertyValue(entityObject, prop.getAuditField().getName(), newCollectionValue);
                                } else {
                                    collection.add(component);
                                }
                                // explicitly invoke the set method so that the
                                // tuplizer can be involved as well.
                                // classMetadata.setPropertyValue(entityObject,
                                // prop.getAuditField().getName(),
                                // newCollectionValue, EntityMode.POJO);
                            } else if (AuditEvent.REMOVE_AUDIT_EVENT_TYPE.equals(event.getType())) {
                                long index = 0;
                                for (Iterator i = collection.iterator(); i.hasNext(); index++) {
                                    i.next();
                                    if (index == prop.getIndex()) {
                                        i.remove();
                                        break;
                                    }
                                }
                            }
                        } else {
                            throw new HibernateException("The property " + prop.getAuditField().getName() + " has multiple values but we currently only support " + Collection.class.getName()
                                    + " types.");
                        }
                    }
                } catch (InstantiationException e) {
                    throw new HibernateException(e);
                } catch (InvocationTargetException e) {
                    throw new HibernateException(e);
                } catch (IllegalAccessException e) {
                    throw new HibernateException(e);
                }
            } else {
                SimpleObjectProperty prop = (SimpleObjectProperty) property;
                Object value = null;
                if (prop.getValue() != null) {
                    value = auditConfiguration.getExtensionManager().getPropertyValueConverter().valueOf(Class.forName(prop.getAuditType().getClassName()), prop.getValue());
                }
                if (prop.getIndex() == null) {
                    classMetadata.setPropertyValue(entityObject, prop.getAuditField().getName(), value);
                } else {
                    Object collectionValue = classMetadata.getPropertyValue(entityObject, prop.getAuditField().getName());
                    if (collectionValue instanceof Collection) {
                        if (AuditEvent.ADD_AUDIT_EVENT_TYPE.equals(event.getType())) {
                            ((Collection) collectionValue).add(value);
                        } else if (AuditEvent.REMOVE_AUDIT_EVENT_TYPE.equals(event.getType())) {
                            long index = 0;
                            for (Iterator i = ((Collection) collectionValue).iterator(); i.hasNext(); index++) {
                                if (index == prop.getIndex()) {
                                    i.remove();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private static AuditObject instantiate(AuditObject object) {
        Hibernate.initialize(object);

        if (object instanceof HibernateProxy) {
            object = (AuditObject) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        return object;
    }

    private static AuditObjectProperty instantiate(AuditObjectProperty object) {
        Hibernate.initialize(object);

        if (object instanceof HibernateProxy) {
            object = (AuditObjectProperty) ((HibernateProxy) object).getHibernateLazyInitializer().getImplementation();
        }
        return object;
    }

    private static Setter setter(Class clazz, String name) throws MappingException {
        try {
            return BASIC_PROPERTY_ACCESSOR.getSetter(clazz, name);
        } catch (PropertyNotFoundException pnfe) {
            return DIRECT_PROPERTY_ACCESSOR.getSetter(clazz, name);
        }
    }

    private static class EntityKey {
        private String className;
        private String id;

        public EntityKey(String className, String id) {
            this.className = className;
            this.id = id;
        }

        public String getClassName() {
            return className;
        }

        public String getId() {
            return id;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof EntityKey)) {
                return false;
            }
            return className.equals(((EntityKey) obj).getClassName()) && id.equals(((EntityKey) obj).getId());
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 31 * hash + (null == className ? 0 : className.hashCode());
            hash = 31 * hash + (null == id ? 0 : id.hashCode());

            return hash;
        }
    }

}
