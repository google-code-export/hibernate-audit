package com.googlecode.hibernate.audit.test.util.packinsp;

import org.testng.annotations.Test;
import com.googlecode.hibernate.audit.util.packinsp.PackageInspector;
import com.googlecode.hibernate.audit.util.packinsp.Filter;
import com.googlecode.hibernate.audit.test.util.data.A;
import com.googlecode.hibernate.audit.test.util.data.B;
import com.googlecode.hibernate.audit.test.util.data.C;

import java.util.Set;

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
public class PackageInspectorTest
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    @Test(enabled = true)
    public void testItself() throws Exception
    {
        PackageInspector pi = new PackageInspector(getClass());
        Set<Class> result = pi.inspect(new Filter()
        {
            public boolean accept(Class c)
            {
                // accepts everything
                return true;
            }
        });

        assert !result.isEmpty();

        for(Class c: result)
        {
            // it's either anonymous or PackageInspectorTest itself
            assert PackageInspectorTest.class.equals(c) || c.getName().contains(".PackageInspectorTest$");
        }
    }

    @Test(enabled = true)
    public void testFilter() throws Exception
    {
        PackageInspector pi = new PackageInspector(A.class);
        Set<Class> result = pi.inspect(new Filter()
        {
            public boolean accept(Class c)
            {
                String name = c.getName();
                name = name.substring(name.lastIndexOf('.') + 1);

                if ("A".equals(name) ||
                    "B".equals(name) ||
                    "C".equals(name))
                {
                    return true;
                }

                return false;
            }
        });

        assert result.size() == 3;
        assert result.contains(A.class);
        assert result.contains(B.class);
        assert result.contains(C.class);
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

}
