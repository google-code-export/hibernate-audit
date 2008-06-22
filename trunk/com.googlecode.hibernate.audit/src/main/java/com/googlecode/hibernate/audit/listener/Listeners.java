package com.googlecode.hibernate.audit.listener;

import org.apache.log4j.Logger;

import java.util.Set;
import java.util.HashSet;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.net.URL;
import java.io.File;
import java.io.FilenameFilter;

/**
 * Event listener manipulation utilities.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Listeners
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(Listeners.class);

    // Static --------------------------------------------------------------------------------------

    private static Set<String> auditedEventTypes;

    private static final AuditedEventTypesUpdatingFilenameFilter auditedEventTypesUpdater =
        new AuditedEventTypesUpdatingFilenameFilter();

    /**
     * @return all audited event types, determined based on the static list of avaliable audit
     *         listeners. The string representation of an event type is the one used by
     *         org.hibernate.event.EventListeners to key the listeners in its eventInterfaceFromType
     *         map.
     *
     * @see org.hibernate.event.EventListeners
     *
     * @exception Exception if anything goes wrong while rummaging through the filesystem.
     *
     */
    public static Set<String> getAuditedEventTypes() throws Exception
    {
        synchronized(Listeners.class)
        {
            if (auditedEventTypes == null)
            {
                auditedEventTypes = new HashSet<String>();

                ProtectionDomain pd = Listeners.class.getProtectionDomain();
                CodeSource cs = pd.getCodeSource();
                URL url = cs.getLocation();
                String protocol = url.getProtocol();

                if ("file".equals(protocol))
                {
                    File base = new File(url.getFile());

                    String relativePath = Listeners.class.getName();
                    relativePath =
                        relativePath.substring(0, relativePath.lastIndexOf(".")).replace('.', '/');
                    relativePath += "/";

                    if (base.isDirectory())
                    {
                        new File(base, relativePath).listFiles(auditedEventTypesUpdater);
                    }
                    else
                    {
                        // we only know to handle jar files so far, not that we would ever need
                        // something else, presumably

                        JarFile jarFile = new JarFile(base);

                        log.debug("looking in " + jarFile.getName());

                        for(Enumeration<JarEntry> ents = jarFile.entries(); ents.hasMoreElements();)
                        {
                            JarEntry je = ents.nextElement();
                            String name = je.getName();
                            if (!name.startsWith(relativePath))
                            {
                                continue;
                            }
                            name = name.substring(relativePath.length());

                            if (name.indexOf("/") != -1)
                            {
                                // we don't look in subdirectories
                                continue;
                            }

                            auditedEventTypesUpdater.accept(null, name);
                        }
                    }
                }
                else
                {
                    throw new IllegalStateException("don't know how to handle " + url);
                }
            }

            return auditedEventTypes;
        }
    }

    /**
     * Gets something like "PostInsertAuditEventListener" and returns "post-insert".
     *
     * Returns null if no such name can be identified.
     */
    private static String classNameToHibernateEventType(String classname)
    {
        int i = classname.indexOf("AuditEventListener");

        if (i <= 0)
        {
            return null;
        }

        String s = classname.substring(0, i);
        StringBuffer sb = new StringBuffer();

        for(i = 0; i < s.length(); i++)
        {
            char c = s.charAt(i);
            
            if (c < 91)
            {
                if (i > 0)
                {
                    sb.append('-');
                }

                sb.append((char)(c + 32));
            }
            else
            {
                sb.append(c);
            }

        }

        return sb.toString();
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private static class AuditedEventTypesUpdatingFilenameFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            if (auditedEventTypes == null)
            {
                return false;
            }

            int i = name.indexOf(".class");

            if (i == -1)
            {
                return false;
            }

            String eventType = classNameToHibernateEventType(name.substring(0, i));
            if (eventType != null)
            {
                auditedEventTypes.add(eventType);
                return true;
            }

            return false;
        }
    }
}
