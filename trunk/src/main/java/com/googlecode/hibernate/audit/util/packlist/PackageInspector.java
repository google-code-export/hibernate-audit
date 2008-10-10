package com.googlecode.hibernate.audit.util.packlist;

import org.apache.log4j.Logger;

import java.util.Set;
import java.util.Enumeration;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.security.ProtectionDomain;
import java.security.CodeSource;
import java.net.URL;
import java.io.File;

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

    // Constructors --------------------------------------------------------------------------------

    /**
     * @param representative the class to be used to identify the package.
     */
    public PackageInspector(Class representative)
    {
        packageName = representative.getName();
        packageName = packageName.substring(0, packageName.lastIndexOf("."));

        ProtectionDomain pd = representative.getProtectionDomain();
        CodeSource cs = pd.getCodeSource();
        URL url = cs.getLocation();
        String protocol = url.getProtocol();

        if (!"file".equals(protocol))
        {
            throw new IllegalStateException("don't know to inspect the package with URL " + url);
        }

        File base = new File(url.getFile());

        String relativePath = packageName.replace('.', '/') + "/";

//        if (base.isDirectory())
//        {
//            new File(base, relativePath).listFiles(entitySetUpdater);
//        }
//        else
//        {
//            // we only know to handle jar files so far, not that we would ever need
//            // something else, presumably
//
//            JarFile jarFile = new JarFile(base);
//
//            log.debug("looking in " + jarFile.getName());
//
//            for(Enumeration<JarEntry> ents = jarFile.entries(); ents.hasMoreElements();)
//            {
//                JarEntry je = ents.nextElement();
//                String name = je.getName();
//                if (!name.startsWith(relativePath))
//                {
//                    continue;
//                }
//
//                name = name.substring(relativePath.length());
//
//                if (name.indexOf("/") != -1)
//                {
//                    // we don't look in subdirectories
//                    continue;
//                }
//
//                entitySetUpdater.accept(null, name);
//            }
//        }
    }

    // Public --------------------------------------------------------------------------------------

    public String getPackageName()
    {
        return packageName;
    }

    public Set<Class> inspect(Filter filter)
    {
        return null;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
