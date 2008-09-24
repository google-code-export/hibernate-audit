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
    private Long auditEntityTypeId;
    private Long auditTypeFieldId;

    // Constructors --------------------------------------------------------------------------------

    /**
     * "null" has an "all" semantics here.
     */
    public TransactionFilter(Date fromDate, Date toDate)
    {
        this(fromDate, toDate, null, (Long)null, (Long)null);
    }

    /**
     * "null" has an "all" semantics here.
     */
    public TransactionFilter(Date fromDate, Date toDate, String user,
                             Long auditEntityTypeId, Long auditTypeFieldId)
    {
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.user = user;
        this.auditEntityTypeId = auditEntityTypeId;
        this.auditTypeFieldId = auditTypeFieldId;
    }

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

    public Long getAuditEntityTypeId()
    {
        return auditEntityTypeId;
    }

    public Long getAuditTypeFieldId()
    {
        return auditTypeFieldId;
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
