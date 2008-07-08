package com.googlecode.hibernate.audit.test.mock.jca;

import org.apache.log4j.Logger;

import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.xa.XAException;
import java.sql.Connection;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class JDBCConnectionXAResource implements XAResource
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(JDBCConnectionXAResource.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private Connection rawConnection;
    private Xid currentXid;

    // Constructors --------------------------------------------------------------------------------

    public JDBCConnectionXAResource(Connection rawConnection)
    {
        this.rawConnection = rawConnection;
    }

    // XAResource implementation -------------------------------------------------------------------

    public void commit(Xid xid, boolean b) throws XAException
    {
        if (!xid.equals(currentXid))
        {
            throw new XAException("wrong Xid in commit");
        }

        try
        {
            rawConnection.commit();
            currentXid = null;
        }
        catch (Throwable t)
        {
            log.error("cound not commit local transaction", t);
            throw new XAException("could not commit local transaction");
        }
    }

    /**
     * From Java Transaction API Specifications, Version 1.1, pag. 48.
     *
     * This method ends the work performed on behalf of a transaction branch. The resource manager
     * dissociates the XA resource from the transaction branch specified and let the transaction be
     * completed.
     *
     * If TMSUSPEND is specified in flags, the transaction branch is temporarily suspended in
     * incomplete state. The transaction context is in suspened state and must be resumed via start
     * with TMRESUME specified.
     *
     * If TMFAIL is specified, the portion of work has failed. The resource manager may mark the
     * transaction as rollback-only.
     *
     * If TMSUCCESS is specified, the portion of work has completed successfully.
     *
     * @param xid - A global transaction identifier that is the same as what was used previously in
     *        the start method.
     * @param flags - One of TMSUCCESS, TMFAIL, or TMSUSPEND.
     *
     * @throws XAException An error has occurred. Possible XAException values are XAER_RMERR,
     *         XAER_RMFAIL, XAER_NOTA, XAER_INVAL, XAER_PROTO, or XA_RB*.
     */
    public void end(Xid xid, int flags) throws XAException
    {
        if (flags != XAResource.TMSUCCESS)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        if (xid != currentXid)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        // ... otherwise, do nothing

    }

    public void forget(Xid xid) throws XAException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public int getTransactionTimeout() throws XAException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean isSameRM(XAResource xaResource) throws XAException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public int prepare(Xid xid) throws XAException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public Xid[] recover(int i) throws XAException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void rollback(Xid xid) throws XAException
    {
        if (!xid.equals(currentXid))
        {
            throw new XAException("wrong Xid in rollback");
        }

        currentXid = null;

        try
        {
            rawConnection.rollback();
        }
        catch (Throwable t)
        {
            log.error("cound not rollback local transaction", t);
            throw new XAException("cound not rollback local transaction");
        }
    }

    public boolean setTransactionTimeout(int i) throws XAException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    /**
     * From Java Transaction API Specifications, Version 1.1, pag. 51.
     *
     * This method starts work on behalf of a transaction branch.
     *
     * If TMJOIN is specified, the start is for joining an exisiting transaction branch xid.
     * If TMRESUME is specified, the start is to resume a suspended transaction branch specified in
     * xid. If neither TMJOIN nor TMRESUME is specified and the transaction branch specified in xid
     * already exists, the resource manager throw the XAException with XAER_DUPID error code.
     *
     * @param xid A global transaction identifier to be associated with the resource.
     * @param flags, one of TMNOFLAGS, TMJOIN, or TMRESUME.
     *
     * @throws XAException An error has occurred. Possible exceptions are XA_RB*, XAER_RMERR,
     * XAER_RMFAIL, XAER_DUPID, XAER_OUTSIDE, XAER_NOTA, XAER_INVAL, or XAER_PROTO.
     */
    public void start(Xid xid, int flags) throws XAException
    {
        if (currentXid != null && flags == XAResource.TMNOFLAGS)
        {
            throw new XAException("trying to start a new transaction when old is not complete");
        }

        if (currentXid  == null && flags != XAResource.TMNOFLAGS)
        {
            throw new XAException("trying to start a new transaction with wrong flags: " + flags);
        }

        if (currentXid == null)
        {
            // local transaction started by default on raw connection
            currentXid = xid;
        }

        log.debug(this + " started");
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "XAResource[" + rawConnection + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
