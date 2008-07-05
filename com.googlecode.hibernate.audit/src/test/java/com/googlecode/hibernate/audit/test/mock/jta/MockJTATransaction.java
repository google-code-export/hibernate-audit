package com.googlecode.hibernate.audit.test.mock.jta;


import org.apache.log4j.Logger;

import javax.transaction.Status;
import javax.transaction.Transaction;
import javax.transaction.RollbackException;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.SystemException;
import javax.transaction.Synchronization;
import javax.transaction.xa.XAResource;
import javax.transaction.xa.XAException;
import javax.transaction.xa.Xid;
import java.util.List;
import java.util.ArrayList;

/**
 * Based on org.jboss.tm.TransactionImpl.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class MockJTATransaction implements Transaction
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(MockJTATransaction.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private int status;

    private List<Synchronization> synchronizations;

    private List<MockResource> resources;
    private boolean resourcesEnded;
    private MockResource lastResource;

    private Xid xid;
    private long lastBranchId;

    // Constructors --------------------------------------------------------------------------------

    public MockJTATransaction()
    {
        status = Status.STATUS_ACTIVE;
        synchronizations = new ArrayList<Synchronization>();
        resources = new ArrayList<MockResource>();
        xid = XidFactory.createNewXid();
        lastBranchId = 0;
    }

    // Transaction implementation ------------------------------------------------------------------

    /**
     * From Java Transaction API Specifications, Version 1.1, pag. 34.
     *
     * Complete the transaction associated with the target Transaction object.
     *
     * @exception RollbackException Thrown to indicate that the transaction has been rolled back
     *            rather than committed.
     * @exception HeuristicMixedException Thrown to indicate that a heuristic decision was made and
     *            that some relevant updates have been committed while others have been rolled back.
     * @exception HeuristicRollbackException Thrown to indicate that a heuristic decision was made
     *            and that all relevant updates have been rolled back.
     * @exception SecurityException Thrown to indicate that the thread is not allowed to commit the
     *            transaction.
     * @exception IllegalStateException Thrown if the transaction in the target object is inactive.
     * @exception SystemException Thrown if the transaction manager encounters an unexpected error
     *            condition.
     */
    public void commit()
        throws RollbackException, HeuristicMixedException, HeuristicRollbackException,
               SecurityException, IllegalStateException, SystemException
    {
        if (status != Status.STATUS_ACTIVE)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        // call beforeCompletion() on all synchronizations

        for(Synchronization s: synchronizations)
        {
            try
            {
                s.beforeCompletion();
            }
            catch (Throwable t)
            {
                log.error("beforeCompletion() failed on " + s, t);

                status = Status.STATUS_MARKED_ROLLBACK;
                break;
            }
        }

        // end transaction association for all resources

        for(MockResource r: resources)
        {
            try
            {
                r.end();
            }
            catch(XAException e)
            {
                log.error("end() failed on " + r, e);

                status = Status.STATUS_MARKED_ROLLBACK;
            }
        }

        resourcesEnded = true;

        if (status == Status.STATUS_ACTIVE)
        {
            // we're good, commit

            int resourceCount = resources.size();

            if (resourceCount == 0)
            {
                // nothing to do
                status = Status.STATUS_COMMITTED;
            }
            else if (resourceCount == 1)
            {
                // one phase commit, skip prepare and just commit
                commitEnlistedResources(true);
            }
            else
            {
                // more than one resource, two phase commit
                // prepare and commit
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }
        }

        if (status != Status.STATUS_COMMITTED)
        {
            // rollback resources

            status = Status.STATUS_ROLLING_BACK;

            for (MockResource r: resources)
            {
               try
               {
                  r.rollback();
               }
               catch (XAException e)
               {
                   log.error("rollback() generated an XAException on " + r, e);

                   int xaError = e.errorCode;

                   if (xaError == XAException.XA_HEURRB ||
                       xaError == XAException.XA_HEURCOM ||
                       xaError == XAException.XA_HEURMIX ||
                       xaError == XAException.XA_HEURHAZ)
                   {
                       throw new RuntimeException("NOT YET IMPLEMENTED");
                   }

                   // otherwise, error is logged, go on
               }
               catch (Throwable t)
               {
                   log.error("rollback() failed an XAException on " + r, t);
               }
            }

            status = Status.STATUS_ROLLEDBACK;
        }

        // call afterCompletion() on all synchronizations

        for(Synchronization s: synchronizations)
        {
            try
            {
                s.afterCompletion(status);
            }
            catch (Throwable t)
            {
                log.warn("afterCompletion() failed on " + s, t);
            }
        }

        if (status != Status.STATUS_COMMITTED)
        {
            throw new RollbackException("transaction failed with status " +
                                        JTAUtil.statusToString(status));
        }

        status = Status.STATUS_NO_TRANSACTION;

    }

    public boolean delistResource(XAResource xaResource, int i)
        throws IllegalStateException, SystemException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    /**
     * From Java Transaction API Specifications, Version 1.1, pag. 35.
     *
     * Enlist the resource specified with the transaction associated with the target Transaction
     * object.
     *
     * @param xaResource - The XAResource object associated with the resource (connection).
     *
     * @return true if the enlistment is successful; otherwise false.
     *
     * @throws RollbackException Thrown to indicate that the transaction has been marked for
     *         rollback only.
     * @throws IllegalStateException Thrown if the transaction in the target object is in prepared
     *         state or the transaction is inactive.
     * @throws SystemException Thrown if the transaction manager encounters an unexpected error
     *         condition.
     */
    public boolean enlistResource(XAResource xaResource)
        throws RollbackException, IllegalStateException, SystemException
    {
        if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            throw new RollbackException(this + " already marked for rollback");
        }

        if (status == Status.STATUS_ROLLING_BACK)
        {
            throw new RollbackException(this + " rolling back");
        }

        if (status == Status.STATUS_ROLLEDBACK)
        {
            throw new RollbackException(this + " already rolled back");
        }

        if (status == Status.STATUS_PREPARED)
        {
            throw new IllegalStateException(this + " is in prepared state");
        }

        if (status != Status.STATUS_ACTIVE && status != Status.STATUS_PREPARING)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        if (resourcesEnded)
        {
            throw new IllegalStateException("all resources have been ended already on " + this);
        }

        MockResource resource = findResource(xaResource);

        try
        {
            if (resource != null)
            {
                if (resource.isEnlisted())
                {
                    return true;
                }

                if (!resource.isDelisted(xaResource))
                {
                    return resource.startResource();
                }

                // this is a resource that returns false on all calls to isSameRM(). Further, the last
                // resource enlisted has already been delisted, so it is time to enlist it again ...
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            resource = findResourceManager(xaResource);

            if (resource != null)
            {
                // The xaResource is new. We register the xaResource with the Xid that the resource
                // manager has previously seen from this transaction, and note that it has the same
                // resource manager
                throw new RuntimeException("NOT YET IMPLEMENTED");
            }

            // new resource and new resource manager, create a new transaction branch.
            resource =
                addResource(xaResource, XidFactory.createNewBranch(xid, lastBranchId ++), null);

            return resource.startResource();
        }
        catch(XAException e)
        {
            log.error("enlisting " + xaResource + " with " + this + " failed", e);
            return false;
        }
    }

    public int getStatus() throws SystemException
    {
        return status;
    }

    /**
     * From Java Transaction API Specifications, Version 1.1, pag. 35.
     *
     * Register a synchronization object for the transaction currently associated with the target
     * object. The transction manager invokes the beforeCompletion method prior to starting the
     * two-phase transaction commit process. After the transaction is completed, the transaction
     * manager invokes the afterCompletion method.
     *
     * @exception IllegalStateException Thrown if the transaction in the target object is in
     *            prepared state or the transaction is inactive.
     * @exception RollbackException Thrown to indicate that the transaction has been marked for
     *            rollback only.
     * @exception SystemException Thrown if the transaction manager encounters an unexpected error
     *            condition.
     */
    public synchronized void registerSynchronization(Synchronization synchronization)
        throws RollbackException, IllegalStateException, SystemException
    {
        if (status == Status.STATUS_MARKED_ROLLBACK)
        {
            throw new RollbackException("transaction marked for rollback");
        }

        if (status != Status.STATUS_ACTIVE &&
            status != Status.STATUS_PREPARING)
        {
            throw new RuntimeException("NOT YET IMPLEMENTED");
        }

        synchronizations.add(synchronization);
    }

    public void rollback() throws IllegalStateException, SystemException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    public void setRollbackOnly() throws IllegalStateException, SystemException
    {
        throw new RuntimeException("NOT YET IMPLEMENTED");
    }

    // Public --------------------------------------------------------------------------------------

    @Override
    public String toString()
    {
        return "MockJTATransaction[" + Integer.toHexString(System.identityHashCode(this)) + "]" +
               "(" + JTAUtil.statusToString(status) + ")";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    /**
     * For use of friends who know what they're doing.
     */
    void setStatus(int status)
    {
        this.status = status;
    }

    // Private -------------------------------------------------------------------------------------

    /**
     * Return the corresponding resource representation given a XAResource instance, or null if
     * there is no such representation.
     *
     */
    private MockResource findResource(XAResource xaResource)
    {
        // Search backwards to ensure that if a resource was enlisted multiple times, then the last
        // one will be returned. All others should be in the state RS_ENDED. This allows
        // ResourceManagers that always return false from isSameRM to be enlisted and delisted
        // multiple times.
       for (int i = resources.size() - 1; i >= 0; i--)
       {
          MockResource r = resources.get(i);
          if (xaResource == r.getXAResource())
          {
              return r;
          }
       }

       return null;
    }

    private MockResource findResourceManager(XAResource xaResource) throws XAException
    {
        for(MockResource r: resources)
        {
            if (r.isResourceManager(xaResource))
            {
               return r;
            }
        }

       return null;
    }

    /**
     *  @param xaResource - the new XA resource to add. It is assumed that the resource is not
     *         already in the table of XA resources.
     *
     *  @param branchXid - the Xid for the transaction branch that is to be used for associating
     *         with this resource.
     *
     *  @param sameRMResource - the resource of the first XA resource having the same resource
     *         manager as xaResource, or null if xaResource is the first resource seen with this
     */
    private MockResource addResource(XAResource xaResource,
                                     Xid branchXid,
                                     MockResource sameRMResource)
    {
       MockResource resource = new MockResource(this, xaResource, branchXid, sameRMResource);
       resources.add(resource);

       // remember the first resource that wants the last resource gambit
       if (lastResource == null && xaResource instanceof LastResource)
       {
          lastResource = resource;
       }

       return resource;
    }

    private void commitEnlistedResources(boolean onePhase)
    {
        status = Status.STATUS_COMMITTING;

        for(MockResource r: resources)
        {
            if (status != Status.STATUS_COMMITTING)
            {
                // abort on state change
                return;
            }

            if (!onePhase && r == lastResource)
            {
                // ignore the last resource, it is already committed
                continue;
            }

            try
            {
                r.commit(onePhase);
            }
            catch (XAException e)
            {
                log.error(r + " failed to commit", e);

                if (e.errorCode == XAException.XA_HEURRB ||
                    e.errorCode == XAException.XA_HEURCOM ||
                    e.errorCode == XAException.XA_HEURMIX ||
                    e.errorCode == XAException.XA_HEURHAZ)
                {
                    throw new RuntimeException("NOT YET IMPLEMENTED");
                }
                else
                {
                    if (onePhase)
                    {
                        status = Status.STATUS_MARKED_ROLLBACK;
                        break;
                    }

                    // not much we can do if there is an RMERR in the commit phase of 2PC. Try
                    // the other resources.
                }
            }
            catch (Throwable t)
            {
                log.error("unexpected error while committing " + r, t);
            }
        }

        if (status == Status.STATUS_COMMITTING)
        {
            status = Status.STATUS_COMMITTED;
        }
    }

    // Inner classes -------------------------------------------------------------------------------

}
