package com.googlecode.hibernate.audit.util.packinsp;

import org.apache.log4j.Logger;

import java.util.Set;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.net.URL;
import java.io.File;
import java.io.IOException;
import java.io.FilenameFilter;

/**
 * A generic mechanism to extract a list of classes present in a specific package and that fulfill
 * certain conditions.
 * 
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$Revision$</tt>
 *
 * $Id$
 */
public class PackageInspector
{
    // Constants -----------------------------------------------------------------------------------

    private static Logger log = Logger.getLogger(PackageInspector.class);

    // Static --------------------------------------------------------------------------------------

    // Attributes ----------------------------------------------------------------------------------

    private String packageName;
    private File base;
    private String relativePath;

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param representative the class to be used to identify the package.
     */
    public PackageInspector(Class representative) throws IOException
    {
        packageName = representative.getName();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));

        ProtectionDomain pd = representative.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL url = cs.getLocation();
        String protocol = url.getProtocol();

        if (!"file".equals(protocol))
        {
            throw new IOException("don't know to inspect the package with URL " + url);
        }

        base = new File(url.getFile());
        relativePath = packageName.replace('.', '/') + "/";
    }

    // Public --------------------------------------------------------------------------------------

    public String getPackageName()
    {
        return packageName;
    }

    /**
     * @param filter the instance that tells whether a specific class will be added or not to the
     *        final inspection result.
     *
     *        inspect() can be used for lateral effects as well, provided that the filter instance
     *        is crafted as such. In this case, filter instance can always return 'false' and
     *        inspect() will return an empty set.
     *
     * @throws Exception
     */
    public Set<Class> inspect(Filter filter) throws Exception
    {
        Updater updater = new Updater(filter);
        updater.scan();
        return updater.getScanResult();
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------

    private class Updater implements FilenameFilter
    {
        private Exception e;
        private Set<Class> result;
        private Filter filter;

        Updater(Filter filter)
        {
            this.filter = filter;
            result = new HashSet<Class>();
        }

        public boolean accept(File dir, String name)
        {
            if (e != null)
            {
                return false;
            }

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

                if (filter.accept(c))
                {
                    result.add(c);
                }

                return false;
            }
            catch(Exception e)
            {
                // trouble loading the class
                log.error("cannot load " + fullyQualifiedClassName, e);

                this.e = e;

                return false;
            }
        }

        void scan() throws IOException
        {
            if (base.isDirectory())
            {
                new File(base, relativePath).listFiles(this);
            }
            else
            {
                // we only know to handle jar files so far, not that we would ever need something
                // else, presumably

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

                    accept(null, name);
                }
            }
        }

        Set<Class> getScanResult() throws Exception
        {
            if (e != null)
            {
                throw e;
            }

            return result;
        }
    }
}
