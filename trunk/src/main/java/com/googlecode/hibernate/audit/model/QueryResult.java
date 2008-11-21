package com.googlecode.hibernate.audit.model;

import org.hibernate.Session;

import java.util.List;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class QueryResult
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private List result;
    private Session session;

    // Constructors --------------------------------------------------------------------------------

    public QueryResult(List result)
    {
        this(null, result);
    }

    public QueryResult(Session session, List result)
    {
        this.session = session;
        this.result = result;
    }

    // Public --------------------------------------------------------------------------------------

    public List getResult()
    {
        return result;
    }

    /**
     * May return null if the query(...) was invoked with leaveSessionOpen = false.
     */
    public Session getSession()
    {
        return session;
    }

    public void setSession(Session session)
    {
        this.session = session;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
