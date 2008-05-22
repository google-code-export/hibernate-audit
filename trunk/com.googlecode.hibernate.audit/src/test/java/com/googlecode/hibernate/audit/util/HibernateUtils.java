package com.googlecode.hibernate.audit.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;
import org.apache.log4j.Logger;

import java.util.Iterator;

/**
 * Utility class for Hibernate related routines.
 *
 * @author <a href="mailto:chobantonov@gmail.com">Petko Chobantonov</a>
 * @author <a href="mailto:jchobantonov@gmail.com">Zhelyazko Chobantonov</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class HibernateUtils {

    private static final Logger log = Logger.getLogger(HibernateUtils.class);

	private static SessionFactory sessionFactory;

    /**
     * The thread-bound current Hibernate session.
     */
	public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

    public static synchronized void initPersistenceUnit() throws Exception
    {
        if (sessionFactory != null)
        {
            log.debug("Hibernate persistence unit already initialized");
            return;
        }

        log.debug("initializing Hibernate persistence unit");

        AnnotationConfiguration config = new AnnotationConfiguration();
        config.configure();

        sessionFactory = config.buildSessionFactory();

        dropTestTables(config);
    }


    public static synchronized void dropPersistenceUnit() throws Exception
    {
        if (sessionFactory == null)
        {
            log.debug("Hibernate persistence unit already dropped");
            return;
        }

        sessionFactory.close(); // this will drop all tables, assuming that 'hbm2ddl.auto' is
                                // "create-drop", which it should.
        sessionFactory = null;
    }

    /**
	 * @return the current Hibernate session, or opens a new one if there is none yet.
     *
	 * @throws HibernateException in case of error.
	 */
	public static Session getCurrentSession() throws HibernateException {

        checkInit();

        Session s = session.get();
		// open a new Session, if this thread has none yet.
		if (s == null) {
			s = (Session) sessionFactory.openSession();
			session.set(s);
		}
		return s;
	}

	/**
	 * Closes the current Hibernate session.
	 * 
	 * @throws HibernateException
	 *             in case of error.
	 */
	public static void closeCurrentSession() throws HibernateException {

        checkInit();

        Session s = session.get();
		session.set(null);
		if (s != null) {
			s.close();
		}
	}

    private static synchronized void checkInit() throws IllegalStateException
    {
        if (sessionFactory != null)
        {
            return;
        }

        throw new IllegalStateException("Hibernate persistence unit not initialized");
    }

    private static void dropTestTables(AnnotationConfiguration config) throws Exception
    {
        for(Iterator i = config.getTableMappings(); i.hasNext(); )
        {
            System.out.println(">>>" + i.next());
        }
    }

}
