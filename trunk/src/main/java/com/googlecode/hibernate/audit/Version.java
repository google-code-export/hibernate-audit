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

import java.net.URL;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.apache.log4j.Logger;

public class Version {
	private static final Logger log = Logger.getLogger(Version.class);

	static {
		String version = null;

		try {
			URL versionUrl = Version.class.getResource("/" + Version.class.getName().replaceAll("\\.", "/") + ".class");
			if (versionUrl != null) {
				String cp = versionUrl.toString();
				String packageLocation = Version.class.getPackage().getName().replaceAll("\\.", "/");

				if (cp.indexOf(packageLocation) != -1) {
					cp = cp.substring(0, cp.indexOf(packageLocation)) + "META-INF/MANIFEST.MF";
					Manifest manifest = new Manifest((new URL(cp)).openStream());
					Attributes attributes = manifest.getMainAttributes();
					version = attributes.getValue("Implementation-Version");
				}
			}
		} catch (Throwable ignored) {
		}
		log.info("Hibernate Audit " + version != null ? version : "");
	}

	public static void touch() {
	}
}
