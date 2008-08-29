package com.googlecode.hibernate.audit.delta;

import com.googlecode.hibernate.audit.util.Entity;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Comparator;

/**
 * Represents a transactional delta.
 *
 * TODO experimental class
 *
 * @author <a href="mailto:ovidiu@feodorov.com">Ovidiu Feodorov</a>
 *
 * Copyright 2008 Ovidiu Feodorov
 *
 * @version <tt>$</tt>
 */
public class Delta
{
    // Constants -----------------------------------------------------------------------------------

    // Static --------------------------------------------------------------------------------------

    /**
     * TODO this method should go away from here.
     */
    public static void render(StringBuffer sb, Delta delta)
    {
        List<Entity> entities = new ArrayList<Entity>(delta.getEntities());

        Collections.sort(entities, new Comparator<Entity>()
        {
            public int compare(Entity o1, Entity o2)
            {
                return (int)(((Long)o1.getId()).longValue() - ((Long)o2.getId()).longValue());
            }
        });

        for(Entity e: entities)
        {

            List<Change> changes = delta.getChanges(e);

            if (changes.isEmpty())
            {
                continue;
            }

            for(Change c: changes)
            {
                sb.append("<tr><td>changeid</td><td>username</td><td>timestamp</td>").
                    append("<td>").append(e.getType().getName()).append("</td>").
                    append("<td>").append(e.getId()).append("</td>").
                    append("<td>").append(c.getPropertyName()).append("</td>").
                    append("<td>").append(c.getPropertyValue()).append("</td>").
                    append("</tr>\n");
            }
        }
    }

    // Attributes ----------------------------------------------------------------------------------

    private Map<Entity, List<Change>> changes;

    // Constructors --------------------------------------------------------------------------------

    public Delta()
    {
        changes = new HashMap<Entity, List<Change>>();
    }

    // Public --------------------------------------------------------------------------------------

    public void addChange(Change c)
    {
        Entity e = c.getEntity();
        List<Change> lc = changes.get(e);

        if (lc == null)
        {
            lc = new ArrayList<Change>();
            changes.put(e, lc);
        }

        lc.add(c);
    }

    public Set<Entity> getEntities()
    {
        return changes.keySet();
    }

    public List<Change> getChanges(Entity e)
    {
        List<Change> lc = changes.get(e);

        if (lc == null)
        {
            lc = Collections.emptyList();
        }

        return lc;
    }

    // Package protected ---------------------------------------------------------------------------

    // Protected -----------------------------------------------------------------------------------

    // Private -------------------------------------------------------------------------------------

    // Inner classes -------------------------------------------------------------------------------
}
