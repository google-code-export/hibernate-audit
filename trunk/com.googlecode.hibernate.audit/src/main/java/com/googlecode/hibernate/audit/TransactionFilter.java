package com.googlecode.hibernate.audit;

import java.util.Date;

/**
 * TODO: add 'logical group' as a filtering criterion.
 * 
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class TransactionFilter
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Date fromDate;
    private Date toDate;
    private String user;
    private String entityName;
    private String memberVariableName;

    // Constructors --------------------------------------------------------------------------------

    /**
     * "null" has an "all" semantics here.
     */
    public TransactionFilter(Date fromDate, Date toDate, String user,
                             String entityName, String memberVariableName)
    {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.user = user;
        this.entityName = entityName;
        this.memberVariableName = memberVariableName;
    }

    // Public --------------------------------------------------------------------------------------

    public Date getFromDate()
    {
        return fromDate;
    }

    public Date getToDate()
    {
        return toDate;
    }

    public String getUser()
    {
        return user;
    }

    public String getEntityName()
    {
        return entityName;
    }

    public String getMemberVariableName()
    {
        return memberVariableName;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
