package com.googlecode.hibernate.audit.util;

import org.hibernate.type.CollectionType;
import org.hibernate.type.BagType;
import org.hibernate.type.SetType;
import org.hibernate.type.EntityType;
import org.hibernate.type.Type;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.Transaction;
import org.hibernate.cfg.Environment;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.transaction.JTATransaction;
import org.hibernate.tuple.Tuplizer;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.apache.log4j.Logger;

import javax.transaction.TransactionManager;
import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
import java.util.Collections;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import com.googlecode.hibernate.audit.model.AuditType;
import com.googlecode.hibernate.audit.model.TypeCache;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Hibernate
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(Hibernate.class);

    // Static --------------------------------------------------------------------------------------

    public static Class collectionTypeToClass(CollectionType ct)
    {
        if (ct instanceof BagType)
        {
            // this is hibernate's unordered collection that accepts duplicates, we use list
            return List.class;
        }
        else if (ct instanceof SetType)
        {
            return Set.class;
        }

        throw new RuntimeException("we don't know to translate " + ct);
    }

    /**
     * @return null if it cannot figure out the type.
     */
    public static Class getTypeFromTuplizer(EntityPersister p, EntityMode m)
    {
        Tuplizer t = p.getEntityMetamodel().getTuplizerOrNull(m);

        if (t == null)
        {
            return null;
        }

        return t.getMappedClass();
    }

    /**
     * TODO this is a hack, should go away when https://jira.novaordis.org/browse/HBA-80 is fixed
     * @return null if cannot figure out entityName based on class.
     */
    public static String entityNameHeuristics(Class c)
    {

        String name = c.getName();

        name = name.substring(name.lastIndexOf('.') + 1);

        if (name.endsWith("Impl"))
        {
            name = name.substring(0, name.lastIndexOf("Impl"));
        }

        return name;
    }

    public static Class guessEntityClass(EntityType et, EntityPersister ep, EntityMode em)
    {
        Class entityClass = et.getReturnedClass();

        if (Map.class.equals(entityClass))
        {
            // this is what Hibernate returns when it cannot figure out the class, most likley due
            // to the fact that audited application uses entity names and custom tuplizers
            EntityTuplizer t = ep.getEntityMetamodel().getTuplizer(em);
            entityClass = t.getMappedClass();
        }

        return entityClass;
    }

    public static String roleToVariableName(String role)
    {
        int i = role.lastIndexOf('.');

        if (i == -1)
        {
            return role;
        }

        return role.substring(i + 1);
    }

    /**
     * TODO this is a hack give access to underlying transaction, won't work anywhere else but a
     * JTA environment.
     *
     * May return null if no underlying JTA transaction is found.
     *
     * See https://jira.novaordis.org/browse/HBA-134
     */
    public static javax.transaction.Transaction getUnderlyingTransaction(SessionFactoryImpl sf, 
                                                                         Transaction hibernateTx)
        throws Exception
    {
        if (!(hibernateTx instanceof JTATransaction))
        {
            throw new RuntimeException(
                "NOT A JTA ENVIRONMENT, NOT YET IMPLEMENTED, " +
                "SEE https://jira.novaordis.org/browse/HBA-134");
        }

        TransactionManager tm = sf.getTransactionManager();

        javax.transaction.Transaction tx = tm.getTransaction();

        if (tx == null)
        {
            // extra debugging info
            log.debug("null JTA transaction, transaction manager " + tm +
                      ", transaction manager status: " + tm.getStatus());
        }

        return tx;
    }

    /**
     * TODO refactor listener code to use this method.
     * https://jira.novaordis.org/browse/HBA-171
     */
    public static AuditType hibernateTypeToAuditType(Type hibernateType,
                                                     TypeCache typeCache,
                                                     SessionFactoryImplementor sf) throws Exception
    {
        AuditType auditType = null;

        if (hibernateType.isEntityType())
        {
            EntityType et = (EntityType)hibernateType;
            String en = et.getAssociatedEntityName();
            EntityPersister ep = sf.getEntityPersister(en);
            Class ec = Hibernate.guessEntityClass(et, ep, EntityMode.POJO);
            Class idc = ep.getIdentifierType().getReturnedClass();
            auditType = typeCache.getAuditEntityType(idc, ec);
        }
        else if (hibernateType.isCollectionType())
        {
            // figure out collection type
            CollectionType ct = (CollectionType)hibernateType;
            Class cc = Hibernate.collectionTypeToClass(ct);

            // figure out element type
            Type et = ct.getElementType(sf);
            if (!(et instanceof EntityType))
            {
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }
            EntityType eet = (EntityType)et;
            String een = eet.getAssociatedEntityName();
            EntityPersister eep = sf.getEntityPersister(een);
            Class eec = Hibernate.guessEntityClass(eet, eep, EntityMode.POJO);

            auditType = typeCache.getAuditCollectionType(cc, eec);
        }
        else if (hibernateType.isComponentType())
        {
            // not handled yet
            // TODO https://jira.novaordis.org/browse/HBA-32
        }
        else
        {
            auditType = typeCache.getAuditPrimitiveType(hibernateType.getReturnedClass());
        }

        return auditType;
    }

    private static Set<String> hibernatePropertyNames;

    /**
     * Returns a Set of Hibernate property names, as declared in Hibernate's Environment class:
     * 'hibernate.max_fetch_depth', 'hibernate.hbm2ddl.auto' etc. The method caches statically,
     * as we don't expect to hot redeploy Hibernate. Returns an unmodifiable Set.
     */
    public synchronized static Set<String> getHibernatePropertyNames()
    {
        if (hibernatePropertyNames == null)
        {
            hibernatePropertyNames = new HashSet<String>();
            Field[] fields = Environment.class.getFields();
            for(Field f: fields)
            {
                int mod = f.getModifiers();
                if (!Modifier.isPublic(mod) || !Modifier.isStatic(mod) || !Modifier.isFinal(mod))
                {
                    continue;
                }

                String value = null;

                try
                {
                    Object o = f.get(null);

                    if (!(o instanceof String))
                    {
                        continue;
                    }

                    value = (String)o;
                }
                catch(Exception e)
                {
                    // ignore, we're not interested in this field
                    continue;
                }

                hibernatePropertyNames.add(value);
            }

            hibernatePropertyNames = Collections.unmodifiableSet(hibernatePropertyNames);
        }

        return hibernatePropertyNames;
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
