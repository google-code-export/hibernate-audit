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
package com.googlecode.hibernate.audit.test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.URIConverter;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;

public final class EMFUtil {

    private EMFUtil() {
    }

    public static EList<EObject> toEObject(final String xmi, final boolean unsetID, EPackage[] ePackages) {
        try {
            Map<Object, Object> options = new HashMap<Object, Object>();
            options.put(XMLResource.OPTION_ENCODING, "UTF-8");
            options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
            options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);

            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());

            // Register XML resource factory
            if (ePackages != null) {
                for (int i = 0; i < ePackages.length; i++) {
                    resourceSet.getPackageRegistry().put(ePackages[i].getNsURI(), ePackages[i]);
                }
            }

            URIConverter.ReadableInputStream input = new URIConverter.ReadableInputStream(new StringReader(xmi), "UTF-8");
            Resource resource = resourceSet.createResource(URI.createURI(""));

            resource.load(input, options);

            if (!resource.getErrors().isEmpty() || !resource.getWarnings().isEmpty()) {
                throw new IllegalArgumentException("Unable to load the EObject. Errors:" + resource.getErrors() + ";Warnings:" + resource.getWarnings());
            }

            EList<EObject> result = resource.getContents();

            if (result != null && unsetID) {
                Map<EObject, Boolean> preventCycles = new IdentityHashMap<EObject, Boolean>();

                ArrayList<EObject> rootList = new ArrayList<EObject>();

                // collect all referenced & proxied objects
                for (EObject eObj : result) {
                    readReferences(eObj, preventCycles, rootList);
                }

                for (EObject eobj : rootList) {
                    if (eobj.eClass().getEIDAttribute() != null) {
                        eobj.eSet(eobj.eClass().getEIDAttribute(), null);
                    }
                    // composition objects
                    Iterator<EObject> iterator = eobj.eAllContents();
                    while (iterator.hasNext()) {
                        EObject dependentEObject = iterator.next();
                        if (dependentEObject.eClass().getEIDAttribute() != null) {
                            dependentEObject.eSet(dependentEObject.eClass().getEIDAttribute(), null);
                        }
                    }
                }
            }

            return result;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public static String toXMI(final EObject object, EPackage[] ePackages) {
        try {
            Map<Object, Object> options = new HashMap<Object, Object>();
            options.put(XMLResource.OPTION_ENCODING, "UTF-8");
            options.put(XMLResource.OPTION_DEFER_IDREF_RESOLUTION, Boolean.TRUE);
            options.put(XMLResource.OPTION_DEFER_ATTACHMENT, Boolean.TRUE);

            StringWriter sw = new StringWriter();
            URIConverter.WriteableOutputStream uws = new URIConverter.WriteableOutputStream(sw, "UTF-8");

            ResourceSet resourceSet = new ResourceSetImpl();
            resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());

            // Register XML resource factory

            if (ePackages != null) {
                for (int i = 0; i < ePackages.length; i++) {
                    resourceSet.getPackageRegistry().put(ePackages[i].getNsURI(), ePackages[i]);
                }
            }

            Resource resource = resourceSet.createResource(URI.createURI(""));

            Map<EObject, Boolean> preventCycles = new IdentityHashMap<EObject, Boolean>();
            ArrayList<EObject> rootList = new ArrayList<EObject>();

            // collect all referenced & proxied objects
            readReferences(object, preventCycles, rootList);

            for (EObject obj : rootList) {
                resource.getContents().add(obj);
            }
            resource.save(uws, options);

            return sw.toString();
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    private static void readReferences(final EObject eobject, final Map<EObject, Boolean> preventCycles, final List<EObject> rootList) {
        if (preventCycles.containsKey(eobject)) { // been here get away
            return;
        }
        preventCycles.put(eobject, null);

        if (eobject.eContainer() != null) {
            readReferences(eobject.eContainer(), preventCycles, rootList);
        } else { // a root object
            rootList.add(eobject);
        }

        for (Object erefObj : eobject.eClass().getEAllReferences()) {
            EReference eref = (EReference) erefObj;
            final Object value = eobject.eGet(eref);
            if (value == null) {
                continue;
            }
            if (value instanceof List) {
                for (Object obj : (List<?>) value) {
                    readReferences((EObject) obj, preventCycles, rootList);
                }
            } else { // an eobject
                readReferences((EObject) value, preventCycles, rootList);
            }
        }
    }

}
