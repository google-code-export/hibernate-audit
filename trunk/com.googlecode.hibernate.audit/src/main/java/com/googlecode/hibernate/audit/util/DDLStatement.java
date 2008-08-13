package com.googlecode.hibernate.audit.util;

import org.apache.log4j.Logger;

/**
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class DDLStatement
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(DDLStatement.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private StringBuffer sb;
    private DDLAction action;
    private DDLType targetType;
    private String targetName;

    // Constructors --------------------------------------------------------------------------------

    public DDLStatement()
    {
        sb = new StringBuffer();
    }

    // Public --------------------------------------------------------------------------------------

    public String getString()
    {
        return sb.toString();
    }

    public DDLAction getAction()
    {
        return action;
    }

    public DDLType getTargetType()
    {
        return targetType;
    }

    public String getTargetName()
    {
        return targetName;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    /**
     * 'Post-processes' the statement string representation by removing unwanted characters,
     * removing spaces, validating, etc.
     */
    protected void postProcess() throws Exception
    {
        // get rid of the latest space
        if (sb.charAt(sb.length() - 1) == ' ')
        {
            sb.replace(sb.length() - 1, sb.length(), "");
        }

        int i = sb.indexOf(";");

        if (i != -1)
        {
            sb.replace(i, i + 1, "");
        }

        String s = sb.toString().trim();
        i = s.indexOf(" ");
        String actionString = s.substring(0, i).toUpperCase();
        action = DDLAction.valueOf(actionString);

        s = s.substring(i).trim();
        i  = s.indexOf(" ");
        String targetTypeString = s.substring(0, i).toUpperCase();
        targetType = DDLType.valueOf(targetTypeString);

        s = s.substring(i).trim();
        i = s.indexOf(" ");
        if (i != -1)
        {
            targetName = s.substring(0, i);
        }
        else
        {
            targetName = s;
        }

        targetName = targetName.toUpperCase();

        log.debug("identified statement " + sb.toString());
    }

    protected void append(String s)
    {
        sb.append(s).append(' ');
    }

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
