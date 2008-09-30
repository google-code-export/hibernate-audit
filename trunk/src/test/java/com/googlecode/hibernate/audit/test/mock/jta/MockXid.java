package com.googlecode.hibernate.audit.test.mock.jta;

import javax.transaction.xa.Xid;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockXid implements Xid
{
    // Constants -----------------------------------------------------------------------------------

    public static final int MOCK_FORMAT_ID = 0xbeef;

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private int formatId;
    private byte[] globalTransactionId;
    private byte[] branchId;

    private long localId;

    // Constructors --------------------------------------------------------------------------------

    public MockXid(byte[] globalTransactionId, byte[] branchId, long localId)
    {
        this.formatId = MOCK_FORMAT_ID;
        this.globalTransactionId = globalTransactionId;
        this.branchId = branchId;
        this.localId = localId;
    }

    public MockXid(Xid base, byte[] branchId)
    {
        this.formatId = base.getFormatId();
        this.globalTransactionId = base.getGlobalTransactionId();
        this.branchId = branchId;

        if (!(base instanceof MockXid))
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        MockXid mockBase = (MockXid)base;

        this.localId = mockBase.getLocalId();
    }

    // Xid implementation --------------------------------------------------------------------------

    public int getFormatId()
    {
        return formatId;
    }

    public byte[] getGlobalTransactionId()
    {
        return globalTransactionId;
    }

    public byte[] getBranchQualifier()
    {
        return branchId;
    }

    // Public --------------------------------------------------------------------------------------

    public long getLocalId()
    {
        return localId;
    }

    @Override
    public String toString()
    {
        return "MockXid[" + new String(globalTransactionId) + ":" +
               (branchId == XidFactory.NO_BRANCH ? "main" : "branch " + new String(branchId)) + "]";

    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
