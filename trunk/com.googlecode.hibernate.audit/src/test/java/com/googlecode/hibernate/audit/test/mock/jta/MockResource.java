package com.googlecode.hibernate.audit.test.mock.jta;

import org.apache.log4j.Logger;

import javax.transaction.xa.XAException;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.Xid;
import javax.transaction.Status;

/**
 * Represents a resource enrolled with a transaction.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockResource
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(MockResource.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private ResourceState state;
    private XAResource xaResource;
    private MockResource sameResourceManager;
    private Xid branchXid;
    private MockJTATransaction owner;

    // Constructors --------------------------------------------------------------------------------

    /**
     *  @param xaResource - the corresponding XA resource. It is assumed that the resource is not
     *         already in the table of XA resources.
     *
     *  @param branchXid - the Xid for the transaction branch that is to be used for associating
     *         with this resource.
     *
     *  @param sameResourceManager - the resource of the first XA resource having the same resource
     *         manager as xaResource, or null if xaResource is the first resource seen with this
     */
   public MockResource(MockJTATransaction owner, XAResource xaResource,
                       Xid branchXid, MockResource sameResourceManager)
   {
       this.owner = owner;
       this.xaResource = xaResource;

       if (sameResourceManager != null)
       {
           this.sameResourceManager = sameResourceManager;
           throw new RuntimeException("NOT YET IMPLEMENTED");
       }

       this.branchXid = branchXid;
       this.state = ResourceState.NEW;
   }

    // Public --------------------------------------------------------------------------------------

    public void end() throws XAException
    {
        if (ResourceState.ENLISTED.equals(state) || ResourceState.SUSPENDED.equals((state)))
        {
            end(XAResource.TMSUCCESS);
        }
    }

    public void commit(boolean onePhase) throws XAException
    {
        if (!onePhase &&! ResourceState.VOTE_OK.equals(state))
        {
            // voted read-only at prepare phase.
            return;
        }

        if (sameResourceManager != null)
        {
            // this resource manager already committed
              return;
        }

        xaResource.commit(branchXid, onePhase);
    }

    public void rollback() throws XAException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public XAResource getXAResource()
    {
        return xaResource;
    }

    public boolean isEnlisted()
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean isDelisted(XAResource xaResource)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public boolean startResource() throws XAException
    {
        int flags = XAResource.TMJOIN;

        if (sameResourceManager == null)
        {
            if (ResourceState.NEW.equals(state))
            {
                flags = XAResource.TMNOFLAGS;
            }
            else if (ResourceState.SUSPENDED.equals(state))
            {
                flags = XAResource.TMRESUME;
            }
        }

        try
        {
            xaResource.start(branchXid, flags);
        }
        catch(XAException e)
        {
            throw e;
        }
        catch (Throwable t)
        {
            log.warn("unspecified XA resource start failure", t);
            owner.setStatus(Status.STATUS_MARKED_ROLLBACK);
            return false;
        }

        state = ResourceState.ENLISTED;
        return true;
    }

    public boolean isResourceManager(XAResource xaResource)
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    private void end(int flag) throws XAException
    {
        try
        {
            xaResource.end(branchXid, flag);
        }
        catch(XAException e)
        {
            throw e;
        }
        catch (Throwable t)
        {
            log.error("attempt to end resource ended in unexpected failure", t);
            owner.setStatus(Status.STATUS_MARKED_ROLLBACK);

            // resource may or may not be ended after illegal exception. We just assume it ended.
            state = ResourceState.ENDED;
            return;
        }

        if (flag == XAResource.TMSUSPEND)
        {
            state = ResourceState.SUSPENDED;
        }
        else
        {
            if (flag == XAResource.TMFAIL)
            {
                owner.setStatus(Status.STATUS_MARKED_ROLLBACK);
            }
            state = ResourceState.ENDED;
        }
    }

    // Inner classes -------------------------------------------------------------------------------
}
