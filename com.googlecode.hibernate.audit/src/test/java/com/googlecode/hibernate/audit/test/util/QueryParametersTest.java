package com.googlecode.hibernate.audit.test.util;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import com.googlecode.hibernate.audit.util.QueryParameters;

import java.util.List;
import java.util.Date;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
@Test(sequential = true)
public class QueryParametersTest
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(QueryParametersTest.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testFillEmptyList() throws Exception
    {
        Query q = MockQuery.newInstance();
        QueryParameters.fill(q);

        // make sure nothing bothered the query
        List<InvocationRecord> history = ((InvocationHistory)q).getHistory();
        assert history.isEmpty();
    }

    @Test(enabled = true)
    public void testNoMutator() throws Exception
    {
        Query q = MockQuery.newInstance();

        try
        {
            QueryParameters.fill(q, new RandomType());
            throw new Error("Should've failed");
        }
        catch(IllegalStateException e)
        {
            log.debug(e.getMessage());
        }

        // make sure nothing bothered the query
        List<InvocationRecord> history = ((InvocationHistory)q).getHistory();
        assert history.isEmpty();
    }

    @Test(enabled = true)
    public void testFillDate() throws Exception
    {
        Query q = MockQuery.newInstance();
        QueryParameters.fill(q, new Date(111));

        List<InvocationRecord> history = ((InvocationHistory)q).getHistory();
        assert history.size() == 1;

        InvocationRecord r = history.get(0);

        assert "setDate".equals(r.getMethodName());
        Object[] args = r.getArguments();

        assert args.length == 2;

        assert new Integer(0).equals(args[0]);
        assert new Date(111).equals(args[1]);
    }

    @Test(enabled = true)
    public void testFillTwoDates() throws Exception
    {
        Query q = MockQuery.newInstance();
        QueryParameters.fill(q, new Date(111), new Date(222));

        List<InvocationRecord> history = ((InvocationHistory)q).getHistory();
        assert history.size() == 2;

        InvocationRecord r = history.get(0);

        assert "setDate".equals(r.getMethodName());
        Object[] args = r.getArguments();

        assert args.length == 2;

        assert new Integer(0).equals(args[0]);
        assert new Date(111).equals(args[1]);

        r = history.get(1);

        assert "setDate".equals(r.getMethodName());
        args = r.getArguments();

        assert args.length == 2;

        assert new Integer(1).equals(args[0]);
        assert new Date(222).equals(args[1]);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
