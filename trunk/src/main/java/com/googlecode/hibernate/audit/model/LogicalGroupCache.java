package com.googlecode.hibernate.audit.model;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.EntityMode;
import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.persister.entity.EntityPersister;
import com.googlecode.hibernate.audit.util.wocache.WriteOnceCache;
import com.googlecode.hibernate.audit.util.wocache.InstanceFactory;
import com.googlecode.hibernate.audit.util.wocache.Key;
import com.googlecode.hibernate.audit.util.wocache.CacheQuery;
import com.googlecode.hibernate.audit.LogicalGroup;

/**
 * A fast internal transactional cache for write-once logical group instances.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class LogicalGroupCache
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(LogicalGroupCache.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private WriteOnceCache<AuditLogicalGroup> logicalGroups;
    private InstanceFactory lgif;
    private TypeCache typeCache;
    private SessionFactoryImplementor sf;

    // Constructors --------------------------------------------------------------------------------

    public LogicalGroupCache(SessionFactory isf, TypeCache typeCache, SessionFactoryImplementor sf)
    {
        logicalGroups = new WriteOnceCache<AuditLogicalGroup>(isf);
        lgif = new AuditLogicalGroupInstanceFactory();
        this.typeCache = typeCache;
        this.sf = sf;

        log.debug(this + " constructed");
    }

    // Public --------------------------------------------------------------------------------------

    public AuditLogicalGroup getLogicalGroup(LogicalGroup lg) throws Exception
    {
        return getLogicalGroup(lg, true);
    }

    /**
     * @param insert - specifies behavior on cache miss - if true, transactionally insert in the
     *        database, return null otherwise.
     */
    public AuditLogicalGroup getLogicalGroup(LogicalGroup lg, boolean insert) throws Exception
    {
        // look up the corresponding AuditType

        // TODO LAT

        String definingEntityName = lg.getDefiningEntityName();
        EntityPersister ep = sf.getEntityPersister(definingEntityName);
        Class ec = ep.getMappedClass(EntityMode.POJO);
        Class idc = ep.getIdentifierType().getReturnedClass();
        AuditType at = typeCache.getAuditEntityType(idc, ec, insert);

        if (at == null)
        {
            if (insert)
            {
                // the previous call should've thrown exception, but be extra sure and make sure
                // we fail
                throw new IllegalStateException(
                   "failed to insert a new AuditType corresponding to " + ec + " in cache");
            }

            return null;
        }

        CacheQuery<AuditLogicalGroup> cq =
            new CacheQuery<AuditLogicalGroup>(AuditLogicalGroup.class, insert, lgif,
                                              "externalId", lg.getLogicalGroupId(),
                                              "auditType", at);

        AuditLogicalGroup alg = logicalGroups.get(cq);

        if (alg != null)
        {
            // restore instance integrity, see https://jira.novaordis.org/browse/HBA-149
            alg.setAuditType(at);
        }

        return alg;
    }

    public void clear()
    {
        logicalGroups.clear();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class AuditLogicalGroupInstanceFactory implements InstanceFactory
    {
        public AuditLogicalGroup createInstance(Key key)
        {
            Long externalId = (Long)key.getValue("externalId");

            if (externalId == null)
            {
                throw new IllegalArgumentException("missing external id");
            }

            AuditType at = (AuditType)key.getValue("auditType");

            if (at == null)
            {
                throw new IllegalArgumentException("missing type");
            }

            AuditLogicalGroup alg = new AuditLogicalGroup();
            alg.setLogicalGroupId(externalId);
            alg.setAuditType(at);
            return alg;
        }
    }
}