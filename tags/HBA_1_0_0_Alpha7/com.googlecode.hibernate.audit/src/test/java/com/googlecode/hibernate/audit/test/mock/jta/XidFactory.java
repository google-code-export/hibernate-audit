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
public class XidFactory
{
    // Constants -----------------------------------------------------------------------------------

    public static final byte[] NO_BRANCH = new byte[1];

    // Static --------------------------------------------------------------------------------------

    private static byte[] baseGlobalIdBytes;

    private static long nextLocalId = 0;

    static
    {
        baseGlobalIdBytes = "mock-transaction-0-".getBytes();
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    public synchronized static Xid createNewXid()
    {
        long localId;

        synchronized(XidFactory.class)
        {
            localId = nextLocalId ++;
        }

        byte[] localIdBytes = Long.toString(localId).getBytes();
        byte[] globalId = new byte[baseGlobalIdBytes.length + localIdBytes.length];
        System.arraycopy(baseGlobalIdBytes, 0, globalId, 0, baseGlobalIdBytes.length);
        System.arraycopy(localIdBytes, 0, globalId, baseGlobalIdBytes.length, localIdBytes.length);

        return new MockXid(globalId, NO_BRANCH, localId);
    }

    public static Xid createNewBranch(Xid base, long branchId)
    {
        String sbid = Long.toString(branchId);
        byte[] branchIdBytes = sbid.getBytes();
        return new MockXid(base, branchIdBytes);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
