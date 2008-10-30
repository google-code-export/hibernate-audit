package com.googlecode.hibernate.audit.listener;

import org.hibernate.event.EventListeners;
import org.hibernate.impl.SessionFactoryImpl;

import java.util.Set;
import java.util.HashSet;
import java.util.Collections;
import java.util.Map;
import java.util.HashMap;
import java.lang.reflect.Method;
import java.lang.reflect.Array;
import java.lang.reflect.Modifier;

import com.googlecode.hibernate.audit.util.packinsp.PackageInspector;
import com.googlecode.hibernate.audit.util.packinsp.Filter;

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

    // Static --------------------------------------------------------------------------------------

    public static Set<String> ALL_EVENT_TYPES = new HashSet<String>();

    static
    {
        // based on Hibernate 3.2.6.GA
        
        ALL_EVENT_TYPES.add("auto-flush");
        ALL_EVENT_TYPES.add("merge");
        ALL_EVENT_TYPES.add("create");
        ALL_EVENT_TYPES.add("create-onflush");
        ALL_EVENT_TYPES.add("delete");
        ALL_EVENT_TYPES.add("dirty-check");
        ALL_EVENT_TYPES.add("evict");
        ALL_EVENT_TYPES.add("flush");
        ALL_EVENT_TYPES.add("flush-entity");
        ALL_EVENT_TYPES.add("load");
        ALL_EVENT_TYPES.add("load-collection");
        ALL_EVENT_TYPES.add("lock");
        ALL_EVENT_TYPES.add("refresh");
        ALL_EVENT_TYPES.add("replicate");
        ALL_EVENT_TYPES.add("save-update");
        ALL_EVENT_TYPES.add("save");
        ALL_EVENT_TYPES.add("update");
        ALL_EVENT_TYPES.add("pre-load");
        ALL_EVENT_TYPES.add("pre-update");
        ALL_EVENT_TYPES.add("pre-delete");
        ALL_EVENT_TYPES.add("pre-insert");
        ALL_EVENT_TYPES.add("pre-collection-recreate");
        ALL_EVENT_TYPES.add("pre-collection-remove");
        ALL_EVENT_TYPES.add("pre-collection-update");
        ALL_EVENT_TYPES.add("post-load");
        ALL_EVENT_TYPES.add("post-update");
        ALL_EVENT_TYPES.add("post-delete");
        ALL_EVENT_TYPES.add("post-insert");
        ALL_EVENT_TYPES.add("post-commit-update");
        ALL_EVENT_TYPES.add("post-commit-delete");
        ALL_EVENT_TYPES.add("post-commit-insert");
        ALL_EVENT_TYPES.add("post-collection-recreate");
        ALL_EVENT_TYPES.add("post-collection-remove");
        ALL_EVENT_TYPES.add("post-collection-update");

        ALL_EVENT_TYPES = Collections.unmodifiableSet(ALL_EVENT_TYPES);
    }

    private static Map<String, TypeHolder> auditedEventTypes;

    /**
     * @return all audited event types, determined based on the static list of avaliable audit
     *         listeners. The string representation of an event type is the one used by
     *         org.hibernate.event.EventListeners to key the listeners in its eventInterfaceFromType
     *         map.
     *
     * TODO implementation quasi-identical with Entities.getAuditEntities(). Could be consolidated.
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
                auditedEventTypes = new HashMap<String, TypeHolder>();

                PackageInspector pi = new PackageInspector(Listeners.class);
                pi.inspect(new Filter()
                {
                    public boolean accept(Class c)
                    {
                        int mods = c.getModifiers();

                        if (Modifier.isAbstract(mods))
                        {
                            return false;
                        }

                        String name = c.getName();
                        name = name.substring(name.lastIndexOf('.') + 1);

                        if (name.startsWith("Abstract"))
                        {
                            return false;
                        }

                        String eventType = classNameToHibernateEventType(name);

                        if (eventType == null)
                        {
                            return false;
                        }

                        auditedEventTypes.put(eventType, new TypeHolder(eventType, c));
                        return false; // we don't accumulate classes in inspect()'s result, we
                                      // already have them in auditedEventTypes
                    }
                });
            }

            return auditedEventTypes.keySet();
        }
    }

    public static String getEventListenersGetterName(String eventType)
    {
        return getEventListenersAccessorName(eventType, false);
    }

    public static String getEventListenersSetterName(String eventType)
    {
        return getEventListenersAccessorName(eventType, true);
    }

    /**
     * Returns the org.hibernate.event.EventListener getter method that returns the array of
     * listeners corresponding to the given eventType. For example, if eventType is 'post-insert',
     * then the returned getter method would be org.hibernate.event.EventListener's:
     *
     * public PostInsertEventListener[] getPostInsertEventListeners();
     *
     */
    public static Method getEventListenersGetter(String eventType) throws Exception
    {
        String getterName = getEventListenersGetterName(eventType);
        return EventListeners.class.getMethod(getterName);
    }

    /**
     * Returns the org.hibernate.event.EventListener setter method that can be used to set the array
     * of listeners corresponding to the given eventType. For example, if eventType is
     * 'post-insert', then the returned setter method would be org.hibernate.event.EventListener's:
     *
     * 	public void setPostInsertEventListeners(PostInsertEventListener[] postInsertEventListener);
     *
     */
    public static Method getEventListenersSetter(String eventType) throws Exception
    {
        String setterName = getEventListenersSetterName(eventType);
        Method[] ms = EventListeners.class.getMethods();
        for(Method m: ms)
        {
            if (m.getName().equals(setterName))
            {
                return m;
            }
        }

        return null;
    }

    /**
     * Given the string representation of an audited event type, the method returns the
     * corresponding AuditEventListener subtype.
     *
     * @return null if there's no such audit event type
     */
    public static Class getAuditEventListenerClass(String eventType)
    {
        if (auditedEventTypes == null)
        {
            return null;
        }

        TypeHolder h = auditedEventTypes.get(eventType);

        if (h == null)
        {
            return null;
        }

        return h.auditListenerClass;
    }

    /**
     * @param asf - the session factory to install the listener on (the "audited" session factory).
     * @param auditedEventType - one of the audit event types, as returned by
     *        Listeners.getAuditedEventTypes() ("post-insert", "post-update", etc.).
     * @param listener the listener instance to install.
     */
    public static void installAuditEventListener(SessionFactoryImpl asf,
                                                 String auditedEventType,
                                                 AuditEventListener listener) throws Exception
    {
        EventListeners els = asf.getEventListeners();

        Method getter = Listeners.getEventListenersGetter(auditedEventType);
        Method setter = Listeners.getEventListenersSetter(auditedEventType);

        // we expect a listener array here, anything else would be invalid state
        Object[] listeners = (Object[])getter.invoke(els);
        Class hibernateListenerInteface = els.getListenerClassFor(auditedEventType);
        Object[] newListeners =
            (Object[]) Array.newInstance(hibernateListenerInteface, listeners.length + 1);
        System.arraycopy(listeners, 0, newListeners, 0, listeners.length);

        newListeners[newListeners.length - 1] = listener;
        setter.invoke(els, ((Object)newListeners));
    }

    // Attributes ----------------------------------------------------------------------------------

    // Constructors --------------------------------------------------------------------------------

    // Public --------------------------------------------------------------------------------------

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

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

        // special cases

        String result = sb.toString();

        if ("save-or-update".equals(result))
        {
            result = "save-update";
        }

        return result;
    }

    private static String getEventListenersAccessorName(String eventType, boolean setter)
    {

        StringBuffer sb = new StringBuffer();

        if ("create-onflush".equals(eventType))
        {
            sb.append("PersistOnFlush");
        }
        else if ("save-update".equals(eventType))
        {
            sb.append("SaveOrUpdate");
        }
        else if ("create".equals(eventType))
        {
            sb.append("Persist");
        }
        else if ("load-collection".equals(eventType))
        {
            sb.append("InitializeCollection");
        }
        else
        {
            int length = eventType.length();
            for(int i = 0; i < length; i ++)
            {
                char c = eventType.charAt(i);

                if (i == 0)
                {
                    sb.append((char)(c - 32));
                }
                else
                {
                    if (c == '-')
                    {
                        c = eventType.charAt(++i);
                        sb.append((char)(c - 32));
                    }
                    else
                    {
                        sb.append(c);
                    }
                }
            }
        }

        sb.append("EventListeners");
        return (setter ? "set" : "get") + sb.toString();

    }


    // Inner classes -------------------------------------------------------------------------------

    private static class TypeHolder
    {
        public String eventType;
        public Class auditListenerClass;

        public TypeHolder(String eventType, Class listenerClass)
        {
            this.eventType = eventType;
            this.auditListenerClass = listenerClass;
        }
    }
}
