package com.googlecode.hibernate.audit.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.AnnotationConfiguration;

/**
 * Utility class for Hibernate related routines.
 */
public class HibernateUtils {

	/**
	 * The session factory
	 */
	private static SessionFactory sessionFactory;

	static {
		try {
			sessionFactory = new AnnotationConfiguration().configure()
					.buildSessionFactory();
		} catch (ExceptionInInitializerError ie) {
			throw ie;
		} catch (Throwable t) {
			throw new ExceptionInInitializerError(t);
		}
	}

	/** the current Hibernate session. */
	public static final ThreadLocal<Session> session = new ThreadLocal<Session>();

	/**
	 * Returns the current Hibernate session, or opens a new one if there is
	 * none yet.
	 * 
	 * @return the current Hibernate session, or opens a new one if there is
	 *         none yet.
	 * @throws HibernateException
	 *             in case of error.
	 */
	public static Session getCurrentSession() throws HibernateException {
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
		Session s = session.get();
		session.set(null);
		if (s != null) {
			s.close();
		}
	}
}
