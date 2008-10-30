package com.googlecode.hibernate.audit.test.performance.util;

import java.util.Random;
import java.util.Date;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Util
{
    // Constants -----------------------------------------------------------------------------------

    private static Random random = new Random(System.currentTimeMillis());

    // Static --------------------------------------------------------------------------------------

    public static String randomString(int stringLength)
    {
        String s = "";

        for(int j = 0; j < stringLength; j ++)
        {
            s += (char)(65 + random.nextInt(26));
        }

        return s;
    }

    /**
     * @return a random in in [min, max].
     */
    public static Integer randomInteger(int min, int max)
    {
        if (min < 0 || max < 0)
        {
            throw new IllegalArgumentException("only positive ints");
        }

        if (min > max)
        {
            throw new IllegalArgumentException("first argument must be smaller than the second");
        }

        return min + random.nextInt(max - min + 1);
    }

    public static Long randomLong(int min, int max)
    {
        if (min < 0 || max < 0)
        {
            throw new IllegalArgumentException("only positive longs");
        }

        if (min > max)
        {
            throw new IllegalArgumentException("first argument must be smaller than the second");
        }

        return new Long(min + random.nextInt(max - min + 1));
    }


    public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy");

    public static Date randomDate(String from, String to) throws Exception
    {
        long min = DATE_FORMAT.parse(from).getTime();
        long max = DATE_FORMAT.parse(to).getTime();

        if (min > max)
        {
            throw new IllegalArgumentException(from + " must precede " + to);
        }

        long span = max - min;

        long val = random.nextLong();

        if (val < 0)
        {
            val = -val;
        }

        long x = val / span;
        return new Date(val - x * span);
    }

    public static Random getRandom()
    {
        return random;
    }

    public static void fillPrimitives(Object o) throws Exception
    {
        Class c = o.getClass();

        for(Method m: c.getMethods())
        {
            String name = m.getName();

            if (!name.startsWith("set"))
            {
                continue;
            }

            name = name.substring(4);

            try
            {
                Integer.parseInt(name);
            }
            catch(Exception e)
            {
                // not a primitive setter
                continue;
            }

            Class[] paramTypes = m.getParameterTypes();

            if (paramTypes.length != 1)
            {
                continue;
            }

            Class paramType = paramTypes[0];

            if (Integer.class.equals(paramType))
            {
                m.invoke(o, Util.randomInteger(10, 99));
            }
            else if (Long.class.equals(paramType))
            {
                m.invoke(o, Util.randomLong(1000, 9999));
            }
            else if (String.class.equals(paramType))
            {
                m.invoke(o, Util.randomString(20));
            }
            else if (Date.class.equals(paramType))
            {
                m.invoke(o, Util.randomDate("02/01/1072", "10/09/2008"));
            }
            else if (Boolean.class.equals(paramType))
            {
                m.invoke(o, random.nextBoolean());
            }
            else
            {
                throw new IllegalStateException(paramType +
                                                " not an expected primitive setter argument");
            }
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
