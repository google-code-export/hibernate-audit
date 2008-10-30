package com.googlecode.hibernate.audit.test.mock.hibernate;

import org.hibernate.engine.SessionFactoryImplementor;
import org.hibernate.engine.NamedQueryDefinition;
import org.hibernate.engine.NamedSQLQueryDefinition;
import org.hibernate.engine.ResultSetMappingDefinition;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.engine.query.QueryPlanCache;
import org.hibernate.persister.entity.EntityPersister;
import org.hibernate.persister.collection.CollectionPersister;
import org.hibernate.MappingException;
import org.hibernate.Interceptor;
import org.hibernate.HibernateException;
import org.hibernate.ConnectionReleaseMode;
import org.hibernate.StatelessSession;
import org.hibernate.impl.SessionFactoryImpl;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.proxy.EntityNotFoundDelegate;
import org.hibernate.classic.Session;
import org.hibernate.cfg.Settings;
import org.hibernate.exception.SQLExceptionConverter;
import org.hibernate.id.IdentifierGenerator;
import org.hibernate.stat.StatisticsImplementor;
import org.hibernate.stat.Statistics;
import org.hibernate.cache.QueryCache;
import org.hibernate.cache.UpdateTimestampsCache;
import org.hibernate.cache.Region;
import org.hibernate.connection.ConnectionProvider;
import org.hibernate.type.Type;
import org.hibernate.dialect.Dialect;
import org.hibernate.dialect.function.SQLFunctionRegistry;

import javax.naming.Reference;
import javax.naming.NamingException;
import javax.transaction.TransactionManager;
import java.util.Map;
import java.util.Set;
import java.sql.Connection;
import java.io.Serializable;


/**
 * An intercepted session factory that delegates to a real one and helps us to simulate all kind
 * of error conditions.
 *
 * NOT USED ANYWHERE YET.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 */
public class InterceptedSessionFactory implements SessionFactoryImplementor
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private SessionFactoryImpl delegate;

    // Constructors --------------------------------------------------------------------------------

    public InterceptedSessionFactory(SessionFactoryImpl delegate)
    {
        this.delegate = delegate;
    }

    // SessionFactoryImplementor implementation ----------------------------------------------------

    public EntityPersister getEntityPersister(String entityName) throws MappingException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public CollectionPersister getCollectionPersister(String role) throws MappingException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Dialect getDialect() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Interceptor getInterceptor() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public QueryPlanCache getQueryPlanCache() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Type[] getReturnTypes(String queryString) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public String[] getReturnAliases(String queryString) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public ConnectionProvider getConnectionProvider() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public String[] getImplementors(String className) throws MappingException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public String getImportedClassName(String name) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public TransactionManager getTransactionManager() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public QueryCache getQueryCache() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public QueryCache getQueryCache(String regionName) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public UpdateTimestampsCache getUpdateTimestampsCache() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public StatisticsImplementor getStatisticsImplementor() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NamedQueryDefinition getNamedQuery(String queryName) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public NamedSQLQueryDefinition getNamedSQLQuery(String queryName) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public ResultSetMappingDefinition getResultSetMapping(String name) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public IdentifierGenerator getIdentifierGenerator(String rootEntityName) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Region getSecondLevelCacheRegion(String regionName) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Map getAllSecondLevelCacheRegions() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public SQLExceptionConverter getSQLExceptionConverter() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Settings getSettings()
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Session openTemporarySession() throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Session openSession(Connection connection, boolean flushBeforeCompletionEnabled, boolean autoCloseSessionEnabled, ConnectionReleaseMode connectionReleaseMode) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Set getCollectionRolesByEntityParticipant(String entityName) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public EntityNotFoundDelegate getEntityNotFoundDelegate() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public SQLFunctionRegistry getSqlFunctionRegistry() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Type getIdentifierType(String className) throws MappingException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public String getIdentifierPropertyName(String className) throws MappingException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Type getReferencedPropertyType(String className, String propertyName) throws MappingException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Session openSession(Connection connection) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Session openSession(Interceptor interceptor) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Session openSession(Connection connection, Interceptor interceptor) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Session openSession() throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Session getCurrentSession() throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public ClassMetadata getClassMetadata(Class persistentClass) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public ClassMetadata getClassMetadata(String entityName) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public CollectionMetadata getCollectionMetadata(String roleName) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Map getAllClassMetadata() throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Map getAllCollectionMetadata() throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Statistics getStatistics() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void close() throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean isClosed() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evict(Class persistentClass) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evict(Class persistentClass, Serializable id) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evictEntity(String entityName) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evictEntity(String entityName, Serializable id) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evictCollection(String roleName) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evictCollection(String roleName, Serializable id) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evictQueries() throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void evictQueries(String cacheRegion) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public StatelessSession openStatelessSession() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public StatelessSession openStatelessSession(Connection connection) {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Set getDefinedFilterNames() {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public FilterDefinition getFilterDefinition(String filterName) throws HibernateException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Reference getReference() throws NamingException {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    public SessionFactoryImpl getDelegate()
    {
        return delegate;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
