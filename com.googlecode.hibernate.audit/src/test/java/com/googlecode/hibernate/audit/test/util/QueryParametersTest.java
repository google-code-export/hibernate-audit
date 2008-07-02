package com.googlecode.hibernate.audit.test.util;

import org.testng.annotations.Test;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import com.googlecode.hibernate.audit.util.QueryParameters;
import com.googlecode.hibernate.audit.util.QueryParameter;

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
        Query q = MockQuery.newInstance("doesn't matter");
        QueryParameters.fill(q);

        // make sure nothing bothered the query
        List<InvocationRecord> history = ((InvocationHistory)q).getHistory();
        assert history.isEmpty();
    }

    @Test(enabled = true)
    public void testNoMutator() throws Exception
    {
        Query q = MockQuery.newInstance("doesn't matter");

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
    public void testFillDatePositional() throws Exception
    {
        Query q = MockQuery.newInstance("something ? somethin else");
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
    public void testFillDateNamed() throws Exception
    {
        Query q = MockQuery.newInstance("something :a somethin else");
        QueryParameters.fill(q, new Date(111));

        List<InvocationRecord> history = ((InvocationHistory)q).getHistory();
        assert history.size() == 1;

        InvocationRecord r = history.get(0);

        assert "setDate".equals(r.getMethodName());
        Object[] args = r.getArguments();

        assert args.length == 2;

        assert "a".equals(args[0]);
        assert new Date(111).equals(args[1]);
    }

    @Test(enabled = true)
    public void testFillTwoDatesPositional() throws Exception
    {
        Query q = MockQuery.newInstance("something ? somethin else ?");
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

    @Test(enabled = true)
    public void testFillTwoDatesNamed() throws Exception
    {
        Query q = MockQuery.newInstance("something :a somethin else :b");
        QueryParameters.fill(q, new Date(111), new Date(222));

        List<InvocationRecord> history = ((InvocationHistory)q).getHistory();
        assert history.size() == 2;

        InvocationRecord r = history.get(0);

        assert "setDate".equals(r.getMethodName());
        Object[] args = r.getArguments();

        assert args.length == 2;

        assert "a".equals(args[0]);
        assert new Date(111).equals(args[1]);

        r = history.get(1);

        assert "setDate".equals(r.getMethodName());
        args = r.getArguments();

        assert args.length == 2;

        assert "b".equals(args[0]);
        assert new Date(222).equals(args[1]);
    }

    @Test(enabled = true)
    public void testExtractParametersEmtpyList() throws Exception
    {
        List<QueryParameter> ps = QueryParameters.extractParameters("");
        assert ps.isEmpty();
    }

    @Test(enabled = true)
    public void testExtractParametersNoParameters() throws Exception
    {
        List<QueryParameter> ps = QueryParameters.extractParameters("something without pars");
        assert ps.isEmpty();
    }

    @Test(enabled = true)
    public void testExtractParametersNamedParameters() throws Exception
    {
        List<QueryParameter> ps = QueryParameters.extractParameters("something :a with :b pars :c");

        assert ps.size() == 3;

        QueryParameter p = ps.get(0);

        assert p.isNamed();
        assert "a".equals(p.getName());
        assert 0 == p.getPosition();

        p = ps.get(1);

        assert p.isNamed();
        assert "b".equals(p.getName());
        assert 1 == p.getPosition();

        p = ps.get(2);

        assert p.isNamed();
        assert "c".equals(p.getName());
        assert 2 == p.getPosition();
    }

    @Test(enabled = true)
    public void testExtractParametersPositionalParameters() throws Exception
    {
        List<QueryParameter> ps = QueryParameters.extractParameters("something ? with ? pars ?");

        assert ps.size() == 3;

        QueryParameter p = ps.get(0);

        assert !p.isNamed();
        assert 0 == p.getPosition();

        p = ps.get(1);

        assert !p.isNamed();
        assert 1 == p.getPosition();

        p = ps.get(2);

        assert !p.isNamed();
        assert 2 == p.getPosition();
    }

    @Test(enabled = true)
    public void testExtractParametersMixedParameters() throws Exception
    {
        List<QueryParameter> ps = QueryParameters.
            extractParameters("something :a mixed with ? and :b");

        assert ps.size() == 3;

        QueryParameter p = ps.get(0);

        assert p.isNamed();
        assert "a".equals(p.getName());
        assert 0 == p.getPosition();

        p = ps.get(1);

        assert !p.isNamed();
        assert 1 == p.getPosition();

        p = ps.get(2);

        assert p.isNamed();
        assert "b".equals(p.getName());
        assert 2 == p.getPosition();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
