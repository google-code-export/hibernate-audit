package com.googlecode.hibernate.audit;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.event.EventListeners;
import org.hibernate.event.PostInsertEventListener;
import org.hibernate.impl.SessionFactoryImpl;
import com.googlecode.hibernate.audit.listener.AuditEventListener;

/**
 * The main programmatic entry point. This class allows turning audit on/off at runtime, and various
 * other configuration options.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class HibernateAudit
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(HibernateAudit.class);

    // Static --------------------------------------------------------------------------------------

    private static HibernateAudit singleton;

    public static synchronized boolean isEnabled()
    {
        return singleton != null;
    }

    /**
     * Turns audit on.
     *
     * The persistence unit being audited will be used to persist the audit data as well.
     */
    public static void enable(SessionFactory auditedSessionFactory) throws Exception
    {
        enable(auditedSessionFactory, null);
    }

    /**
     * Turns audit on.
     *
     * The caller has a choice in using a different persistence unit to store audited data.
     *
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     * @param resource - the resource which contains the configuration for the secondary persistence
     *        unit (used to persist audit data). A null resource means that there is no secondary
     *        persistence unit, the audited persistence unit will be used to persist audit data as
     *        well.
     */
    public static synchronized void enable(SessionFactory auditedSessionFactory, String resource)
        throws Exception
    {
        if (singleton != null)
        {
            log.debug(singleton + " already enabled");
            return;
        }

        singleton = new HibernateAudit(auditedSessionFactory, resource);
        singleton.start();
    }

    /**
     * Turns audit off.
     *
     * @return true if calling this method ended up in audit being disabled, or false if there was
     *         no active audit runtime to disable.
     */
    public static boolean disable()
    {
        if (singleton == null)
        {
            return false;
        }

        singleton.stop();
        singleton = null;
        return true;
    }

    // Attributes ----------------------------------------------------------------------------------

    private SessionFactoryImpl auditedSessionFactory;
    private String secondaryConfigurationResource;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param auditedSessionFactory - the session factory of the audited persistence unit.
     * @param resource - the resource which contains the configuration for the secondary persistence
     *        unit (used to persist audit data). A null resource means that there is no secondary
     *        persistence unit, the audited persistence unit will be used to persist audit data as
     *        well.
     */
    private HibernateAudit(SessionFactory auditedSessionFactory, String resource) throws Exception
    {
        this.auditedSessionFactory = (SessionFactoryImpl)auditedSessionFactory;
        this.secondaryConfigurationResource = resource;
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "HibernateAuditRuntime[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private void start() throws Exception
    {
        log.debug(this + " starting ...");

        // if using a different persistence unit, initialize it first

        if (secondaryConfigurationResource != null)
        {
            //Configuration secondary = new AnnotationConfiguration()

//    /**
//     * Use the mappings and properties specified in the given application
//     * resource. The format of the resource is defined in
//     * <tt>hibernate-configuration-3.0.dtd</tt>.
//     * <p/>
//     * The resource is found via <tt>getConfigurationInputStream(resource)</tt>.
//     */
//    public Configuration configure(String resource) throws HibernateException {
//        log.info( "configuring from resource: " + resource );
//        InputStream stream = getConfigurationInputStream( resource );
//        return doConfigure( stream, resource );
//    }
//
//    /**
//     * Use the mappings and properties specified in the given document.
//     * The format of the document is defined in
//     * <tt>hibernate-configuration-3.0.dtd</tt>.
//     *
//     * @param url URL from which you wish to load the configuration
//     * @return A configuration configured via the file
//     * @throws HibernateException
//     */
//    public Configuration configure(URL url) throws HibernateException {
//        log.info( "configuring from url: " + url.toString() );
//        try {
//            return doConfigure( url.openStream(), url.toString() );
//        }
//        catch (IOException ioe) {
//            throw new HibernateException( "could not configure from URL: " + url, ioe );
//        }
//    }
//
//    /**
//     * Use the mappings and properties specified in the given application
//     * file. The format of the file is defined in
//     * <tt>hibernate-configuration-3.0.dtd</tt>.
//     *
//     * @param configFile <tt>File</tt> from which you wish to load the configuration
//     * @return A configuration configured via the file
//     * @throws HibernateException
//     */
//    public Configuration configure(File configFile) throws HibernateException {
//        log.info( "configuring from file: " + configFile.getName() );
//        try {
//            return doConfigure( new FileInputStream( configFile ), configFile.toString() );
//        }
//        catch (FileNotFoundException fnfe) {
//            throw new HibernateException( "could not find file: " + configFile, fnfe );
//        }
//    }

            throw new Exception("NOT YET IMPLEMENTED");
        }

        installAuditListeners(auditedSessionFactory);

        log.debug(this + " started");
    }

    private void stop()
    {
        log.debug(this + " stopping ...");

        uninstallAuditListeners(auditedSessionFactory);

        //TODO if using a different persistence unit, clean-up that

        auditedSessionFactory = null;

        log.debug(this + " stopped");
    }

    private void installAuditListeners(SessionFactoryImpl sf)
    {
        EventListeners els = sf.getEventListeners();

        PostInsertEventListener[] piels = els.getPostInsertEventListeners();

        // at this stage, we should not have any registered audit listeners
        for(PostInsertEventListener piel: piels)
        {
            if (piel instanceof AuditEventListener)
            {
                throw new IllegalStateException("Hibernate audit already enabled");
            }
        }

        log.debug(this + " installed audit listeners");
    }

    private void uninstallAuditListeners(SessionFactoryImpl sf)
    {
        log.debug(this + " uninstalled audit listeners");
    }

    // Inner classes -------------------------------------------------------------------------------
}
