/**
 * Copyright (C) 2009 Krasimir Chobantonov <kchobantonov@yahoo.com>
 * This file is part of Hibernate Audit.

 * Hibernate Audit is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 * 
 * Hibernate Audit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with Hibernate Audit.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.googlecode.hibernate.audit;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Version {
    private static final Logger log = Logger.getLogger(Version.class);

    static {
        final Properties prop = new Properties();
        final InputStream in = Version.class.getResourceAsStream("/meta-inf/Manifest.mf");
        if (in != null) {
            try {
                prop.load(in);
            } catch (IOException e) {
            }
        }
        final String version = prop.getProperty("Implementation-Version");
        log.info("Hibernate Audit " + version != null ? version : "");
    }

    public static void touch() {
    }
}
