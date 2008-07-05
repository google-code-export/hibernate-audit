package com.googlecode.hibernate.audit.test.mock.jta;

/**
 * A tagging interface to identify an XAResource that does not support prepare and should be used
 * in the last resource gambit. i.e. it is committed after the resources are prepared. If it fails
 * to commit, roll everybody back.
 *
 * @author <a href="mailto:adrian@jboss.org">Adrian Brock</a>
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public interface LastResource
{
}
