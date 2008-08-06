package com.googlecode.hibernate.audit.model;

import org.apache.log4j.Logger;

import javax.persistence.Entity;
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
import java.lang.annotation.Annotation;

/**
 * Audit entities manipulation utility.
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class Entities
{
    // Constants -----------------------------------------------------------------------------------

    private static final Logger log = Logger.getLogger(Entities.class);

    private static final EntitySetUpdatingFilenameFilter entitySetUpdater =
        new EntitySetUpdatingFilenameFilter();

    // Static --------------------------------------------------------------------------------------

    private static String packageName;
    private static Set<Class> auditEntities;

    /**
     * @return all current audit entities.
     *
     * TODO implementation quasi-identical with Listeners.getAuditedEventTypes(). Could be consolidated.
     *
     * @exception Exception if anything goes wrong while rummaging through the filesystem.
     */
    public static Set<Class> getAuditEntities() throws Exception
    {
        synchronized(Entities.class)
        {
            if (auditEntities == null)
            {
                auditEntities = new HashSet<Class>();

                ProtectionDomain pd = Entities.class.getProtectionDomain();
                CodeSource cs = pd.getCodeSource();
                URL url = cs.getLocation();
                String protocol = url.getProtocol();

                if ("file".equals(protocol))
                {
                    File base = new File(url.getFile());

                    if (packageName == null)
                    {
                        packageName = Entities.class.getName();
                        packageName = packageName.substring(0, packageName.lastIndexOf("."));
                    }

                    String relativePath = packageName.replace('.', '/');
                    relativePath += "/";

                    if (base.isDirectory())
                    {
                        new File(base, relativePath).listFiles(entitySetUpdater);
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

                            entitySetUpdater.accept(null, name);
                        }
                    }
                }
                else
                {
                    throw new IllegalStateException("don't know how to handle " + url);
                }
            }

            return new HashSet<Class>(auditEntities);
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private static class EntitySetUpdatingFilenameFilter implements FilenameFilter
    {
        public boolean accept(File dir, String name)
        {
            int i = name.indexOf(".class");

            if (i == -1)
            {
                return false;
            }

            name = name.substring(0, i);
            String fullyQualifiedClassName = packageName + "." + name;

            try
            {
                Class c = Class.forName(fullyQualifiedClassName);
                for(Annotation a: c.getAnnotations())
                {
                    if (Entity.class.equals(a.annotationType()))
                    {
                        auditEntities.add(c);
                        return true;
                    }
                }

                return false;
            }
            catch(Exception e)
            {
                // trouble loading the class
                log.error("cannot find " + fullyQualifiedClassName, e);
                return false;
            }
        }
    }

}
