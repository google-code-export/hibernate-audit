package com.googlecode.hibernate.audit.test.util;

import org.apache.log4j.Logger;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.Map;
import java.util.HashMap;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$</tt>
 *
 */
public class RendezVous
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(RendezVous.class);

    // Static --------------------------------------------------------------------------------------

    private static final Control GO = new Control();

    // Attributes ----------------------------------------------------------------------------------

    private String threadOneName;
    private String threadTwoName;

    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private BlockingQueue<Control> enabler1;
    
    @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
    private BlockingQueue<Control> enabler2;

    private CountDownLatch end;

    // <thread name - Throwable>
    private Map<String, Throwable> throwables;

    // Constructors --------------------------------------------------------------------------------

    /**
     * Thread One starts first.
     */
    public RendezVous(String threadOneName, String threadTwoName)
    {
        this.threadOneName = threadOneName;
        this.threadTwoName = threadTwoName;
        enabler1 = new ArrayBlockingQueue<Control>(1);
        enabler2 = new ArrayBlockingQueue<Control>(1);
        end = new CountDownLatch(2);

        throwables = new HashMap<String, Throwable>();
    }

    // Public --------------------------------------------------------------------------------------

    public void begin() throws InterruptedException
    {
        String threadName = checkThread();

        if (threadOneName.equals(threadName))
        {
            // just go
        }
        else
        {
            // wait
            enabler2.take();
        }
    }

    public void swapControl() throws InterruptedException
    {
        String threadName = checkThread();

        synchronized(this)
        {
            if (!throwables.isEmpty())
            {
                enabler1.offer(GO);
                enabler2.offer(GO);
                return;
            }
        }

        if (threadOneName.equals(threadName))
        {
            enabler2.put(GO);
            enabler1.take();
        }
        else
        {
            enabler1.put(GO);
            enabler2.take();
        }
    }

    public void end() throws InterruptedException
    {
        String threadName = checkThread();

        try
        {
            synchronized(this)
            {
                if (!throwables.isEmpty())
                {
                    enabler1.offer(GO);
                    enabler2.offer(GO);
                    return;
                }
            }

            if (threadOneName.equals(threadName))
            {
                enabler2.put(GO);
            }
            else
            {
                enabler1.put(GO);
            }
        }
        finally
        {
            end.countDown();
        }
    }

    public void abort(Throwable t)
    {
        String threadName = checkThread();

        log.debug("'" + threadName + "' aborting with " + t);

        try
        {
            synchronized(this)
            {
                throwables.put(threadName, t);
            }

            enabler1.offer(GO);
            enabler2.offer(GO);
        }
        finally
        {
            end.countDown();
        }
    }

    /**
     * Waits for both threads to complete their work. If at least one thread exited with exception,
     * the exception will be available via getException(String).
     *
     * @throws InterruptedException
     */
    public void awaitEnd() throws InterruptedException
    {
        end.await();
    }

    /**
     * @return the throwable trhown by the thread, or null otherwise.
     */
    public Throwable getThrowable(String threadName)
    {
        synchronized(this)
        {
            return throwables.get(threadName);
        }
    }

    @Override
    public String toString()
    {
        return "RendezVous[" + Integer.toHexString(System.identityHashCode(this)) + "]";
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    /**
     * @return current thread name, if cleared.
     * @exception IllegalStateException if the current thread is not supposed to touch this
     *            RendezVous instance.
     *
     */
    private String checkThread()
    {
        String name = Thread.currentThread().getName();

        if (threadOneName.equals(name) || threadTwoName.equals(name))
        {
            return name;
        }

        throw new IllegalStateException(Thread.currentThread() + " is not supposed to touch " + this);
    }

    // Inner classes -------------------------------------------------------------------------------

    private static class Control
    {
    }
}
