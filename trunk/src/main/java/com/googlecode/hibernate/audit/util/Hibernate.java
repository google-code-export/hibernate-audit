package com.googlecode.hibernate.audit.util;

import org.hibernate.type.CollectionType;
import org.hibernate.type.BagType;
import org.hibernate.type.SetType;
import org.hibernate.type.EntityType;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.EntityMode;
import org.hibernate.Transaction;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.transaction.JTATransaction;
import org.hibernate.tuple.Tuplizer;
import org.hibernate.tuple.entity.EntityTuplizer;
import org.apache.log4j.Logger;

import javax.transaction.TransactionManager;
import java.util.List;
import java.util.Set;
import java.util.Map;

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

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
